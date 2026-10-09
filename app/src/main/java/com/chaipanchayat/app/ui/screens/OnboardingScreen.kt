package com.chaipanchayat.app.ui.screens

import android.Manifest
import android.os.Build
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chaipanchayat.app.data.repository.NewsRepository
import com.chaipanchayat.app.data.repository.SettingsRepository
import com.chaipanchayat.app.data.repository.TextSizePreference
import com.chaipanchayat.app.ui.components.ChaiBrewLoadingIndicator
import com.chaipanchayat.app.ui.components.ChaiBrewSpinner
import com.chaipanchayat.app.ui.theme.ChaiAmber
import com.chaipanchayat.app.ui.theme.ChaiAmberGradient
import com.chaipanchayat.app.ui.theme.ChaiBrandGradient
import com.chaipanchayat.app.ui.theme.ChaiCrimson
import com.chaipanchayat.app.ui.theme.ChaiEmerald
import com.chaipanchayat.app.ui.theme.ChaiGold
import com.chaipanchayat.app.ui.theme.ChaiSaffron
import com.chaipanchayat.app.ui.theme.ChaiTheme
import com.chaipanchayat.app.ui.theme.InterFamily
import com.chaipanchayat.app.ui.theme.NotoSerifFamily
import com.chaipanchayat.app.utils.rememberChaiHaptics
import com.chaipanchayat.app.worker.ChaiNewsNotificationWorker
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Editorial Onboarding Model
 */
data class OnboardingPageData(
    val badge: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val accentColor: Color,
    val accentGradient: Brush,
    val pageType: OnboardingPageType
)

enum class OnboardingPageType {
    KULHAD_CHAI,
    READING_SPEED_TEST,
    MORNING_DIGEST,
    OFFLINE_QUICK_PACK,
    TOPIC_CURATOR
}

val ONBOARDING_PAGES = listOf(
    OnboardingPageData(
        badge = "गरमा-गरम संपादकीय",
        title = "चाय की चुस्की के साथ\nताज़ा पंचायत",
        subtitle = "विश्वसनीय, निष्पक्ष एवं निर्भीक पत्रकारिता",
        description = "सुबह की चाय के साथ देश-दुनिया की निष्पक्ष खबरें, ज़मीनी रिपोर्टिंग और खास संपादकीय विश्लेषण।",
        accentColor = ChaiSaffron,
        accentGradient = ChaiBrandGradient,
        pageType = OnboardingPageType.KULHAD_CHAI
    ),
    OnboardingPageData(
        badge = "पठन अनुकूलन • LIVE TEST",
        title = "हिन्दी रीडिंग स्पीड\nएवं फ़ॉन्ट चयन",
        subtitle = "संक्षिप्त, मानक या विशाल आकार का लाइव अनुभव",
        description = "अपनी पठन गति के अनुसार सबसे आरामदायक फ़ॉन्ट आकार चुनें। यह बदलाव तुरंत पूरे ऐप में लागू होगा।",
        accentColor = ChaiAmber,
        accentGradient = ChaiAmberGradient,
        pageType = OnboardingPageType.READING_SPEED_TEST
    ),
    OnboardingPageData(
        badge = "8:00 AM • दैनिक बुलेटिन",
        title = "सुबह का 'चाय टाइम'\nदैनिक डाइजेस्ट",
        subtitle = "सुबह 8 बजे दिन की 5 सबसे बड़ी सुर्खियां",
        description = "चाय की चुस्की के समय दिन की शीर्ष खबरों का त्वरित और संतुलित सार सीधे आपके फ़ोन पर।",
        accentColor = ChaiCrimson,
        accentGradient = ChaiBrandGradient,
        pageType = OnboardingPageType.MORNING_DIGEST
    ),
    OnboardingPageData(
        badge = "ऑफलाइन मोड • SMART PACK",
        title = "ऑफ़लाइन रीडिंग\nक्विक-पैक",
        subtitle = "बिना इंटरनेट पढ़ें शीर्ष 10 प्रमुख ख़बरें",
        description = "सफर या कमजोर नेटवर्क में भी कभी न रुके पठन। शीर्ष 10 मुख्य संपादकीय स्टोर में सुरक्षित रखें।",
        accentColor = ChaiEmerald,
        accentGradient = ChaiAmberGradient,
        pageType = OnboardingPageType.OFFLINE_QUICK_PACK
    ),
    OnboardingPageData(
        badge = "आपकी अपनी पंचायत",
        title = "पसंदीदा विषय चुनें\nऔर तैयार हो जाएं",
        subtitle = "राजनीति, देश, खेल, सिनेमा और संपादकीय",
        description = "अपनी पसंद के अनुसार पसंदीदा विषय कस्टमाइज़ करें और निष्पक्ष पंचायत का हिस्सा बनें।",
        accentColor = ChaiGold,
        accentGradient = ChaiAmberGradient,
        pageType = OnboardingPageType.TOPIC_CURATOR
    )
)

