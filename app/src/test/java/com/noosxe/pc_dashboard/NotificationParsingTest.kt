package com.noosxe.pc_dashboard

import com.noosxe.pc_dashboard.data.NotificationMessage
import com.noosxe.pc_dashboard.data.ServerMessage
import com.noosxe.pc_dashboard.data.TelemetryMessage
import com.noosxe.pc_dashboard.data.toDomain
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationParsingTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testParseNotificationEvent() {
        val input = """
            {
              "type": "notification_event",
              "timestamp": 1716213825,
              "data": {
                "app_name": "Slack",
                "replaces_id": 0,
                "app_icon": "slack",
                "summary": "New message from Alice",
                "body": "Hey, are you free for a call?",
                "actions": ["default", "Activate"],
                "hints": {
                  "urgency": 1
                },
                "expire_timeout": 5000
              }
            }
        """.trimIndent()

        val message = json.decodeFromString<ServerMessage>(input)
        assertTrue(message is NotificationMessage)
        val notification = (message as NotificationMessage).toDomain()
        
        assertEquals("Slack", notification.appName)
        assertEquals("New message from Alice", notification.summary)
        assertEquals("Hey, are you free for a call?", notification.body)
        assertEquals(1716213825L, notification.timestamp)
    }

    @Test
    fun testParseTelemetry() {
        val input = """
            {
              "type": "telemetry",
              "timestamp": 1716213825,
              "data": {
                "cpu": { "usage_percent": 15.5, "temp_celsius": 45.0 },
                "gpu": { "usage_percent": 20.0, "temp_celsius": 50.0, "vram_used_bytes": 1073741824, "vram_total_bytes": 8589934592 },
                "ram": { "used_bytes": 4294967296, "total_bytes": 17179869184, "percentage": 25.0 }
              }
            }
        """.trimIndent()

        val message = json.decodeFromString<ServerMessage>(input)
        assertTrue(message is TelemetryMessage)
        val stats = (message as TelemetryMessage).toDomain()

        assertEquals(15.5f, stats.cpuUsage)
        assertEquals(45.0f, stats.cpuTemp)
        assertEquals(4.0f, stats.ramUsage)
        assertEquals(16.0f, stats.ramTotal)
    }
}
