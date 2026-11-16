package com.chaipanchayat.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.chaipanchayat.app.data.model.WPPost
import com.chaipanchayat.app.ui.theme.ChaiSaffron
import com.chaipanchayat.app.ui.theme.ChaiTheme
import com.chaipanchayat.app.ui.theme.InterFamily
import kotlin.math.absoluteValue

/**
 * Multi-Story Lead Carousel for the Home feed.
 * Cycles smoothly across top lead stories with depth scaling, horizontal image parallax,
 * and an animated pill indicator bar.
 */
@Composable
fun HeroCarousel(
    leadPosts: List<WPPost>,
    getHeadline: (WPPost) -> String,
    onNavigateToArticle: (Long) -> Unit,
    modifier: Modifier = Modifier,
    parallaxOffsetPx: Float = 0f,
    isHindiUi: Boolean = true
) {
    if (leadPosts.isEmpty()) return
    if (leadPosts.size == 1) {
        HeroCard(
            post = leadPosts.first(),
            onClick = { onNavigateToArticle(leadPosts.first().id) },
            displayTitle = getHeadline(leadPosts.first()),
            parallaxOffsetPx = parallaxOffsetPx,
            modifier = modifier
        )
        return
    }

    val pagerState = rememberPagerState(pageCount = { leadPosts.size })

    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            beyondViewportPageCount = 1,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val post = leadPosts[page]
            val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
            val absOffset = pageOffset.absoluteValue.coerceIn(0f, 1f)
            val cardScale = lerp(1.0f, 0.94f, absOffset)
            val cardAlpha = lerp(1.0f, 0.72f, absOffset)

            HeroCard(
                post = post,
                onClick = { onNavigateToArticle(post.id) },
                displayTitle = getHeadline(post),
                parallaxOffsetPx = parallaxOffsetPx,
                horizontalParallaxPx = pageOffset * 44f,
                applyTestTag = page == pagerState.currentPage,
                modifier = Modifier.graphicsLayer {
                    scaleX = cardScale
                    scaleY = cardScale
                    alpha = cardAlpha
                }
            )
        }

        // Sleek Carousel Progress Dots + Counter
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                repeat(leadPosts.size) { index ->
                    val isCurrent = pagerState.currentPage == index
                    val dotWidth by animateDpAsState(
                        targetValue = if (isCurrent) 20.dp else 6.dp,
                        animationSpec = spring(dampingRatio = 0.65f, stiffness = 460f),
                        label = "carousel_dot_w_$index"
                    )
                    val dotColor by animateColorAsState(
                        targetValue = if (isCurrent) ChaiSaffron else ChaiTheme.extended.muted.copy(alpha = 0.42f),
                        animationSpec = tween(220),
                        label = "carousel_dot_c_$index"
                    )
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(dotWidth)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isHindiUi) "बड़ी ख़बरें" else "TOP BRIEFING",
                    color = ChaiTheme.extended.muted,
                    fontFamily = InterFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.5.sp,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${pagerState.currentPage + 1}/${leadPosts.size}",
                    color = ChaiSaffron,
                    fontFamily = InterFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp
                )
            }
        }
    }
}