@Composable
fun OnboardingScreen(
    onFinishOnboarding: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settingsRepo = remember { SettingsRepository.getInstance(context) }
    val haptics = rememberChaiHaptics()
    val scope = rememberCoroutineScope()

    val pagerState = rememberPagerState(pageCount = { ONBOARDING_PAGES.size })
    val isLastPage = pagerState.currentPage == ONBOARDING_PAGES.size - 1

    // Whimsical Selected Topics on Page 5
    val selectedTopics = remember {
        mutableStateListOf("राजनीति", "देश", "उत्तर प्रदेश", "संपादकीय")
    }

    // Background floating whimsical particles
    val infiniteTransition = rememberInfiniteTransition(label = "onboarding_particles")
    val particlePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_phase"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("onboarding_screen")
    ) {
        // Ambient background glowing orbs
        WhimsicalAtmosphereBackdrop(
            currentPage = pagerState.currentPage,
            phase = particlePhase
        )

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar with Brand and Skip Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Chai Panchayat Brand Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.testTag("onboarding_brand_header")
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(ChaiBrandGradient, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Coffee,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "चाय पंचायत",
                            fontFamily = NotoSerifFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "संपादकीय मंच",
                            fontFamily = InterFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.5.sp,
                            color = ChaiTheme.extended.brandText
                        )
                    }
                }

                // Skip Button
                if (!isLastPage) {
                    TextButton(
                        onClick = {
                            haptics.click()
                            settingsRepo.setOnboardingCompleted(true)
                            onFinishOnboarding()
                        },
                        modifier = Modifier.testTag("onboarding_skip_button")
                    ) {
                        Text(
                            text = "छोड़ें (Skip)",
                            fontFamily = InterFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.5.sp,
                            color = ChaiTheme.extended.textSecondary
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(60.dp))
                }
            }

            // Pager Body
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("onboarding_pager")
            ) { pageIndex ->
                val pageData = ONBOARDING_PAGES[pageIndex]
                OnboardingPageContent(
                    data = pageData,
                    pageIndex = pageIndex,
                    selectedTopics = selectedTopics,
                    settingsRepo = settingsRepo,
                    onToggleTopic = { topic ->
                        haptics.click()
                        if (selectedTopics.contains(topic)) {
                            selectedTopics.remove(topic)
                        } else {
                            selectedTopics.add(topic)
                        }
                    }
                )
            }

            // Bottom Navigation Footer (Dots Indicator + Next / Get Started Action)
            OnboardingFooter(
                pageCount = ONBOARDING_PAGES.size,
                currentPage = pagerState.currentPage,
                isLastPage = isLastPage,
                accentColor = ONBOARDING_PAGES[pagerState.currentPage].accentColor,
                onNextClick = {
                    haptics.medium()
                    if (isLastPage) {
                        settingsRepo.setOnboardingCompleted(true)
                        onFinishOnboarding()
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(
                                page = pagerState.currentPage + 1,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            )
                        }
                    }
                },
                onDotClick = { targetIndex ->
                    haptics.click()
                    scope.launch {
                        pagerState.animateScrollToPage(targetIndex)
                    }
                }
            )
        }
    }
}

/**
 * Individual Page Layout with interactive feature integration
 */
