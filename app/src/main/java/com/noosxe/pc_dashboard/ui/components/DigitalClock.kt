package com.noosxe.pc_dashboard.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DigitalClock(modifier: Modifier = Modifier) {
    var time by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            time = System.currentTimeMillis()
            delay(1000)
        }
    }

    val formatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    Text(
        text = formatter.format(Date(time)),
        style = MaterialTheme.typography.titleLarge,
        modifier = modifier
    )
}
