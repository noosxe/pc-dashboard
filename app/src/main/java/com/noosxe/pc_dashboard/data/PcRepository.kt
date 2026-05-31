package com.noosxe.pc_dashboard.data

import kotlinx.coroutines.flow.Flow

interface PcRepository {
    fun getPcStatsFlow(): Flow<PcStats>
    fun getNotificationsFlow(): Flow<PcNotification>
    fun getSessionLockFlow(): Flow<Boolean>
    fun getMediaStateFlow(): Flow<MediaState>
    fun sendMediaCommand(player: String, command: String)
}
