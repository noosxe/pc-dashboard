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
                gpuUsage = Random.nextFloat() * 100,
                gpuTemp = 30 + Random.nextFloat() * 60,
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
                appName = "Slack",
                summary = "New message from Alice",
                body = "Hey, are you free for a call?",
                timestamp = System.currentTimeMillis() / 1000
            )
        )
        delay(10000)
        emit(
            PcNotification(
                appName = "System",
                summary = "Update available",
                body = "A new system update is ready to install.",
                timestamp = System.currentTimeMillis() / 1000
            )
        )
    }
}
