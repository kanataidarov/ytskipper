package com.github.kanataidarov.ytskipper

import android.content.Context
import android.content.Intent
import androidx.core.content.edit
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.text.TextUtils
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.github.kanataidarov.ytskipper.service.ScreenClickerService
import com.github.kanataidarov.ytskipper.ui.theme.YTSkipperTheme
import androidx.core.net.toUri

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YTSkipperTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ScreenClickerScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun ScreenClickerScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val prefs = remember {
        context.getSharedPreferences(ScreenClickerService.PREFS_NAME, Context.MODE_PRIVATE)
    }

    var targetText by remember {
        mutableStateOf(
            prefs.getString(ScreenClickerService.KEY_TARGET_TEXT, ScreenClickerService.DEFAULT_TARGET_TEXT)
                ?: ScreenClickerService.DEFAULT_TARGET_TEXT
        )
    }

    var cooldownSeconds by remember {
        mutableFloatStateOf(
            prefs.getInt(ScreenClickerService.KEY_COOLDOWN_SECONDS, ScreenClickerService.DEFAULT_COOLDOWN_SECONDS).toFloat()
        )
    }

    var isAccessibilityEnabled by remember { mutableStateOf(false) }
    var isPaused by remember {
        mutableStateOf(prefs.getBoolean(ScreenClickerService.KEY_PAUSED, false))
    }
    var isBatteryOptimized by remember { mutableStateOf(true) }

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            isAccessibilityEnabled = isAccessibilityServiceEnabled(context)
            isPaused = prefs.getBoolean(ScreenClickerService.KEY_PAUSED, false)
            isBatteryOptimized = isBatteryOptimizationEnabled(context)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "YT Skipper",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Auto-tap on matching screen text",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        val isRunning = isAccessibilityEnabled && !isPaused

        Card(
            onClick = {
                if (!isAccessibilityEnabled) {
                    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                } else {
                    val newPaused = !isPaused
                    isPaused = newPaused
                    prefs.edit { putBoolean(ScreenClickerService.KEY_PAUSED, newPaused) }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isRunning)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = when {
                        !isAccessibilityEnabled -> "Service is INACTIVE"
                        isPaused -> "Service is PAUSED"
                        else -> "Service is ACTIVE"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isRunning)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when {
                        !isAccessibilityEnabled -> "Tap to enable in Accessibility Settings."
                        isPaused -> "Tap to resume."
                        else -> "Tap to pause."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isRunning)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = targetText,
            onValueChange = { newValue ->
                targetText = newValue
                prefs.edit { putString(ScreenClickerService.KEY_TARGET_TEXT, newValue) }
            },
            label = { Text("Target text (exact match)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Cooldown after click: ${cooldownSeconds.toInt()}s",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth()
        )

        Slider(
            value = cooldownSeconds,
            onValueChange = { cooldownSeconds = it },
            onValueChangeFinished = {
                prefs.edit { putInt(ScreenClickerService.KEY_COOLDOWN_SECONDS, cooldownSeconds.toInt()) }
            },
            valueRange = 0f..60f,
            steps = 59,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("0s", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("60s", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (isAccessibilityEnabled) "Open Accessibility Settings" else "Enable Service in Settings"
            )
        }

        if (isBatteryOptimized) {
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Battery Optimization Active",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "For best performance, disable battery optimization for this app.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    requestIgnoreBatteryOptimizations(context)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Disable Battery Optimization")
            }
        }
    }
}

private fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val expectedComponentName = "${context.packageName}/${ScreenClickerService::class.java.canonicalName}"
    val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false

    val colonSplitter = TextUtils.SimpleStringSplitter(':')
    colonSplitter.setString(enabledServices)
    while (colonSplitter.hasNext()) {
        val componentName = colonSplitter.next()
        if (componentName.equals(expectedComponentName, ignoreCase = true)) {
            return true
        }
    }
    return false
}

private fun isBatteryOptimizationEnabled(context: Context): Boolean {
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    return !powerManager.isIgnoringBatteryOptimizations(context.packageName)
}

@Suppress("BatteryLife")
private fun requestIgnoreBatteryOptimizations(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = "package:${context.packageName}".toUri()
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        // If direct request fails, open battery optimization settings
        try {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            context.startActivity(intent)
        } catch (_: Exception) {
            // Ignore if both fail
        }
    }
}
