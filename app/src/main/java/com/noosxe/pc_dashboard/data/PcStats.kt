package com.noosxe.pc_dashboard.data

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
