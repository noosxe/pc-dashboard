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
}
