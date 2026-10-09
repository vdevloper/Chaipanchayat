package com.chaipanchayat.app.ui.screens

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
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.draw.blur
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chaipanchayat.app.data.repository.SettingsRepository
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    val illustrationType: IllustrationType
)

enum class IllustrationType {
    KULHAD_CHAI,
    HEADLINE_TELEPROMPTER,
    HINDI_TTS_VOICE,
    TOPIC_CURATOR
}

val ONBOARDING_PAGES = listOf(
    OnboardingPageData(
        badge = "गरमा-गरम संपादकीय",
        title = "चाय की चुस्की के साथ\nताज़ा पंचायत",
        subtitle = "स्थानीय, राष्ट्रीय एवं अंतरराष्ट्रीय पत्रकारिता",
        description = "सुबह की चाय के साथ देश-दुनिया की निष्पक्ष और प्रामाणिक खबरें, खास संपादकीय विश्लेषण के साथ।",
        accentColor = ChaiSaffron,
        accentGradient = ChaiBrandGradient,
        illustrationType = IllustrationType.KULHAD_CHAI
    ),
    OnboardingPageData(
        badge = "लाइव न्यूज़ बुलेटिन",
        title = "लाइव टेलीप्रॉम्प्टर एवं\nब्रेकिंग अलर्ट्स",
        subtitle = "हर पल की ताज़ा घटनाक्रम पर पैनी नज़र",
        description = "स्मूद टिकर, ब्रेकिंग फ्लैश और लाइव वीडियो बुलेटिन जो आपको रखें हर खबर में सबसे आगे।",
        accentColor = ChaiCrimson,
        accentGradient = ChaiBrandGradient,
        illustrationType = IllustrationType.HEADLINE_TELEPROMPTER
    ),
    OnboardingPageData(
        badge = "ऑडियो समाचार",
        title = "सुनिए खबरें शुद्ध\nहिन्दी आवाज़ में",
        subtitle = "सफर या काम के दौरान ऑडियो पठन",
        description = "स्मार्ट टेक्स्ट-टू-स्पीच और हार्मोनिक ऑडियो वेवफॉर्म, अब खबरें पढ़ना ही नहीं सुनना भी आसान।",
        accentColor = ChaiAmber,
        accentGradient = ChaiAmberGradient,
        illustrationType = IllustrationType.HINDI_TTS_VOICE
    ),
    OnboardingPageData(
        badge = "आपकी अपनी पंचायत",
        title = "पसंदीदा विषय चुनें\nऔर तैयार हो जाएं",
        subtitle = "राजनीति, खेल, सिनेमा, धर्म और विदेश",
        description = "अपनी पसंद के अनुसार पसंदीदा विषय कस्टमाइज़ करें और ऑफलाइन पठन के लिए आसानी से सहेजें।",
        accentColor = ChaiGold,
        accentGradient = ChaiAmberGradient,
        illustrationType = IllustrationType.TOPIC_CURATOR
    )
)

