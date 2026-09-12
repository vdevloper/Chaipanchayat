package com.chaipanchayat.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.chaipanchayat.app.data.repository.SettingsRepository
import com.chaipanchayat.app.data.repository.ThemeMode
import com.chaipanchayat.app.engine.liquidglass.LiquidGlassBackdrop
import com.chaipanchayat.app.ui.navigation.MainAppNavigation
import com.chaipanchayat.app.ui.theme.ChaiPanchayatTheme
import com.chaipanchayat.app.worker.ChaiNewsNotificationWorker

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Push notification permission handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create notification channel
        ChaiNewsNotificationWorker.createNotificationChannel(applicationContext)

        // Request POST_NOTIFICATIONS permission on Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Schedule periodic background API checks
        val settingsRepo = SettingsRepository.getInstance(applicationContext)
        if (settingsRepo.pushNotifications.value) {
            ChaiNewsNotificationWorker.schedulePeriodic(applicationContext)
        }

        // Check if opened via notification deep link
        val incomingArticleId = intent?.getLongExtra(ChaiNewsNotificationWorker.EXTRA_ARTICLE_ID, -1L)
            ?.takeIf { it > 0 }

        setContent {
            val themeMode by settingsRepo.themeMode.collectAsState()
            val glassSettings by settingsRepo.liquidGlassSettings.collectAsState()

            ChaiPanchayatTheme(
                themeMode = themeMode,
                liquidGlassSettings = glassSettings
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (themeMode == ThemeMode.LIQUID_GLASS) {
                        LiquidGlassBackdrop(settings = glassSettings)
                    }
                    MainAppNavigation(initialArticleId = incomingArticleId)
                }
            }
        }
    }
}
