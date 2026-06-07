package com.noosxe.pc_dashboard.data

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TelemetryMappingTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `should map telemetry DTO with swap and zram to domain model`() {
        val rawJson = """
            {
                "type": "telemetry",
                "timestamp": 1716213825,
                "data": {
                    "cpu": {
                        "usage_percent": 18.7,
                        "temp_celsius": 49.0,
                        "freq_mhz": 3200.0,
                        "power_watts": 45.2
                    },
                    "gpu": {
                        "usage_percent": 41.0,
                        "temp_celsius": 58.0,
                        "vram_used_bytes": 3121561600,
                        "vram_total_bytes": 8589934592,
                        "freq_mhz": 1200.0,
                        "power_watts": 125.5
                    },
                    "ram": {
                        "used_bytes": 14212567040,
                        "total_bytes": 34359738368,
                        "percentage": 41.3
                    },
                    "swap": {
                        "used_bytes": 1073741824,
                        "total_bytes": 4294967296,
                        "percentage": 25.0
                    },
                    "zram": {
                        "mem_used_total_bytes": 536870912,
                        "total_bytes": 2147483648,
                        "compression_ratio": 2.84
                    },
                    "flags": {
                        "swap_supported": true,
                        "zram_supported": true
                    }
                }
            }
        """.trimIndent()

        val message = json.decodeFromString<ServerMessage>(rawJson) as TelemetryMessage
        val domain = message.toDomain()

        val bytesToGb = 1024.0f * 1024.0f * 1024.0f

        assertEquals(18.7f, domain.cpuUsage)
        assertEquals(14212567040L / bytesToGb, domain.ramUsage)
        
        // Swap assertions
        assertTrue(domain.swapSupported)
        assertEquals(1073741824L / bytesToGb, domain.swapUsage)
        assertEquals(4294967296L / bytesToGb, domain.swapTotal)
        assertEquals(25.0f, domain.swapPercentage)

        // zRAM assertions
        assertTrue(domain.zramSupported)
        assertEquals(536870912L / bytesToGb, domain.zramUsed)
        assertEquals(2147483648L / bytesToGb, domain.zramTotal)
        assertEquals(2.84f, domain.zramCompressionRatio)
    }

    @Test
    fun `should handle missing optional swap and zram fields`() {
        val rawJson = """
            {
                "type": "telemetry",
                "timestamp": 1716213825,
                "data": {
                    "cpu": {
                        "usage_percent": 18.7,
                        "temp_celsius": 49.0
                    },
                    "gpu": {
                        "usage_percent": 41.0,
                        "temp_celsius": 58.0,
                        "vram_used_bytes": 0,
                        "vram_total_bytes": 0
                    },
                    "ram": {
                        "used_bytes": 0,
                        "total_bytes": 0,
                        "percentage": 0.0
                    }
                }
            }
        """.trimIndent()

        val message = json.decodeFromString<ServerMessage>(rawJson) as TelemetryMessage
        val domain = message.toDomain()

        assertEquals(false, domain.swapSupported)
        assertEquals(false, domain.zramSupported)
        assertEquals(0f, domain.swapUsage)
        assertEquals(0f, domain.zramUsed)
    }
}
