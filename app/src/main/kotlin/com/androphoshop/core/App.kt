package com.androphoshop.core

import android.app.Application
import com.androphoshop.core.utils.ImageCache
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import com.androphoshop.BuildConfig

@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Initialize other app components
        initializeApp()
    }

    private fun initializeApp() {
        Timber.d("AndroPhoshop Application Initialized")

        // Initialize image cache
        ImageCache.initialize(this)

        // Create notification channels
        createNotificationChannels()

        // Initialize AI models in background
        initializeAIModels()
    }

    private fun createNotificationChannels() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                "ai_channel",
                "AI Processing",
                android.app.NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Channel for AI processing notifications"
            }
            val notificationManager = getSystemService(android.app.NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun initializeAIModels() {
        // Background initialization of AI models
        /* CoroutineScope(Dispatchers.IO).launch {
            try {
                // val aiEngine: AIEnhancementEngine by inject()
                // aiEngine.preloadModels()
                Timber.d("AI Models loaded successfully")
            } catch (e: Exception) {
                Timber.e(e, "Failed to load AI models")
            }
        } */
    }
}