package com.noosxe.pc_dashboard

import android.app.Application
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.svg.SvgDecoder
import com.noosxe.pc_dashboard.data.PcRepository
import com.noosxe.pc_dashboard.data.SettingsRepository
import com.noosxe.pc_dashboard.data.WebSocketPcRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class PCDashboardApplication : Application(), SingletonImageLoader.Factory {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

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

        // Initialize with default values or latest from settings
        pcRepository = WebSocketPcRepository(client = client)

        combine(settingsRepository.serverHost, settingsRepository.serverPort) { host, port ->
            host to port
        }
        .onEach { (host, port) ->
            pcRepository.updateConnectionSettings(host, port)
        }
        .launchIn(applicationScope)
    }
}
