package com.noosxe.pc_dashboard.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

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

data class PcNotification(
    val id: Int,
    val appName: String,
    val summary: String,
    val body: String,
    val actions: List<String>,
    val timestamp: Long
)

/**
 * Data Transfer Objects (DTOs) for server messages.
 */
@Serializable(with = ServerMessageSerializer::class)
sealed class ServerMessage {
    abstract val type: String
    abstract val timestamp: Long
}

@Serializable
@SerialName("telemetry")
data class TelemetryMessage(
    override val type: String,
    override val timestamp: Long,
    val data: TelemetryData
) : ServerMessage()

@Serializable
@SerialName("notification_event")
data class NotificationMessage(
    override val type: String,
    override val timestamp: Long,
    val data: NotificationDataDto
) : ServerMessage()

@Serializable
@SerialName("session_lock")
data class SessionLockMessage(
    override val type: String,
    override val timestamp: Long,
    val data: SessionLockDataDto
) : ServerMessage()

@Serializable
data class SessionLockDataDto(
    val locked: Boolean
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

@Serializable
data class NotificationDataDto(
    @SerialName("app_name") val appName: String,
    @SerialName("replaces_id") val replacesId: Int,
    @SerialName("app_icon") val appIcon: String,
    val summary: String,
    val body: String,
    val actions: List<String>,
    val hints: JsonElement,
    @SerialName("expire_timeout") val expireTimeout: Int
)

object ServerMessageSerializer : JsonContentPolymorphicSerializer<ServerMessage>(ServerMessage::class) {
    override fun selectDeserializer(element: JsonElement) = when (element.jsonObject["type"]?.jsonPrimitive?.content) {
        "notification_event" -> NotificationMessage.serializer()
        "session_lock" -> SessionLockMessage.serializer()
        else -> TelemetryMessage.serializer()
    }
}

/**
 * Maps the server's DTO structure to our domain model.
 */
fun TelemetryMessage.toDomain(): PcStats {
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

fun NotificationMessage.toDomain(): PcNotification {
    return PcNotification(
        id = data.replacesId,
        appName = data.appName,
        summary = data.summary,
        body = data.body,
        actions = data.actions,
        timestamp = timestamp
    )
}