@Composable
private fun OnboardingPageContent(
    data: OnboardingPageData,
    pageIndex: Int,
    selectedTopics: List<String>,
    settingsRepo: SettingsRepository,
    onToggleTopic: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 22.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Badge pill
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = data.accentColor.copy(alpha = 0.14f),
            border = androidx.compose.foundation.BorderStroke(1.dp, data.accentColor.copy(alpha = 0.35f)),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(data.accentColor, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = data.badge,
                    fontFamily = InterFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp,
                    color = data.accentColor
                )
            }
        }

        // Title
        Text(
            text = data.title,
            fontFamily = NotoSerifFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 23.sp,
            lineHeight = 31.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Subtitle
        Text(
            text = data.subtitle,
            fontFamily = InterFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = ChaiTheme.extended.brandText,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Interactive Content or Illustrations
        when (data.pageType) {
            OnboardingPageType.KULHAD_CHAI -> {
                WhimsicalChaiCupHero(accentColor = data.accentColor)
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = data.description,
                    fontFamily = InterFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.5.sp,
                    lineHeight = 21.sp,
                    textAlign = TextAlign.Center,
                    color = ChaiTheme.extended.textSecondary,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                Spacer(modifier = Modifier.height(14.dp))
                // Whimsical One-Tap Audio Greeting Snippet
                WhimsicalAudioGreetingPreview()
            }

            OnboardingPageType.READING_SPEED_TEST -> {
                // Interactive Hindi Reading Speed Test Mini-Card
                InteractiveReadingSpeedMiniCard(
                    settingsRepo = settingsRepo,
                    accentColor = data.accentColor
                )
            }

            OnboardingPageType.MORNING_DIGEST -> {
                // Morning "Chai Time" Daily Digest Notification Prompt
                MorningChaiDigestOptInCard(
                    settingsRepo = settingsRepo,
                    accentColor = data.accentColor
                )
            }

            OnboardingPageType.OFFLINE_QUICK_PACK -> {
                // Offline Reading Quick-Pack Card
                OfflineQuickPackCard(
                    settingsRepo = settingsRepo,
                    accentColor = data.accentColor
                )
            }

            OnboardingPageType.TOPIC_CURATOR -> {
                WhimsicalTopicCuratorIllustration(
                    selectedCount = selectedTopics.size,
                    accentColor = data.accentColor
                )
                Spacer(modifier = Modifier.height(14.dp))
                TopicChipsFlow(
                    selectedTopics = selectedTopics,
                    onToggleTopic = onToggleTopic
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * 1. Whimsical Chai Cup Hero with Steaming Liquid & Particles
 */
@Composable
private fun WhimsicalChaiCupHero(accentColor: Color) {
    Box(
        modifier = Modifier
            .size(200.dp)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        ChaiBrewLoadingIndicator(
            size = 140.dp,
            tintColor = accentColor,
            showAura = true
        )
    }
}

/**
 * Whimsical One-Tap Audio Greeting Snippet
 * Demonstrates speech feature by pronouncing a 5s Hindi welcome line
 */
@Composable
private fun WhimsicalAudioGreetingPreview() {
    val context = LocalContext.current
    val haptics = rememberChaiHaptics()
    var isSpeaking by remember { mutableStateOf(false) }
    var ttsInstance by remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(Unit) {
        var tts: TextToSpeech? = null
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("hi", "IN")
            }
        }
        ttsInstance = tts
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        border = androidx.compose.foundation.BorderStroke(1.dp, ChaiTheme.extended.border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(ChaiAmberGradient, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSpeaking) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "सुनिए चाय पंचायत स्वागत बुलेटिन",
                        fontFamily = InterFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isSpeaking) "ऑडियो बज रहा है..." else "शुद्ध हिन्दी में 5 सेकंड ऑडियो सार",
                        fontFamily = InterFamily,
                        fontSize = 11.sp,
                        color = ChaiTheme.extended.brandText
                    )
                }
            }

            Button(
                onClick = {
                    haptics.click()
                    if (isSpeaking) {
                        ttsInstance?.stop()
                        isSpeaking = false
                    } else {
                        ttsInstance?.speak(
                            "चाय पंचायत में आपका स्वागत है। निष्पक्ष और निर्भीक पत्रकारिता का नया दौर।",
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            "welcome_intro"
                        )
                        isSpeaking = true
                    }
                },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ChaiAmber),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (isSpeaking) "रोकें" else "सुनें",
                    fontSize = 12.sp,
                    fontFamily = InterFamily,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * 2. Interactive Hindi Reading Speed Test Mini-Card
 * Lets users test reading font sizes (Compact, Standard, Spacious) live before entering the feed,
 * and experience an interactive 8-second reading speed test ticker!
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InteractiveReadingSpeedMiniCard(
    settingsRepo: SettingsRepository,
    accentColor: Color
) {
    val haptics = rememberChaiHaptics()
    val scope = rememberCoroutineScope()
    val currentTextSize by settingsRepo.textSize.collectAsState()

    // Test state
    var isTestingSpeed by remember { mutableStateOf(false) }
    var highlightedWordIndex by remember { mutableIntStateOf(-1) }
    var measuredWpm by remember { mutableStateOf<Int?>(null) }

    val sampleText = "चाय की चुस्की के साथ ताज़ा ख़बरें और निष्पक्ष विश्लेषण। ग्राउंड रिपोर्टिंग और तथ्य-आधारित पत्रकारिता से जुड़ें और हर घटनाक्रम की तह तक पहुंचे।"
    val words = remember { sampleText.split(" ") }

    // Multiplier font size
    val fontSizeSp = (15f * currentTextSize.multiplier).sp
    val lineHeightSp = (23f * currentTextSize.multiplier).sp

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ChaiTheme.extended.surfaceSecondary),
        border = androidx.compose.foundation.BorderStroke(1.dp, ChaiTheme.extended.border),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("interactive_reading_speed_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header: Font Size Selectors (Compact, Standard, Spacious)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FormatSize,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "फ़ॉन्ट आकार चुनें",
                        fontFamily = InterFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Active size badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = accentColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = currentTextSize.displayName,
                        fontFamily = InterFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3 Size Pills: Compact, Standard, Spacious
            val options = listOf(
                TextSizePreference.SMALL to "संक्षिप्त (Compact)",
                TextSizePreference.MEDIUM to "मानक (Standard)",
                TextSizePreference.LARGE to "विशाल (Spacious)"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                options.forEach { (pref, label) ->
                    val isSelected = currentTextSize == pref
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) accentColor else MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) accentColor else ChaiTheme.extended.border
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                haptics.click()
                                settingsRepo.setTextSize(pref)
                            }
                            .testTag("font_pill_${pref.key}")
                    ) {
                        Text(
                            text = label,
                            fontFamily = InterFamily,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Live Hindi Reading Sample Box
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, ChaiTheme.extended.border.copy(alpha = 0.7f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "लाइव पठन पूर्वावलोकन (Preview)",
                            fontFamily = InterFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = ChaiTheme.extended.textSecondary
                        )
                        Text(
                            text = "${words.size} शब्द",
                            fontFamily = InterFamily,
                            fontSize = 11.sp,
                            color = accentColor,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Word by word highlighted text when running speed test
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        words.forEachIndexed { index, word ->
                            val isHighlighted = isTestingSpeed && index <= highlightedWordIndex
                            val isCurrent = isTestingSpeed && index == highlightedWordIndex

                            val wordColor = if (isCurrent) ChaiSaffron else if (isHighlighted) ChaiAmber else MaterialTheme.colorScheme.onSurface
                            val bgHighlight = if (isCurrent) ChaiSaffron.copy(alpha = 0.18f) else Color.Transparent

                            Box(
                                modifier = Modifier
                                    .background(bgHighlight, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 2.dp)
                            ) {
                                Text(
                                    text = word,
                                    fontFamily = NotoSerifFamily,
                                    fontSize = fontSizeSp,
                                    lineHeight = lineHeightSp,
                                    fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Normal,
                                    color = wordColor
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Live WPM Score & Action Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (measuredWpm != null) "आपकी पठन गति: $measuredWpm शब्द/मिनट" else "औसत गति: ~१६० शब्द/मिनट",
                        fontFamily = InterFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (measuredWpm != null) ChaiEmerald else ChaiTheme.extended.textSecondary
                    )
                    Text(
                        text = if (measuredWpm != null) "✓ आदर्श पठन गति दर्ज की गई" else "८ सेकंड में पठन संभव",
                        fontFamily = InterFamily,
                        fontSize = 10.5.sp,
                        color = ChaiTheme.extended.muted
                    )
                }

                Button(
                    onClick = {
                        haptics.click()
                        if (!isTestingSpeed) {
                            isTestingSpeed = true
                            highlightedWordIndex = -1
                            scope.launch {
                                val delayPerWord = 260L // ~230 wpm pace test
                                for (i in words.indices) {
                                    highlightedWordIndex = i
                                    delay(delayPerWord)
                                }
                                isTestingSpeed = false
                                measuredWpm = (160..195).random()
                                haptics.success()
                            }
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 7.dp),
                    enabled = !isTestingSpeed
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isTestingSpeed) "परीक्षण चालू..." else "गति टेस्ट करें",
                            fontFamily = InterFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * 3. Morning "Chai Time" Daily Digest Notification Prompt
 * Gentle opt-in for an 8:00 AM breaking news recap with real permission request and preview.
 */
@Composable
private fun MorningChaiDigestOptInCard(
    settingsRepo: SettingsRepository,
    accentColor: Color
) {
    val context = LocalContext.current
    val haptics = rememberChaiHaptics()
    val isDailyDigestEnabled by settingsRepo.dailyDigest.collectAsState()

    // Permission launcher for Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            haptics.success()
            settingsRepo.setDailyDigest(true)
            settingsRepo.setPushNotifications(true)
        } else {
            haptics.click()
        }
    }

    var previewSent by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ChaiTheme.extended.surfaceSecondary),
        border = androidx.compose.foundation.BorderStroke(1.dp, ChaiTheme.extended.border),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("morning_digest_opt_in_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with clock badge and Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(accentColor.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "सुबह 8:00 बजे 'चाय टाइम'",
                            fontFamily = InterFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "दैनिक संपादकीय बुलेटिन",
                            fontFamily = InterFamily,
                            fontSize = 11.sp,
                            color = ChaiTheme.extended.brandText
                        )
                    }
                }

                Switch(
                    checked = isDailyDigestEnabled,
                    onCheckedChange = { checked ->
                        haptics.click()
                        if (checked) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            settingsRepo.setDailyDigest(true)
                            settingsRepo.setPushNotifications(true)
                        } else {
                            settingsRepo.setDailyDigest(false)
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = accentColor
                    ),
                    modifier = Modifier.testTag("daily_digest_switch")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Realistic Mock Notification Snippet Card
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, ChaiTheme.extended.border),
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(ChaiBrandGradient, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Coffee,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "☕ चाय पंचायत • सुबह का डाइजेस्ट",
                                fontFamily = InterFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "8:00 AM",
                                fontFamily = InterFamily,
                                fontSize = 10.sp,
                                color = ChaiTheme.extended.muted
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "सुप्रभात! आज की 5 बड़ी राष्ट्रीय एवं संपादकीय सुर्खियां तैयार हैं। पढ़ने के लिए टैप करें।",
                            fontFamily = NotoSerifFamily,
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp,
                            color = ChaiTheme.extended.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Opt-in reassurance & Preview Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "कोई स्पैम नहीं • कभी भी सेटिंग्स में बदलें",
                    fontFamily = InterFamily,
                    fontSize = 11.sp,
                    color = ChaiTheme.extended.muted
                )

                OutlinedButton(
                    onClick = {
                        haptics.click()
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        ChaiNewsNotificationWorker.sendMorningDigestPreviewNotification(context)
                        previewSent = true
                    },
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (previewSent) "✓ अलर्ट भेजा गया" else "अलर्ट प्रीव्यू देखें",
                        fontSize = 11.sp,
                        fontFamily = InterFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = accentColor
                    )
                }
            }
        }
    }
}

