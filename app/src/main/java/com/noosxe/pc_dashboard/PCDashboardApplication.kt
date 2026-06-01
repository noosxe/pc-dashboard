package com.noosxe.pc_dashboard

import android.app.Application
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.svg.SvgDecoder
import com.noosxe.pc_dashboard.data.PcRepository
import com.noosxe.pc_dashboard.data.SettingsRepository
import com.noosxe.pc_dashboard.data.WebSocketPcRepository
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class PCDashboardApplication : Application(), SingletonImageLoader.Factory {

    // Centralized repository instances to be shared across the app
    lateinit var pcRepository: PcRepository
        private set
    lateinit var settingsRepository: SettingsRepository
        private set

    override fun newImageLoader(context: coil3.PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(SvgDecoder.Factory())
            }
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        
        settingsRepository = SettingsRepository(this)
        
        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.SECONDS) // For WebSockets
            .build()

        // Use the fixed local loopback address for the WebSocket server
        pcRepository = WebSocketPcRepository(client = client, wsUrl = "ws://127.0.0.1:12345/ws")
    }
}
