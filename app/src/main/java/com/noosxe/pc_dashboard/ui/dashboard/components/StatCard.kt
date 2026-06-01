package com.noosxe.pc_dashboard.ui.dashboard.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StatCard(title: String, usage: Float, temp: Float, freq: Float, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(text = "Usage: ${"%.1f".format(usage)}%")
            Text(text = "Temp: ${"%.1f".format(temp)}°C")
            Text(text = "Freq: ${"%.2f".format(freq / 1000f)} GHz")
        }
    }
}