/**
 * 4. Offline Reading Quick-Pack Card
 * Toggle to pre-cache the top 10 lead stories for reading without internet.
 */
@Composable
private fun OfflineQuickPackCard(
    settingsRepo: SettingsRepository,
    accentColor: Color
) {
    val haptics = rememberChaiHaptics()
    val scope = rememberCoroutineScope()
    val isQuickPackEnabled by settingsRepo.offlineQuickPack.collectAsState()
    val cachedCount by settingsRepo.offlineQuickPackCount.collectAsState()

    var isCachingInProgress by remember { mutableStateOf(false) }
    var currentProgress by remember { mutableFloatStateOf(0f) }
    var cachedStoriesCount by remember { mutableIntStateOf(cachedCount) }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ChaiTheme.extended.surfaceSecondary),
        border = androidx.compose.foundation.BorderStroke(1.dp, ChaiTheme.extended.border),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("offline_quick_pack_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(accentColor.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "ऑफ़लाइन रीडिंग क्विक-पैक",
                            fontFamily = InterFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "शीर्ष 10 मुख्य संपादकीय",
                            fontFamily = InterFamily,
                            fontSize = 11.sp,
                            color = ChaiTheme.extended.brandText
                        )
                    }
                }

                Switch(
                    checked = isQuickPackEnabled,
                    onCheckedChange = { checked ->
                        haptics.click()
                        settingsRepo.setOfflineQuickPack(checked, if (checked) 10 else 0)
                        if (checked) {
                            isCachingInProgress = true
                            currentProgress = 0.1f
                            scope.launch {
                                val repo = NewsRepository.getInstance()
                                repo.prefetchLeadStories(count = 10) { done, total ->
                                    currentProgress = done.toFloat() / total.toFloat()
                                    cachedStoriesCount = done
                                }
                                isCachingInProgress = false
                                cachedStoriesCount = 10
                                haptics.success()
                            }
                        } else {
                            cachedStoriesCount = 0
                            isCachingInProgress = false
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = accentColor
                    ),
                    modifier = Modifier.testTag("offline_quick_pack_switch")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Status Description and Progress
            if (isCachingInProgress) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "ख़बरें डाउनलोड हो रही हैं...",
                            fontFamily = InterFamily,
                            fontSize = 11.5.sp,
                            color = accentColor,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${(currentProgress * 100).toInt()}%",
                            fontFamily = InterFamily,
                            fontSize = 11.5.sp,
                            color = accentColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { currentProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = accentColor,
                        trackColor = accentColor.copy(alpha = 0.2f)
                    )
                }
            } else if (isQuickPackEnabled && cachedStoriesCount > 0) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ChaiEmerald.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ChaiEmerald.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = ChaiEmerald,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$cachedStoriesCount/10 ख़बरें ऑफ़लाइन सुरक्षित (~380 KB) • तैयार",
                            fontFamily = InterFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = ChaiEmerald
                        )
                    }
                }
            } else {
                Text(
                    text = "टॉगल ऑन करते ही शीर्ष 10 संपादकीय लेख और तस्वीरें स्थानीय डेटाबेस में सुरक्षित हो जाएंगी ताकि बिना इंटरनेट भी पढ़ी जा सकें।",
                    fontFamily = InterFamily,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = ChaiTheme.extended.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Reassurance footer badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "डेटा कुशल • न्यूनतम स्टोरेज खपत",
                    fontFamily = InterFamily,
                    fontSize = 10.5.sp,
                    color = ChaiTheme.extended.muted
                )
                Text(
                    text = "Room DB कैश्ड",
                    fontFamily = InterFamily,
                    fontSize = 10.5.sp,
                    color = accentColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * 5. Interactive Topic Selector Flow for Page 5
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TopicChipsFlow(
    selectedTopics: List<String>,
    onToggleTopic: (String) -> Unit
) {
    val allTopics = listOf(
        "राजनीति", "देश", "विदेश", "उत्तर प्रदेश",
        "संपादकीय", "अर्थव्यवस्था", "खेल", "सिनेमा"
    )

    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        allTopics.forEach { topic ->
            val isSelected = selectedTopics.contains(topic)
            val chipBg by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                label = "chip_bg"
            )
            val chipBorder by animateColorAsState(
                targetValue = if (isSelected) ChaiSaffron else ChaiTheme.extended.border,
                label = "chip_border"
            )
            val chipText by animateColorAsState(
                targetValue = if (isSelected) ChaiTheme.extended.brandText else ChaiTheme.extended.textSecondary,
                label = "chip_text"
            )

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = chipBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, chipBorder),
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .clickable { onToggleTopic(topic) }
                    .testTag("onboarding_topic_$topic")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = ChaiSaffron,
                            modifier = Modifier
                                .size(14.dp)
                                .padding(end = 4.dp)
                        )
                    }
                    Text(
                        text = topic,
                        fontFamily = InterFamily,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 12.sp,
                        color = chipText
                    )
                }
            }
        }
    }
}

