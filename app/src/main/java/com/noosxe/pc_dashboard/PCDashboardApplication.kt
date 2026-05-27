package com.noosxe.pc_dashboard

import android.app.Application
import com.noosxe.pc_dashboard.data.PcRepository
import com.noosxe.pc_dashboard.data.WebSocketPcRepository
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class PCDashboardApplication : Application() {

    // Centralized repository instance to be shared across the app
    lateinit var pcRepository: PcRepository
        private set

    override fun onCreate() {
        super.onCreate()
        
        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.SECONDS) // For WebSockets
            .build()

        // Use the fixed local loopback address for the WebSocket server
        pcRepository = WebSocketPcRepository(client = client, wsUrl = "ws://127.0.0.1:12345/ws")
    }
}
