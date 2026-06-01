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
import kotlinx.coroutines.flow.distinctUntilChanged

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

    // Cache for song art data to handle messages with missing art_url for the same song
    private val artUrlCache = mutableMapOf<String, PlayerState>()

    private val serverMessageFlow: Flow<ServerMessage> = callbackFlow {
        val request = Request.Builder().url(wsUrl).build()
        
        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                this@WebSocketPcRepository.webSocket = webSocket
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val serverMessage = json.decodeFromString<ServerMessage>(text)
                    if (BuildConfig.DEBUG) {
                        if (serverMessage is MediaMessage) {
                            val artLengths = serverMessage.data.activePlayers.map { it.metadata.artUrl.length }
                            Log.d("WS_MEDIA", "Received media state. Art lengths: $artLengths")
                        } else {
                            Log.d("WS_${serverMessage.type.uppercase()}", text.take(1000))
                        }
                    }
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
        .map { message ->
            val domain = message.toDomain()
            val playersWithArt = domain.players.map { player ->
                // Create a unique key for the song to cache its art URL
                val rawKey = "${player.trackId}|${player.title}|${player.artist}|${player.album}"
                val songKey = try {
                    val digest = MessageDigest.getInstance("SHA-1")
                    val result = digest.digest(rawKey.toByteArray())
                    result.joinToString("") { "%02x".format(it) }
                } catch (e: Exception) {
                    rawKey // Fallback to raw key if hashing fails
                }

                val processedPlayer = if (player.artUrl.isNotEmpty() && !player.artUrl.startsWith("file://")) {
                    // Update cache if new art is provided (either URL or Bytes)
                    artUrlCache[songKey] = player
                    player
                } else {
                    // Try to retrieve from cache if art is missing in the message
                    val cachedPlayer = artUrlCache[songKey]
                    if (cachedPlayer != null) {
                        player.copy(
                            artUrl = cachedPlayer.artUrl,
                            artBytes = cachedPlayer.artBytes
                        )
                    } else {
                        player
                    }
                }

                // If this is a new song (different songKey), we should NOT use art from a previous song.
                // However, the current logic only populates art if the message has it OR if it's in cache for THIS songKey.
                // So "old image for a new track" might be coming from Coil's cache if the trackId is the same or blank.
                processedPlayer
            }
            // Ensure stable order by sorting players by their ID (player name)
            MediaState(players = playersWithArt.sortedBy { it.player })
        }
        .distinctUntilChanged()

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
}