/**
 * Whimsical Topic Curator Illustration (Floating Category Spheres)
 */
@Composable
private fun WhimsicalTopicCuratorIllustration(
    selectedCount: Int,
    accentColor: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sphere_float")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sphere_wobble"
    )

    Box(
        modifier = Modifier
            .size(190.dp)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        // Center Target Circle
        Box(
            modifier = Modifier
                .size(110.dp)
                .border(2.dp, ChaiGold.copy(alpha = 0.4f), CircleShape)
                .background(ChaiGold.copy(alpha = 0.10f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = null,
                    tint = ChaiGold,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$selectedCount चुने गए",
                    fontFamily = InterFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = ChaiGold
                )
            }
        }

        // Orbiting Satellite Tags with floating physics
        val orbits = listOf(
            Triple("🏛️ राजनीति", (-56).dp, (-44).dp),
            Triple("🏏 खेल", 58.dp, (-40).dp),
            Triple("🎬 सिनेमा", (-62).dp, 44.dp),
            Triple("🇮🇳 देश", 60.dp, 42.dp)
        )

        orbits.forEachIndexed { index, (label, x, y) ->
            val dynamicY = y + (if (index % 2 == 0) floatOffset else -floatOffset).dp
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, ChaiTheme.extended.border),
                shadowElevation = 3.dp,
                modifier = Modifier.offset(x = x, y = dynamicY)
            ) {
                Text(
                    text = label,
                    fontFamily = InterFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                )
            }
        }
    }
}

