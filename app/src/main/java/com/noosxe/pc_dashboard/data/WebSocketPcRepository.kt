package com.noosxe.pc_dashboard.data

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
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

class WebSocketPcRepository(
    private val client: OkHttpClient = OkHttpClient(),
    private val wsUrl: String = "ws://127.0.0.1:12345/ws"
) : PcRepository {

    private val json = Json { 
        ignoreUnknownKeys = true 
        encodeDefaults = true
    }
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var webSocket: WebSocket? = null

    private val serverMessageFlow: Flow<ServerMessage> = callbackFlow {
        val request = Request.Builder().url(wsUrl).build()
        
        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                this@WebSocketPcRepository.webSocket = webSocket
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val serverMessage = json.decodeFromString<ServerMessage>(text)
                    trySend(serverMessage)
                } catch (e: Exception) {
                    Log.e("WebSocketPcRepo", "Error parsing JSON: $text", e)
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
                close()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocketPcRepo", "WebSocket failure", t)
                close(t)
            }
        }

        val webSocket = client.newWebSocket(request, listener)

        awaitClose {
            webSocket.close(1000, "Flow closed")
            this@WebSocketPcRepository.webSocket = null
        }
    }
    .onStart { Log.d("WebSocketPcRepo", "Starting WebSocket flow") }
    .retryWhen { cause, attempt ->
        Log.d("WebSocketPcRepo", "Retrying WebSocket connection, attempt: $attempt", cause)
        delay(2000)
        true
    }
    .shareIn(
        scope = repositoryScope,
        started = SharingStarted.WhileSubscribed(5000),
        replay = 1
    )

    override fun getPcStatsFlow(): Flow<PcStats> = serverMessageFlow
        .filterIsInstance<TelemetryMessage>()
        .map { it.toDomain() }

    override fun getNotificationsFlow(): Flow<PcNotification> = serverMessageFlow
        .filterIsInstance<NotificationMessage>()
        .map { it.toDomain() }

    override fun getSessionLockFlow(): Flow<Boolean> = serverMessageFlow
        .filterIsInstance<SessionLockMessage>()
        .map { it.data.locked }

    override fun getMediaStateFlow(): Flow<MediaState> = serverMessageFlow
        .filterIsInstance<MediaMessage>()
        .map { 
            val domain = it.toDomain()
            // Ensure stable order by sorting players by their ID (player name)
            domain.copy(players = domain.players.sortedBy { p -> p.player })
        }

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
}
