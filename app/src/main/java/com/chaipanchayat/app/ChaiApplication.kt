package com.chaipanchayat.app

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.chaipanchayat.app.data.api.WordPressApiClient
import com.chaipanchayat.app.data.repository.NewsRepository
import java.io.File

class ChaiApplication : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
        // Initialize HTTP disk cache and connection pool safely
        runCatching {
            WordPressApiClient.init(this)
        }
        // Initialize local database storage caching safely
        runCatching {
            NewsRepository.init(this)
        }
    }

    override fun newImageLoader(): ImageLoader {
        return runCatching {
            ImageLoader.Builder(this)
                .memoryCache {
                    MemoryCache.Builder(this)
                        .maxSizePercent(0.25)
                        .build()
                }
                .diskCache {
                    DiskCache.Builder()
                        .directory(File(cacheDir, "image_cache"))
                        .maxSizeBytes(50L * 1024 * 1024) // 50 MB
                        .build()
                }
                .crossfade(true)
                .allowHardware(false)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .build()
        }.getOrElse {
            // Safe fallback image loader if disk cache directory access fails
            ImageLoader.Builder(this)
                .allowHardware(false)
                .crossfade(true)
                .build()
        }
    }
}
