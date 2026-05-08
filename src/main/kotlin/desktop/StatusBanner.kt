package edu.teamcandy.desktop

import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun StatusBanner(message: String, isSuccess: Boolean, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    LaunchedEffect(message) {
        delay(3000)
        onDismiss()
    }
    Snackbar(
        modifier = modifier,
        backgroundColor = if (isSuccess) Color(0xFF388E3C) else MaterialTheme.colors.error,
        action = {
            TextButton(onClick = onDismiss) {
                Text("Dismiss", color = Color.White)
            }
        }
    ) {
        Text(message, color = Color.White)
    }
}