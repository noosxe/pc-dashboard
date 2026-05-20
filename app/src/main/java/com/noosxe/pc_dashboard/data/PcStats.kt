package com.noosxe.pc_dashboard.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Domain model used by the UI.
 */
data class PcStats(
    val cpuUsage: Float = 0f,
    val cpuTemp: Float = 0f,
    val gpuUsage: Float = 0f,
    val gpuTemp: Float = 0f,
    val ramUsage: Float = 0f,
    val vramUsage: Float = 0f,
    val ramTotal: Float = 16f, // GB
    val vramTotal: Float = 8f  // GB
)

/**
 * Data Transfer Objects (DTOs) for server messages.
 */
@Serializable
data class ServerMessage(
    val type: String,
    val timestamp: Long,
    val data: TelemetryData
)

@Serializable
data class TelemetryData(
    val cpu: CpuStatsDto,
    val gpu: GpuStatsDto,
    val ram: RamStatsDto
)

@Serializable
data class CpuStatsDto(
    @SerialName("usage_percent") val usagePercent: Float,
    @SerialName("temp_celsius") val tempCelsius: Float
)

@Serializable
data class GpuStatsDto(
    @SerialName("usage_percent") val usagePercent: Float,
    @SerialName("temp_celsius") val tempCelsius: Float,
    @SerialName("vram_used_bytes") val vramUsedBytes: Long,
    @SerialName("vram_total_bytes") val vramTotalBytes: Long
)

@Serializable
data class RamStatsDto(
    @SerialName("used_bytes") val usedBytes: Long,
    @SerialName("total_bytes") val totalBytes: Long,
    val percentage: Float
)

/**
 * Maps the server's DTO structure to our domain model.
 */
fun ServerMessage.toDomain(): PcStats {
    val bytesToGb = 1024.0f * 1024.0f * 1024.0f
    
    return PcStats(
        cpuUsage = data.cpu.usagePercent,
        cpuTemp = data.cpu.tempCelsius,
        gpuUsage = data.gpu.usagePercent,
        gpuTemp = data.gpu.tempCelsius,
        ramUsage = data.ram.usedBytes / bytesToGb,
        ramTotal = data.ram.totalBytes / bytesToGb,
        vramUsage = data.gpu.vramUsedBytes / bytesToGb,
        vramTotal = data.gpu.vramTotalBytes / bytesToGb
    )
}
