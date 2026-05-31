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

data class MediaState(
    val players: List<PlayerState> = emptyList()
)

data class PlayerState(
    val player: String = "",
    val identity: String = "",
    val desktopEntry: String = "",
    val title: String = "",
    val artist: String = "",
    val status: String = "Stopped",
    val positionMs: Long = 0,
    val lengthMs: Long = 0,
    val volume: Double = 0.0,
    val artUrl: String = ""
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
@SerialName("media_state")
data class MediaMessage(
    override val type: String,
    override val timestamp: Long,
    val data: MediaListDataDto
) : ServerMessage()

@Serializable
data class MediaListDataDto(
    @SerialName("active_players") val activePlayers: List<MediaDataDto>
)

@Serializable
data class MediaDataDto(
    @SerialName("player_name") val playerName: String,
    val identity: String? = null,
    @SerialName("desktop_entry") val desktopEntry: String? = null,
    @SerialName("playback_status") val playbackStatus: String,
    val volume: Double,
    @SerialName("position_microseconds") val positionMicroseconds: Long,
    val metadata: MediaMetadataDto
)

@Serializable
data class MediaMetadataDto(
    @SerialName("track_id") val trackId: String,
    val title: String,
    val artist: List<String>,
    val album: String,
    @SerialName("art_url") val artUrl: String,
    @SerialName("length_microseconds") val lengthMicroseconds: Long
)

@Serializable
data class MediaActionRequest(
    val type: String = "media_command",
    @SerialName("player_name") val playerName: String,
    val command: String,
    val args: MediaActionArgsDto? = null
)

@Serializable
data class MediaActionArgsDto(
    @SerialName("offset_microseconds") val offsetMicroseconds: Long? = null,
    @SerialName("position_microseconds") val positionMicroseconds: Long? = null,
    @SerialName("track_id") val trackId: String? = null,
    val volume: Double? = null
)

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

@Serializable
@SerialName("media_response")
data class MediaResponseMessage(
    override val type: String,
    val status: String,
    val message: String? = null,
    override val timestamp: Long = 0
) : ServerMessage()

@Serializable
@SerialName("success")
data class SuccessMessage(
    override val type: String,
    val status: String,
    val message: String? = null,
    override val timestamp: Long = 0
) : ServerMessage()

@Serializable
data class UnknownMessage(
    override val type: String,
    override val timestamp: Long = 0
) : ServerMessage()

object ServerMessageSerializer : JsonContentPolymorphicSerializer<ServerMessage>(ServerMessage::class) {
    override fun selectDeserializer(element: JsonElement) = when (element.jsonObject["type"]?.jsonPrimitive?.content) {
        "notification_event" -> NotificationMessage.serializer()
        "session_lock" -> SessionLockMessage.serializer()
        "media_state" -> MediaMessage.serializer()
        "media_response" -> MediaResponseMessage.serializer()
        "success" -> SuccessMessage.serializer()
        "telemetry" -> TelemetryMessage.serializer()
        else -> UnknownMessage.serializer()
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

fun MediaMessage.toDomain(): MediaState {
    return MediaState(
        players = data.activePlayers.map { player ->
            PlayerState(
                player = player.playerName,
                identity = player.identity ?: player.playerName,
                desktopEntry = player.desktopEntry ?: "",
                title = player.metadata.title,
                artist = player.metadata.artist.joinToString(", "),
                status = player.playbackStatus,
                positionMs = player.positionMicroseconds / 1000,
                lengthMs = player.metadata.lengthMicroseconds / 1000,
                volume = player.volume,
                artUrl = player.metadata.artUrl
            )
        }
    )
}
