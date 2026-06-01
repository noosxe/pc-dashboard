package com.noosxe.pc_dashboard.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.random.Random

class MockPcRepository : PcRepository {
    override fun getPcStatsFlow(): Flow<PcStats> = flow {
        while (true) {
            val stats = PcStats(
                cpuUsage = Random.nextFloat() * 100,
                cpuTemp = 30 + Random.nextFloat() * 50,
                cpuFreq = 2000 + Random.nextFloat() * 3000,
                gpuUsage = Random.nextFloat() * 100,
                gpuTemp = 30 + Random.nextFloat() * 60,
                gpuFreq = 500 + Random.nextFloat() * 1500,
                ramUsage = Random.nextFloat() * 16,
                vramUsage = Random.nextFloat() * 8
            )
            emit(stats)
            delay(1000) // Update every second
        }
    }

    override fun getNotificationsFlow(): Flow<PcNotification> = flow {
        delay(5000) // Wait 5 seconds before first notification
        emit(
            PcNotification(
                id = 1,
                appName = "Slack",
                appIcon = "slack",
                appIconBase64 = null,
                summary = "New message from Alice",
                body = "Hey, are you free for a call?",
                actions = listOf("default", "Activate", "dismiss", "Dismiss"),
                timestamp = System.currentTimeMillis() / 1000
            )
        )
        delay(10000)
        emit(
            PcNotification(
                id = 2,
                appName = "System",
                appIcon = "system-software-update",
                appIconBase64 = null,
                summary = "Update available",
                body = "A new system update is ready to install.",
                actions = emptyList(),
                timestamp = System.currentTimeMillis() / 1000
            )
        )
    }

    override fun getSessionLockFlow(): Flow<Boolean> = flow {
        while (true) {
            emit(false)
            delay(1000000) // Effectively disabled for production mock
        }
    }

    override fun getPowerProfileFlow(): Flow<String> = flow {
        val profiles = listOf("balanced", "power-saver", "performance")
        var index = 0
        delay(8000) // Initial delay
        while (true) {
            emit(profiles[index])
            index = (index + 1) % profiles.size
            delay(30000) // Update every 30 seconds
        }
    }

    override fun getMediaStateFlow(): Flow<MediaState> = flow {
        while (true) {
            emit(
                MediaState(
                    players = listOf(
                        PlayerState(
                            player = "spotify",
                            identity = "Spotify",
                            title = "Never Gonna Give You Up",
                            artist = "Rick Astley",
                            album = "Whenever You Need Somebody",
                            status = "Playing",
                            positionMs = 30000,
                            lengthMs = 213000,
                            volume = 0.8,
                            artUrl = "https://i.scdn.co/image/ab67616d0000b273575511af41b4b419830739c2"
                        )
                    )
                )
            )
            delay(2000)
        }
    }

    override fun getCommandResponsesFlow(): Flow<String> = flow {
        // No responses for mock
    }

    override fun sendMediaCommand(player: String, command: String) {
        // No-op for mock
    }

    override fun sendNotificationAction(notificationId: Int, actionKey: String) {
        // No-op for mock
    }

    override fun dismissNotification(notificationId: Int) {
        // No-op for mock
    }
}
