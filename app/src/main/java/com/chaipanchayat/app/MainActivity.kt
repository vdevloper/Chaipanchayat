package com.chaipanchayat.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.chaipanchayat.app.data.repository.SettingsRepository
import com.chaipanchayat.app.data.repository.ThemeMode
import com.chaipanchayat.app.engine.liquidglass.LiquidGlassBackdrop
import com.chaipanchayat.app.ui.navigation.MainAppNavigation
import com.chaipanchayat.app.ui.theme.ChaiPanchayatTheme
import com.chaipanchayat.app.utils.DeepLinkManager
import com.chaipanchayat.app.worker.ChaiNewsNotificationWorker

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Push notification permission handled
    }

    private val targetArticleId = mutableStateOf<Long?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create notification channel safely
        runCatching {
            ChaiNewsNotificationWorker.createNotificationChannel(applicationContext)
        }

        // Schedule periodic background API checks safely
        val settingsRepo = SettingsRepository.getInstance(applicationContext)
        runCatching {
            if (settingsRepo.pushNotifications.value) {
                ChaiNewsNotificationWorker.schedulePeriodic(applicationContext)
            }
        }

        // 1. Check deep link from incoming launch intent or notification
        val directDeepLinkId = DeepLinkManager.extractArticleId(intent)
            ?: runCatching {
                intent?.getLongExtra(ChaiNewsNotificationWorker.EXTRA_ARTICLE_ID, -1L)?.takeIf { it > 0 }
            }.getOrNull()

        if (directDeepLinkId != null) {
            targetArticleId.value = directDeepLinkId
            DeepLinkManager.markHandled(applicationContext, directDeepLinkId)
        } else {
            // 2. Check deferred deep link (install referrer or clipboard on fresh install)
            val deferredId = DeepLinkManager.checkDeferredInstallDeepLink(applicationContext)
            if (deferredId != null) {
                targetArticleId.value = deferredId
            }
        }

        setContent {
            LaunchedEffect(Unit) {
                com.chaipanchayat.app.engine.liquidglass.ThemePrewarmer.warmUp()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    runCatching {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                        if (!hasPermission) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }
            }
            val themeMode by settingsRepo.themeMode.collectAsState()
            val glassSettings by settingsRepo.liquidGlassSettings.collectAsState()
            val currentTargetId by targetArticleId

            ChaiPanchayatTheme(
                themeMode = themeMode,
                liquidGlassSettings = glassSettings
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (themeMode == ThemeMode.LIQUID_GLASS) {
                        LiquidGlassBackdrop(settings = glassSettings)
                    }
                    MainAppNavigation(
                        initialArticleId = currentTargetId,
                        onArticleOpened = {
                            targetArticleId.value = null
                        }
                    )
                    com.chaipanchayat.app.engine.liquidglass.ThemeTransitionOverlay()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val newTargetId = DeepLinkManager.extractArticleId(intent)
            ?: runCatching {
                intent.getLongExtra(ChaiNewsNotificationWorker.EXTRA_ARTICLE_ID, -1L).takeIf { it > 0 }
            }.getOrNull()

        if (newTargetId != null) {
            targetArticleId.value = newTargetId
            DeepLinkManager.markHandled(applicationContext, newTargetId)
        }
    }
}

