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
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.shareIn
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class WebSocketPcRepository(
    private val client: OkHttpClient = OkHttpClient(),
    private val wsUrl: String = "ws://localhost:12345/ws"
) : PcRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val serverMessageFlow: Flow<ServerMessage> = callbackFlow {
        val request = Request.Builder().url(wsUrl).build()
        
        val listener = object : WebSocketListener() {
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
}
