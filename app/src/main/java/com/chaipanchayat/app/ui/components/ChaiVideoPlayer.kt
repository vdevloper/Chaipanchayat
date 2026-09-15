package com.chaipanchayat.app.ui.components

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.outlined.OndemandVideo
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.ScreenRotation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.chaipanchayat.app.ui.theme.ChaiCrimson
import com.chaipanchayat.app.ui.theme.ChaiSaffron
import com.chaipanchayat.app.ui.theme.InterFamily
import com.chaipanchayat.app.utils.rememberChaiHaptics

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ChaiVideoPlayer(
    youtubeId: String?,
    videoUrl: String? = null,
    title: String? = null,
    thumbnailUrl: String? = null,
    autoPlay: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptics = rememberChaiHaptics()
    val configuration = LocalConfiguration.current

    var isPlaying by rememberSaveable { mutableStateOf(autoPlay) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var isWebLoading by remember { mutableStateOf(false) }
    var isRenderCrashed by remember { mutableStateOf(false) }
    var isFullscreen by rememberSaveable { mutableStateOf(false) }

    val isDeviceLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val activity = remember(context) {
        var ctx = context
        while (ctx is ContextWrapper) {
            if (ctx is Activity) return@remember ctx
            ctx = ctx.baseContext
        }
        null
    }

    val cleanYtId = remember(youtubeId) {
        youtubeId?.trim()?.takeIf { it.isNotBlank() && !it.startsWith("mp4_") }
    }

    val displayThumb = remember(cleanYtId, thumbnailUrl) {
        when {
            !thumbnailUrl.isNullOrBlank() -> thumbnailUrl
            !cleanYtId.isNullOrBlank() -> "https://img.youtube.com/vi/$cleanYtId/hqdefault.jpg"
            else -> null
        }
    }

    fun toggleOrientation() {
        haptics.heavy()
        val act = activity ?: return
        val current = act.resources.configuration.orientation
        if (current == Configuration.ORIENTATION_LANDSCAPE) {
            act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }
    }

    fun enterFullscreen() {
        haptics.heavy()
        isPlaying = true
        isFullscreen = true
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
    }

    fun exitFullscreen() {
        haptics.heavy()
        isFullscreen = false
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    fun launchExternalVideo() {
        haptics.click()
        if (!cleanYtId.isNullOrBlank()) {
            try {
                val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$cleanYtId")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(appIntent)
            } catch (_: Exception) {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$cleanYtId")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(webIntent)
            }
        } else if (!videoUrl.isNullOrBlank()) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    // Reset orientation on disposal so app doesn't remain stuck in landscape
    DisposableEffect(Unit) {
        onDispose {
            try {
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                webViewRef?.stopLoading()
                webViewRef?.loadUrl("about:blank")
                webViewRef?.destroy()
            } catch (_: Exception) { }
            webViewRef = null
        }
    }

    // Fullscreen Dialog overlay
    if (isFullscreen) {
        Dialog(
            onDismissRequest = { exitFullscreen() },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            BackHandler {
                exitFullscreen()
            }

            var showControls by remember { mutableStateOf(true) }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        showControls = !showControls
                    }
            ) {
                // Video Content
                VideoSurface(
                    isPlaying = true,
                    cleanYtId = cleanYtId,
                    videoUrl = videoUrl,
                    isRenderCrashed = isRenderCrashed,
                    isWebLoading = isWebLoading,
                    displayThumb = displayThumb,
                    title = title,
                    onPlayClick = { },
                    onExternalClick = { launchExternalVideo() },
                    onWebViewCreated = { webViewRef = it },
                    onWebLoadingChanged = { isWebLoading = it },
                    onRenderCrashChanged = { isRenderCrashed = it },
                    modifier = Modifier.fillMaxSize()
                )

                // Top & Bottom Fullscreen HUD
                AnimatedVisibility(
                    visible = showControls,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Top bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Black.copy(alpha = 0.85f), Color.Transparent)
                                    )
                                )
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                IconButton(
                                    onClick = { exitFullscreen() },
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Exit Fullscreen",
                                        tint = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = title ?: "चाय पंचायत वीडियो",
                                    color = Color.White,
                                    fontFamily = InterFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Rotate Screen Mode Button
                                IconButton(
                                    onClick = { toggleOrientation() },
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ScreenRotation,
                                        contentDescription = "Rotate Screen",
                                        tint = ChaiSaffron
                                    )
                                }

                                // Open in External App
                                IconButton(
                                    onClick = { launchExternalVideo() },
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                                        contentDescription = "Open in YouTube App",
                                        tint = Color.White
                                    )
                                }

                                // Exit Fullscreen Button
                                IconButton(
                                    onClick = { exitFullscreen() },
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FullscreenExit,
                                        contentDescription = "Exit Fullscreen",
                                        tint = Color.White
                                    )
                                }
                            }
                        }

                        // Bottom status bar in Fullscreen
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                                    )
                                )
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isDeviceLandscape) "Landscape Mode • लैंडस्केप मोड" else "Portrait Fullscreen • पोर्ट्रेट फुलस्क्रीन",
                                color = Color(0xFFDDDDDD),
                                fontFamily = InterFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Button(
                                onClick = { exitFullscreen() },
                                colors = ButtonDefaults.buttonColors(containerColor = ChaiCrimson),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FullscreenExit,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "मिनीमाइज करें",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Standard In-Feed / Inline Surface
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.Black,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color.Black)
            ) {
                VideoSurface(
                    isPlaying = isPlaying,
                    cleanYtId = cleanYtId,
                    videoUrl = videoUrl,
                    isRenderCrashed = isRenderCrashed,
                    isWebLoading = isWebLoading,
                    displayThumb = displayThumb,
                    title = title,
                    onPlayClick = {
                        haptics.medium()
                        isRenderCrashed = false
                        isPlaying = true
                    },
                    onExternalClick = { launchExternalVideo() },
                    onWebViewCreated = { webViewRef = it },
                    onWebLoadingChanged = { isWebLoading = it },
                    onRenderCrashChanged = { isRenderCrashed = it },
                    modifier = Modifier.fillMaxSize()
                )

                // Inline quick action overlay: Fullscreen & Rotate floating buttons
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Rotate Mode Button
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.65f),
                        modifier = Modifier
                            .size(34.dp)
                            .clickable { toggleOrientation() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.ScreenRotation,
                                contentDescription = "Rotate Screen",
                                tint = if (isDeviceLandscape) ChaiSaffron else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Fullscreen Mode Button
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.65f),
                        modifier = Modifier
                            .size(34.dp)
                            .clickable { enterFullscreen() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Fullscreen,
                                contentDescription = "Fullscreen",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Bottom action strip
            Surface(
                color = Color(0xFF141414),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isPlaying && !isRenderCrashed) ChaiCrimson else Color.Gray)
                        )
                        Text(
                            text = if (isPlaying && !isRenderCrashed) "सक्रिय • Playing" else "चाय पंचायत वीडियो",
                            color = Color(0xFFCCCCCC),
                            fontFamily = InterFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Rotate Mode toggle button
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isDeviceLandscape) ChaiSaffron.copy(alpha = 0.2f) else Color(0xFF242424),
                            modifier = Modifier.clickable { toggleOrientation() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ScreenRotation,
                                    contentDescription = "Rotate",
                                    tint = if (isDeviceLandscape) ChaiSaffron else Color(0xFFCCCCCC),
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = if (isDeviceLandscape) "लैंडस्केप" else "रोटेट",
                                    color = if (isDeviceLandscape) ChaiSaffron else Color(0xFFCCCCCC),
                                    fontFamily = InterFamily,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Fullscreen Button
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF242424),
                            modifier = Modifier.clickable { enterFullscreen() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fullscreen,
                                    contentDescription = "Fullscreen",
                                    tint = Color(0xFFCCCCCC),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "फुलस्क्रीन",
                                    color = Color(0xFFCCCCCC),
                                    fontFamily = InterFamily,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        if (isPlaying && !isRenderCrashed) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF242424),
                                modifier = Modifier.clickable {
                                    haptics.click()
                                    webViewRef?.reload()
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Refresh,
                                        contentDescription = "Reload video",
                                        tint = Color(0xFFCCCCCC),
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                        }

                        // Open in External App
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = ChaiCrimson.copy(alpha = 0.2f),
                            modifier = Modifier.clickable { launchExternalVideo() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = if (!cleanYtId.isNullOrBlank()) "YouTube" else "वीडियो",
                                    color = Color(0xFFFF6B6B),
                                    fontFamily = InterFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                                    contentDescription = "Open in YouTube App",
                                    tint = Color(0xFFFF6B6B),
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun VideoSurface(
    isPlaying: Boolean,
    cleanYtId: String?,
    videoUrl: String?,
    isRenderCrashed: Boolean,
    isWebLoading: Boolean,
    displayThumb: String?,
    title: String?,
    onPlayClick: () -> Unit,
    onExternalClick: () -> Unit,
    onWebViewCreated: (WebView) -> Unit,
    onWebLoadingChanged: (Boolean) -> Unit,
    onRenderCrashChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(modifier = modifier) {
        if (isRenderCrashed) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF161616)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.OndemandVideo,
                        contentDescription = null,
                        tint = ChaiSaffron,
                        modifier = Modifier.size(34.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "वीडियो को सुरक्षित बाहरी प्लेयर में चलाएं",
                        color = Color.White,
                        fontFamily = InterFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onExternalClick,
                        colors = ButtonDefaults.buttonColors(containerColor = ChaiCrimson),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (!cleanYtId.isNullOrBlank()) "YouTube में खोलें" else "वीडियो चलाएं",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else if (isPlaying && (!cleanYtId.isNullOrBlank() || !videoUrl.isNullOrBlank())) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        setLayerType(View.LAYER_TYPE_SOFTWARE, null)

                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = false
                            databaseEnabled = false
                            mediaPlaybackRequiresUserGesture = false
                            cacheMode = WebSettings.LOAD_NO_CACHE
                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            allowFileAccess = false
                        }

                        webChromeClient = object : WebChromeClient() {}
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                onWebLoadingChanged(false)
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?
                            ) {
                                super.onReceivedError(view, request, error)
                                if (request?.isForMainFrame == true) {
                                    onWebLoadingChanged(false)
                                }
                            }

                            override fun onRenderProcessGone(
                                view: WebView?,
                                detail: RenderProcessGoneDetail?
                            ): Boolean {
                                onRenderCrashChanged(true)
                                onWebLoadingChanged(false)
                                try {
                                    (view?.parent as? ViewGroup)?.removeView(view)
                                    view?.destroy()
                                } catch (_: Exception) {}
                                return true
                            }

                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                val url = request?.url?.toString().orEmpty()
                                if (url.contains("youtube-nocookie.com") || url.contains("youtube.com/embed")) {
                                    return false
                                }
                                return try {
                                    val intent = Intent(Intent.ACTION_VIEW, request?.url).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    ctx.startActivity(intent)
                                    true
                                } catch (_: Exception) {
                                    false
                                }
                            }
                        }

                        onWebLoadingChanged(true)
                        if (!cleanYtId.isNullOrBlank()) {
                            val embedHtml = """
                                <!DOCTYPE html>
                                <html>
                                <head>
                                <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
                                <style>
                                    * { margin:0; padding:0; box-sizing:border-box; }
                                    body { background:#000; overflow:hidden; width:100vw; height:100vh; display:flex; align-items:center; justify-content:center; }
                                    iframe { width:100%; height:100%; border:none; }
                                </style>
                                </head>
                                <body>
                                <iframe 
                                    src="https://www.youtube-nocookie.com/embed/$cleanYtId?autoplay=1&playsinline=1&enablejsapi=1&rel=0&modestbranding=1" 
                                    allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" 
                                    allowfullscreen>
                                </iframe>
                                </body>
                                </html>
                            """.trimIndent()
                            loadDataWithBaseURL("https://www.youtube-nocookie.com", embedHtml, "text/html", "UTF-8", null)
                        } else if (!videoUrl.isNullOrBlank()) {
                            val videoHtml = """
                                <!DOCTYPE html>
                                <html>
                                <head>
                                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                <style>
                                    * { margin:0; padding:0; }
                                    body { background:#000; overflow:hidden; width:100vw; height:100vh; }
                                    video { width:100%; height:100%; object-fit:contain; }
                                </style>
                                </head>
                                <body>
                                <video controls autoplay playsinline src="$videoUrl"></video>
                                </body>
                                </html>
                            """.trimIndent()
                            loadDataWithBaseURL("https://chaipanchayat.com", videoHtml, "text/html", "UTF-8", null)
                        }
                        onWebViewCreated(this)
                    }
                }
            )

            if (isWebLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = ChaiSaffron,
                        modifier = Modifier.size(36.dp),
                        strokeWidth = 3.dp
                    )
                }
            }
        } else {
            // Thumbnail Preview
            if (!displayThumb.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(displayThumb)
                        .crossfade(true)
                        .build(),
                    contentDescription = title ?: "Video thumbnail",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.25f), Color.Black.copy(alpha = 0.65f))
                        )
                    )
            )

            Surface(
                shape = CircleShape,
                color = ChaiCrimson,
                modifier = Modifier
                    .size(56.dp)
                    .align(Alignment.Center)
                    .clickable { onPlayClick() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play Video",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.OndemandVideo,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "वीडियो देखने के लिए टैप करें",
                        color = Color.White,
                        fontFamily = InterFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
