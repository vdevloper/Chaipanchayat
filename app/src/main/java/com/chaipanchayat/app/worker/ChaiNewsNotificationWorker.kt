package com.chaipanchayat.app.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.chaipanchayat.app.MainActivity
import com.chaipanchayat.app.R
import com.chaipanchayat.app.data.api.WordPressApiClient
import com.chaipanchayat.app.data.repository.SettingsRepository
import java.util.concurrent.TimeUnit

/**
 * ChaiNewsNotificationWorker:
 * Periodically pings the Chai Panchayat WordPress REST API to check for fresh articles
 * and delivers high-priority Android notifications with instant deep linking.
 */
class ChaiNewsNotificationWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val settingsRepo = SettingsRepository.getInstance(appContext)

            // Check if notifications are enabled by the user in settings
            if (!settingsRepo.pushNotifications.value) {
                Log.d(TAG, "Push notifications disabled by user preference. Skipping.")
                return Result.success()
            }

            // Ensure WordPressApiClient is ready
            WordPressApiClient.init(appContext)

            // Fetch the latest 5 posts directly from the API
            val latestPosts = WordPressApiClient.fetchPosts(
                page = 1,
                perPage = 5,
                includeContent = false
            )

            if (latestPosts.isEmpty()) {
                Log.d(TAG, "No posts returned from API ping.")
                return Result.success()
            }

            val newestPost = latestPosts.first()
            val lastSeenId = settingsRepo.getLastSeenPostId()

            if (lastSeenId == 0L) {
                // First run: save the newest post ID without sending an alert
                settingsRepo.setLastSeenPostId(newestPost.id)
                Log.d(TAG, "First run initialization. Saved last seen post ID: ${newestPost.id}")
                return Result.success()
            }

            if (newestPost.id > lastSeenId) {
                Log.i(TAG, "New post detected: ${newestPost.id} - ${newestPost.cleanTitle}")
                sendPostNotification(appContext, newestPost.id, newestPost.cleanTitle, newestPost.primaryCategory)
                settingsRepo.setLastSeenPostId(newestPost.id)
            } else {
                Log.d(TAG, "No newer articles found (latest: ${newestPost.id}, saved: $lastSeenId).")
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking news updates in background worker", e)
            Result.retry()
        }
    }

    private fun sendPostNotification(
        context: Context,
        postId: Long,
        title: String,
        category: String?
    ) {
        createNotificationChannel(context)

        // Permission check on Android 13+ (Tiramisu, API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                Log.w(TAG, "POST_NOTIFICATIONS permission not granted. Cannot display notification.")
                return
            }
        }

        // Tap intent to open MainActivity with target article ID
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_ARTICLE_ID, postId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            postId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val categoryPrefix = if (!category.isNullOrBlank()) "[${category.uppercase()}] " else ""
        val fullContent = "$categoryPrefix$title"

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("☕ चाय पंचायत • ताज़ा खबर")
            .setContentText(fullContent)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(fullContent)
                    .setSummaryText("चाय पंचायत डिजिटल")
            )
            .setColor(Color.parseColor("#FF5A00")) // Chai Saffron
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(postId.toInt(), notificationBuilder.build())
            Log.d(TAG, "Notification delivered successfully for post $postId")
        } catch (se: SecurityException) {
            Log.e(TAG, "SecurityException while displaying notification", se)
        }
    }

    companion object {
        const val TAG = "ChaiNewsWorker"
        const val CHANNEL_ID = "chai_breaking_news_v2"
        const val CHANNEL_NAME = "चाय पंचायत ताज़ा खबरें"
        const val CHANNEL_DESC = "चाय पंचायत डिजिटल की ब्रेकिंग न्यूज़ व ताज़ा अपडेट्स"
        const val EXTRA_ARTICLE_ID = "article_id"
        private const val UNIQUE_PERIODIC_WORK_NAME = "chai_periodic_news_checker"
        private const val UNIQUE_ONE_TIME_WORK_NAME = "chai_immediate_news_checker"

        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val importance = NotificationManager.IMPORTANCE_HIGH
                val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                    description = CHANNEL_DESC
                    enableLights(true)
                    lightColor = Color.parseColor("#FF5A00")
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 200, 100, 200)
                }
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                notificationManager?.createNotificationChannel(channel)
            }
        }

        /**
         * Schedules the periodic background worker to ping the API every 15 minutes.
         */
        fun schedulePeriodic(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val periodicRequest = PeriodicWorkRequestBuilder<ChaiNewsNotificationWorker>(
                15, TimeUnit.MINUTES,
                5, TimeUnit.MINUTES // Flex interval
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicRequest
            )
            Log.i(TAG, "Scheduled periodic news update worker (15 min interval).")
        }

        /**
         * Cancels periodic background work if user disables push notifications.
         */
        fun cancelPeriodic(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_PERIODIC_WORK_NAME)
            Log.i(TAG, "Cancelled periodic news update worker.")
        }

        /**
         * Triggers an immediate one-time API check (e.g. from Settings or when refreshed).
         */
        fun triggerImmediateCheck(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val oneTimeRequest = OneTimeWorkRequestBuilder<ChaiNewsNotificationWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                UNIQUE_ONE_TIME_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                oneTimeRequest
            )
            Log.i(TAG, "Triggered immediate one-time news update check.")
        }

        fun checkOnceNow(context: Context) {
            triggerImmediateCheck(context)
        }
    }
}