@OptIn(ExperimentalLayoutApi::class)
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

    // Whimsical Selected Topics on Page 4
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
        // Ambient background glowing orbs (whimsical ambient)
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
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Chai Panchayat Mini Brand Header
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
                    // Empty spacer to keep balance
                    Spacer(modifier = Modifier.width(60.dp))
                }
            }

            // Pager Body (Hero illustration & Editorial description)
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
 * Individual Page Layout
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OnboardingPageContent(
    data: OnboardingPageData,
    pageIndex: Int,
    selectedTopics: List<String>,
    onToggleTopic: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Whimsical Interactive Illustration Container
        Box(
            modifier = Modifier
                .size(240.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            when (data.illustrationType) {
                IllustrationType.KULHAD_CHAI -> {
                    WhimsicalChaiCupIllustration(accentColor = data.accentColor)
                }
                IllustrationType.HEADLINE_TELEPROMPTER -> {
                    WhimsicalTeleprompterIllustration(accentColor = data.accentColor)
                }
                IllustrationType.HINDI_TTS_VOICE -> {
                    WhimsicalAudioVoiceIllustration(accentColor = data.accentColor)
                }
                IllustrationType.TOPIC_CURATOR -> {
                    WhimsicalTopicCuratorIllustration(
                        selectedCount = selectedTopics.size,
                        accentColor = data.accentColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Badge pill
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = data.accentColor.copy(alpha = 0.14f),
            border = androidx.compose.foundation.BorderStroke(1.dp, data.accentColor.copy(alpha = 0.35f)),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
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
            fontSize = 24.sp,
            lineHeight = 33.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtitle
        Text(
            text = data.subtitle,
            fontFamily = InterFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.5.sp,
            color = ChaiTheme.extended.brandText,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Editorial description or interactive selector
        if (data.illustrationType == IllustrationType.TOPIC_CURATOR) {
            // Interactive topic chips selection
            val allTopics = listOf(
                "राजनीति", "देश", "विदेश", "उत्तर प्रदेश",
                "संपादकीय", "अर्थव्यवस्था", "खेल", "सिनेमा"
            )
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
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
        } else {
            Text(
                text = data.description,
                fontFamily = InterFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 13.5.sp,
                lineHeight = 21.sp,
                textAlign = TextAlign.Center,
                color = ChaiTheme.extended.textSecondary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

/**
 * 1. Whimsical Steaming Chai Cup Illustration (Canvas + Particles + Oscillating Steam)
 */
@Composable
private fun WhimsicalChaiCupIllustration(
    accentColor: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "chai_steam")
    val steamOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "steam_rise"
    )

    val gentleWobble by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cup_wobble"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Glowing halo behind cup
        Box(
            modifier = Modifier
                .size(180.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.35f),
                            accentColor.copy(alpha = 0.10f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Canvas(
            modifier = Modifier
                .size(190.dp)
                .rotate(gentleWobble)
        ) {
            val width = size.width
            val height = size.height

            // 1. Saucer Plate
            val saucerRect = androidx.compose.ui.geometry.Rect(
                offset = Offset(width * 0.18f, height * 0.72f),
                size = Size(width * 0.64f, height * 0.10f)
            )
            drawOval(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF8D5B4C), Color(0xFF5A362B))
                ),
                topLeft = saucerRect.topLeft,
                size = saucerRect.size
            )

            // 2. Terracotta Kulhad Clay Cup Body
            val cupPath = Path().apply {
                moveTo(width * 0.30f, height * 0.44f) // top left
                lineTo(width * 0.70f, height * 0.44f) // top right
                lineTo(width * 0.63f, height * 0.74f) // bottom right
                lineTo(width * 0.37f, height * 0.74f) // bottom left
                close()
            }
            drawPath(
                path = cupPath,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFD4704B),
                        Color(0xFFB85633),
                        Color(0xFF8F3E22)
                    ),
                    startX = width * 0.30f,
                    endX = width * 0.70f
                )
            )

            // 3. Terracotta clay decorative ribbed rings
            drawLine(
                color = Color(0x66FFE4D6),
                start = Offset(width * 0.32f, height * 0.52f),
                end = Offset(width * 0.68f, height * 0.52f),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0x44FFE4D6),
                start = Offset(width * 0.34f, height * 0.62f),
                end = Offset(width * 0.66f, height * 0.62f),
                strokeWidth = 2.5f,
                cap = StrokeCap.Round
            )

            // 4. Steaming hot chai liquid top surface
            drawOval(
                brush = Brush.radialGradient(
                    listOf(Color(0xFFFFC58D), Color(0xFFC76C2E))
                ),
                topLeft = Offset(width * 0.30f, height * 0.41f),
                size = Size(width * 0.40f, height * 0.08f)
            )

            // 5. Rising Whimsical Steam Wisps (Mathematical Sinusoidal waves)
            for (i in 0..2) {
                val steamPath = Path()
                val baseX = width * (0.42f + i * 0.08f)
                val steamAlpha = ((1f - steamOffset) * 0.85f).coerceIn(0f, 1f)

                val startY = height * 0.40f
                val endY = height * (0.40f - 0.28f * steamOffset)

                var y = startY
                steamPath.moveTo(baseX, y)
                while (y >= endY) {
                    val waveOffset = (sin((y * 0.08f) + (steamOffset * 2 * PI.toFloat()) + i) * 12f)
                    steamPath.lineTo(baseX + waveOffset, y)
                    y -= 6f
                }

                drawPath(
                    path = steamPath,
                    color = Color.White.copy(alpha = steamAlpha),
                    style = Stroke(width = 4f, cap = StrokeCap.Round)
                )
            }
        }

        // Whimsical Floating Mini "News Paper" Emoji badge
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = androidx.compose.foundation.BorderStroke(1.dp, ChaiTheme.extended.border),
            shadowElevation = 6.dp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-12).dp, y = 14.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Newspaper,
                    contentDescription = null,
                    tint = ChaiSaffron,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "ताज़ा ख़बर",
                    fontFamily = InterFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * 2. Whimsical Headline Teleprompter Illustration (Animated Slanted News Cards)
 */
@Composable
private fun WhimsicalTeleprompterIllustration(
    accentColor: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ticker_slide")
    val tickerY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ticker_vertical"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Red Ambient Glow
        Box(
            modifier = Modifier
                .size(170.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.28f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Stack of 3 News Cards with live shifting offset
        val headlines = listOf(
            "🔴 विशेष: लखनऊ में राज्य बजट सत्र आज से शुरू",
            "⚡ खेल: भारतीय टीम ने फाइनल में दर्ज की ऐतिहासिक जीत",
            "🌍 दुनिया: वैश्विक शिखर सम्मेलन में नई आर्थिक संधि"
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            headlines.forEachIndexed { index, text ->
                val cardOffset = ((index - tickerY) * 12f)
                val isHot = index == 0

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isHot) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isHot) 1.5.dp else 1.dp,
                        color = if (isHot) ChaiCrimson.copy(alpha = 0.6f) else ChaiTheme.extended.border
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isHot) 6.dp else 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = cardOffset.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(if (isHot) ChaiCrimson else ChaiTheme.extended.muted, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = text,
                            fontFamily = NotoSerifFamily,
                            fontWeight = if (isHot) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.5.sp,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

/**
 * 3. Whimsical Hindi TTS Audio Waveform Illustration
 */
@Composable
private fun WhimsicalAudioVoiceIllustration(
    accentColor: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "audio_bars")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "audio_wave_phase"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Glowing circular audio speaker halo
        Box(
            modifier = Modifier
                .size(190.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.30f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Speaker Icon Center Disk
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(ChaiAmberGradient, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.RecordVoiceOver,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        // Harmonic Floating Audio Equalizer Bars around the disk
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-14).dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0..10) {
                val heightMultiplier = (sin(wavePhase + i * 0.6f) * 0.5f + 0.5f)
                val barHeight = (12.dp + 32.dp * heightMultiplier)

                Box(
                    modifier = Modifier
                        .width(5.dp)
                        .height(barHeight)
                        .background(
                            brush = Brush.verticalGradient(
                                listOf(ChaiAmber, ChaiSaffron)
                            ),
                            shape = RoundedCornerShape(3.dp)
                        )
                )
            }
        }
    }
}

/**
 * 4. Whimsical Topic Curator Illustration (Floating Category Spheres)
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
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Center Target Circle
        Box(
            modifier = Modifier
                .size(130.dp)
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
                    modifier = Modifier.size(32.dp)
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
            Triple("🏛️ राजनीति", (-60).dp, (-50).dp),
            Triple("🏏 खेल", 64.dp, (-46).dp),
            Triple("🎬 सिनेमा", (-68).dp, 52.dp),
            Triple("🇮🇳 देश", 66.dp, 50.dp)
        )

        orbits.forEachIndexed { index, (label, x, y) ->
            val dynamicY = y + (if (index % 2 == 0) floatOffset else -floatOffset).dp
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, ChaiTheme.extended.border),
                shadowElevation = 4.dp,
                modifier = Modifier
                    .offset(x = x, y = dynamicY)
            ) {
                Text(
                    text = label,
                    fontFamily = InterFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
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
            .padding(horizontal = 24.dp, vertical = 20.dp),
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
                    targetValue = if (isSelected) 26f else 8f,
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

        // Primary Action Button (Next or Get Started "पंचायत में प्रवेश करें")
        Button(
            onClick = onNextClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = accentColor,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(26.dp),
            contentPadding = PaddingValues(horizontal = 22.dp, vertical = 13.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
            modifier = Modifier.testTag("onboarding_primary_action")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isLastPage) "शुरू करें (Enter)" else "आगे बढ़ें",
                    fontFamily = InterFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.5.sp
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

        // 4 floating whimsical ambient particles with dynamic orbital trails
        val particleColors = listOf(
            ChaiSaffron.copy(alpha = 0.08f),
            ChaiAmber.copy(alpha = 0.07f),
            ChaiCrimson.copy(alpha = 0.06f),
            ChaiGold.copy(alpha = 0.08f)
        )

        for (i in 0..3) {
            val angle = phase + (i * PI.toFloat() / 2f)
            val radiusX = w * 0.35f
            val radiusY = h * 0.25f

            val cx = w * 0.5f + cos(angle) * radiusX
            val cy = h * 0.4f + sin(angle * 1.5f) * radiusY

            drawCircle(
                color = particleColors[i],
                radius = 80f + (i * 20f),
                center = Offset(cx, cy)
            )
        }
    }
}
