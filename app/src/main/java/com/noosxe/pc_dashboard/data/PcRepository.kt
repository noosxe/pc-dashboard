package com.noosxe.pc_dashboard.data

import kotlinx.coroutines.flow.Flow

interface PcRepository {
    fun getPcStatsFlow(): Flow<PcStats>
    fun getNotificationsFlow(): Flow<PcNotification>
}