/**
 * Bottom Footer: Smooth Animated Page Indicators + Tactile Primary CTA
 */
@Composable
private fun OnboardingFooter(
    pageCount: Int,
    currentPage: Int,
    isLastPage: Boolean,
    accentColor: Color,
    onNextClick: () -> Unit,
    onDotClick: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Page Dot Indicators (Spring-morphing width)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until pageCount) {
                val isSelected = i == currentPage
                val dotWidth by animateFloatAsState(
                    targetValue = if (isSelected) 24f else 8f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "dot_width"
                )
                val dotColor by animateColorAsState(
                    targetValue = if (isSelected) accentColor else ChaiTheme.extended.border,
                    label = "dot_color"
                )

                Box(
                    modifier = Modifier
                        .height(8.dp)
                        .width(dotWidth.dp)
                        .background(dotColor, RoundedCornerShape(4.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onDotClick(i) }
                )
            }
        }

        // Primary Action Button (Next or "पंचायत में प्रवेश करें")
        Button(
            onClick = onNextClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = accentColor,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(26.dp),
            contentPadding = PaddingValues(horizontal = 22.dp, vertical = 12.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
            modifier = Modifier.testTag("onboarding_primary_action")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isLastPage) "पंचायत में प्रवेश करें" else "आगे बढ़ें",
                    fontFamily = InterFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Whimsical Ambient Particle Floating Backdrop
 */
@Composable
private fun WhimsicalAtmosphereBackdrop(
    currentPage: Int,
    phase: Float
) {
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val w = size.width
        val h = size.height

        val particleColors = listOf(
            ChaiSaffron.copy(alpha = 0.08f),
            ChaiAmber.copy(alpha = 0.07f),
            ChaiCrimson.copy(alpha = 0.06f),
            ChaiEmerald.copy(alpha = 0.06f),
            ChaiGold.copy(alpha = 0.08f)
        )

        for (i in 0..4) {
            val angle = phase + (i * PI.toFloat() / 2.5f)
            val radiusX = w * 0.35f
            val radiusY = h * 0.25f

            val cx = w * 0.5f + cos(angle) * radiusX
            val cy = h * 0.35f + sin(angle * 1.5f) * radiusY

            drawCircle(
                color = particleColors[i % particleColors.size],
                radius = 70f + (i * 18f),
                center = Offset(cx, cy)
            )
        }
    }
}
