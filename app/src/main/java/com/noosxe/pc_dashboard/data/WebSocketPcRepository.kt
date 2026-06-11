package com.noosxe.pc_dashboard.data

import android.util.Log
import com.noosxe.pc_dashboard.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.shareIn
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.security.MessageDigest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest

@OptIn(ExperimentalCoroutinesApi::class)
class WebSocketPcRepository(
    private val client: OkHttpClient = OkHttpClient(),
    initialHost: String = "127.0.0.1",
    initialPort: Int = 12345
) : PcRepository {

    private val json = Json { 
        ignoreUnknownKeys = true 
        encodeDefaults = true
    }
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var webSocket: WebSocket? = null

    private val connectionSettings = MutableStateFlow(initialHost to initialPort)

    private val serverMessageFlow: Flow<ServerMessage> = connectionSettings
        .flatMapLatest { (host, port) ->
            val wsUrl = "ws://$host:$port/ws"
            callbackFlow {
                val request = Request.Builder().url(wsUrl).build()
                
                val listener = object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        this@WebSocketPcRepository.webSocket = webSocket
                        Log.d("WebSocketPcRepo", "WebSocket connected to $wsUrl")
                    }

                    override fun onMessage(webSocket: WebSocket, text: String) {
                        try {
                            val serverMessage = json.decodeFromString<ServerMessage>(text)
                            if (serverMessage is NotificationMessage) {
                                Log.d("PERF_LATENCY", "WS_RECV_NOTIFICATION id=${serverMessage.data.id} ts=${System.currentTimeMillis()}")
                            }
                            if (BuildConfig.DEBUG) {
                                Log.d("WS_RECV", text.take(1000))
                            }
                            trySend(serverMessage)
                        } catch (e: Exception) {
                            Log.e("WebSocketPcRepo", "Error parsing JSON from $wsUrl: $text", e)
                        }
                    }

                    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                        Log.d("WebSocketPcRepo", "WebSocket closing: $code $reason")
                        webSocket.close(1000, null)
                        close()
                    }

                    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                        Log.e("WebSocketPcRepo", "WebSocket failure for $wsUrl", t)
                        close(t)
                    }
                }

                val webSocket = client.newWebSocket(request, listener)

                awaitClose {
                    Log.d("WebSocketPcRepo", "Closing WebSocket for $wsUrl")
                    webSocket.close(1000, "Flow closed")
                    if (this@WebSocketPcRepository.webSocket == webSocket) {
                        this@WebSocketPcRepository.webSocket = null
                    }
                }
            }
            .onStart { Log.d("WebSocketPcRepo", "Starting WebSocket flow for $wsUrl") }
            .retryWhen { cause, attempt ->
                Log.d("WebSocketPcRepo", "Retrying WebSocket connection to $wsUrl, attempt: $attempt", cause)
                delay(2000)
                true
            }
        }
        .shareIn(
            scope = repositoryScope,
            started = SharingStarted.WhileSubscribed(5000),
            replay = 1
        )

    private val telemetryFlow = serverMessageFlow
        .filterIsInstance<TelemetryMessage>()
        .map { it.toDomain() }
        .shareIn(repositoryScope, SharingStarted.WhileSubscribed(5000), replay = 1)

    private val notificationFlow = serverMessageFlow
        .filterIsInstance<NotificationMessage>()
        .map { it.toDomain() }
        .shareIn(repositoryScope, SharingStarted.WhileSubscribed(5000), replay = 0)

    private val sessionLockFlow = serverMessageFlow
        .filterIsInstance<SessionLockMessage>()
        .map { it.data.locked }
        .distinctUntilChanged()
        .shareIn(repositoryScope, SharingStarted.Eagerly, replay = 1)

    private val powerProfileFlow = serverMessageFlow
        .filterIsInstance<PowerProfileMessage>()
        .map { it.data.activeProfile }
        .distinctUntilChanged()
        .shareIn(repositoryScope, SharingStarted.Eagerly, replay = 1)

    private val mediaStateFlow = serverMessageFlow
        .filterIsInstance<MediaMessage>()
        .map { message ->
            val domain = message.toDomain()
            MediaState(players = domain.players.sortedBy { it.player })
        }
        .distinctUntilChanged()
        .shareIn(repositoryScope, SharingStarted.Eagerly, replay = 1)

    override fun getPcStatsFlow(): Flow<PcStats> = telemetryFlow

    override fun getNotificationsFlow(): Flow<PcNotification> = notificationFlow

    override fun getSessionLockFlow(): Flow<Boolean> = sessionLockFlow

    override fun getPowerProfileFlow(): Flow<String> = powerProfileFlow

    override fun getMediaStateFlow(): Flow<MediaState> = mediaStateFlow


    override fun getCommandResponsesFlow(): Flow<String> = serverMessageFlow
        .map {
            when (it) {
                is MediaResponseMessage -> "Media Command: ${it.status}${it.message?.let { m -> " ($m)" } ?: ""}"
                is SuccessMessage -> "Success: ${it.status}${it.message?.let { m -> " ($m)" } ?: ""}"
                else -> null
            }
        }
        .filterNotNull()

    override fun sendMediaCommand(player: String, command: String) {
        val mappedCommand = when (command.lowercase()) {
            "playpause" -> "play_pause"
            else -> command.lowercase()
        }
        val request = MediaActionRequest(playerName = player, command = mappedCommand)
        val text = json.encodeToString(request)
        webSocket?.send(text) ?: Log.e("WebSocketPcRepo", "WebSocket not connected, cannot send command")
    }

    override fun sendNotificationAction(notificationId: Int, actionKey: String) {
        val request = NotificationActionRequest(notificationId = notificationId, actionKey = actionKey)
        val text = json.encodeToString(request)
        webSocket?.send(text) ?: Log.e("WebSocketPcRepo", "WebSocket not connected, cannot send notification action")
    }

    override fun dismissNotification(notificationId: Int) {
        val request = NotificationDismissRequest(notificationId = notificationId)
        val text = json.encodeToString(request)
        webSocket?.send(text) ?: Log.e("WebSocketPcRepo", "WebSocket not connected, cannot dismiss notification")
    }

    override fun updateConnectionSettings(host: String, port: Int) {
        connectionSettings.value = host to port
    }
}
