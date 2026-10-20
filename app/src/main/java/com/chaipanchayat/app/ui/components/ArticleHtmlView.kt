package com.chaipanchayat.app.ui.components

import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.text.Html
import android.text.style.StyleSpan
import android.text.style.URLSpan
import android.text.style.UnderlineSpan
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.chaipanchayat.app.ui.theme.ChaiCrimson
import com.chaipanchayat.app.ui.theme.ChaiSaffron
import com.chaipanchayat.app.ui.theme.ChaiTheme
import com.chaipanchayat.app.ui.theme.InterFamily
import com.chaipanchayat.app.ui.theme.NotoSerifFamily
import com.chaipanchayat.app.utils.HtmlUtils
import java.util.regex.Pattern

private sealed class HtmlBlock {
    data class Paragraph(val text: String) : HtmlBlock()
    data class Subheading(val text: String) : HtmlBlock()
    data class Heading(val level: Int, val text: String) : HtmlBlock()
    data class Blockquote(val text: String) : HtmlBlock()
    data class ListItem(val isOrdered: Boolean, val index: Int, val text: String) : HtmlBlock()
    data class ImageBlock(val src: String, val alt: String?, val caption: String? = null) : HtmlBlock()
    data class VideoEmbed(val youtubeId: String, val title: String? = null, val videoUrl: String? = null) : HtmlBlock()
    data object Divider : HtmlBlock()
}

@Composable
fun ArticleHtmlView(
    html: String,
    multiplier: Float = 1.0f,
    attachedVideoYtId: String? = null,
    attachedVideoUrl: String? = null,
    articleTitle: String? = null,
    modifier: Modifier = Modifier
) {
    val blocks = remember(html, attachedVideoYtId, attachedVideoUrl) {
        parseHtmlToBlocks(
            rawHtml = html,
            attachedVideoYtId = attachedVideoYtId,
            attachedVideoUrl = attachedVideoUrl,
            articleTitle = articleTitle
        )
    }
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        blocks.forEach { block ->
            when (block) {
                is HtmlBlock.Paragraph -> {
                    RenderInlineText(
                        html = block.text,
                        baseStyle = TextStyle(
                            fontFamily = InterFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = (17.5f * multiplier).sp,
                            lineHeight = (28.5f * multiplier).sp,
                            letterSpacing = 0.2.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        onUriClick = { uriHandler.openUri(it) }
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                is HtmlBlock.Subheading -> {
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = block.text,
                        fontFamily = NotoSerifFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = (20f * multiplier).sp,
                        lineHeight = (29f * multiplier).sp,
                        letterSpacing = 0.1.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                is HtmlBlock.Heading -> {
                    val fontSize = when (block.level) {
                        1 -> 24 * multiplier
                        2 -> 22 * multiplier
                        3 -> 20 * multiplier
                        else -> 19 * multiplier
                    }.sp
                    val lineHeight = when (block.level) {
                        1 -> 32 * multiplier
                        2 -> 30 * multiplier
                        3 -> 28 * multiplier
                        else -> 27 * multiplier
                    }.sp

                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = HtmlUtils.stripHtml(block.text),
                        fontFamily = NotoSerifFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = fontSize,
                        lineHeight = lineHeight,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                is HtmlBlock.Blockquote -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.5.dp)
                                .height(42.dp)
                                .background(ChaiTheme.extended.brandText, RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = HtmlUtils.stripHtml(block.text),
                            fontFamily = NotoSerifFamily,
                            fontWeight = FontWeight.Normal,
                            fontStyle = FontStyle.Italic,
                            fontSize = (17.5f * multiplier).sp,
                            lineHeight = (27.5f * multiplier).sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                is HtmlBlock.ListItem -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                    ) {
                        Text(
                            text = if (block.isOrdered) "${block.index}." else "•",
                            fontFamily = InterFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = (16.5f * multiplier).sp,
                            color = ChaiTheme.extended.brandText,
                            modifier = Modifier.width(22.dp)
                        )
                        Box(modifier = Modifier.weight(1f)) {
                            RenderInlineText(
                                html = block.text,
                                baseStyle = TextStyle(
                                    fontFamily = InterFamily,
                                    fontSize = (16.5f * multiplier).sp,
                                    lineHeight = (25.5f * multiplier).sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                onUriClick = { uriHandler.openUri(it) }
                            )
                        }
                    }
                }

                is HtmlBlock.ImageBlock -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ChaiTheme.extended.surfaceSecondary)
                            .border(
                                1.dp,
                                ChaiTheme.extended.border.copy(alpha = 0.7f),
                                RoundedCornerShape(12.dp)
                            )
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(block.src)
                                .crossfade(true)
                                .build(),
                            contentDescription = block.alt ?: block.caption ?: "Article photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 10f)
                                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                                .background(ChaiTheme.extended.skeleton)
                        )
                        if (!block.caption.isNullOrBlank()) {
                            Text(
                                text = block.caption,
                                fontFamily = InterFamily,
                                fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.Medium,
                                fontSize = (13f * multiplier).sp,
                                lineHeight = (18.5f * multiplier).sp,
                                color = ChaiTheme.extended.muted,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                is HtmlBlock.VideoEmbed -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    ChaiVideoPlayer(
                        youtubeId = block.youtubeId.ifBlank { null },
                        videoUrl = block.videoUrl,
                        title = block.title ?: articleTitle,
                        autoPlay = true, // Smooth autoplay in-flow
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                is HtmlBlock.Divider -> {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = ChaiTheme.extended.border,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RenderInlineText(
    html: String,
    baseStyle: TextStyle,
    onUriClick: (String) -> Unit
) {
    val brandColor = ChaiTheme.extended.brandText
    val annotatedString = remember(html, baseStyle, brandColor) {
        htmlToAnnotatedString(
            html = html,
            linkColor = brandColor,
            boldFontWeight = FontWeight.Bold
        )
    }

    val hasUrls = remember(annotatedString) {
        annotatedString.getStringAnnotations(tag = "URL", start = 0, end = annotatedString.length).isNotEmpty()
    }

    if (hasUrls) {
        @Suppress("DEPRECATION")
        ClickableText(
            text = annotatedString,
            style = baseStyle,
            onClick = { offset ->
                annotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                    .firstOrNull()?.let { annotation ->
                        onUriClick(annotation.item)
                    }
            }
        )
    } else {
        Text(
            text = annotatedString,
            style = baseStyle
        )
    }
}

/**
 * Converts HTML containing inline tags (<b>, <strong>, <i>, <em>, <u>, <a>, <br>)
 * into a rich Compose AnnotatedString with proper SpanStyles and annotations.
 */
private fun htmlToAnnotatedString(
    html: String,
    linkColor: Color,
    boldFontWeight: FontWeight = FontWeight.Bold
): AnnotatedString {
    val cleanHtml = html
        .replace(Regex("^\\s*<p[^>]*>", RegexOption.IGNORE_CASE), "")
        .replace(Regex("</p>\\s*$", RegexOption.IGNORE_CASE), "")
        .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")

    val spanned = try {
        Html.fromHtml(cleanHtml, Html.FROM_HTML_MODE_LEGACY)
    } catch (_: Exception) {
        null
    } ?: return AnnotatedString(HtmlUtils.stripHtml(html))

    val rawText = spanned.toString()
    val trimmedText = rawText.trimEnd('\n', '\r')
    val maxLen = trimmedText.length

    return buildAnnotatedString {
        append(trimmedText)
        val spans = spanned.getSpans(0, spanned.length, Any::class.java)
        for (span in spans) {
            val rawStart = spanned.getSpanStart(span)
            val rawEnd = spanned.getSpanEnd(span)
            val start = rawStart.coerceIn(0, maxLen)
            val end = rawEnd.coerceIn(0, maxLen)
            if (start >= end) continue

            when (span) {
                is StyleSpan -> {
                    when (span.style) {
                        Typeface.BOLD -> addStyle(SpanStyle(fontWeight = boldFontWeight), start, end)
                        Typeface.ITALIC -> addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, end)
                        Typeface.BOLD_ITALIC -> addStyle(
                            SpanStyle(fontWeight = boldFontWeight, fontStyle = FontStyle.Italic),
                            start,
                            end
                        )
                    }
                }
                is UnderlineSpan -> {
                    addStyle(SpanStyle(textDecoration = TextDecoration.Underline), start, end)
                }
                is URLSpan -> {
                    val url = span.url ?: ""
                    addStringAnnotation(tag = "URL", annotation = url, start = start, end = end)
                    addStyle(
                        SpanStyle(
                            color = linkColor,
                            fontWeight = FontWeight.SemiBold,
                            textDecoration = TextDecoration.Underline
                        ),
                        start,
                        end
                    )
                }
            }
        }
    }
}

/**
 * Checks whether an HTML snippet inside a paragraph is actually an editorial sub-heading.
 * In WordPress, authors format sub-headings as standalone <p><strong>Heading</strong></p>
 * or <p><b>Heading</b></p>.
 */
private fun isSubheadingParagraph(innerHtml: String): Boolean {
    val clean = innerHtml.trim()
    val hasBold = clean.contains("<strong", ignoreCase = true) || clean.contains("<b", ignoreCase = true)
    if (!hasBold) return false

    val withoutBold = clean
        .replace(Regex("<strong[^>]*>[\\s\\S]*?</strong>", RegexOption.IGNORE_CASE), "")
        .replace(Regex("<b[^>]*>[\\s\\S]*?</b>", RegexOption.IGNORE_CASE), "")

    val nonBoldText = HtmlUtils.stripHtml(withoutBold).trim()
    if (nonBoldText.isEmpty()) {
        val totalText = HtmlUtils.stripHtml(clean).trim()
        return totalText.isNotEmpty() && totalText.length <= 220
    }
    return false
}

/**
 * Extracts a high-res, valid image URL from img attributes or figure snippet.
 * Checks src, data-orig-file, data-large-file, data-src, data-lazy-src, and srcset.
 */
private fun extractImageSrc(snippet: String): String? {
    val patterns = listOf(
        Pattern.compile("data-orig-file=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE),
        Pattern.compile("data-large-file=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE),
        Pattern.compile("data-src=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE),
        Pattern.compile("data-lazy-src=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE),
        Pattern.compile("src=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE)
    )

    for (p in patterns) {
        val m = p.matcher(snippet)
        if (m.find()) {
            val candidate = m.group(1)?.trim()
            if (!candidate.isNullOrBlank() && !candidate.startsWith("data:image")) {
                return if (candidate.startsWith("http://")) candidate.replaceFirst("http://", "https://") else candidate
            }
        }
    }
    return null
}

private fun parseHtmlToBlocks(
    rawHtml: String,
    attachedVideoYtId: String? = null,
    attachedVideoUrl: String? = null,
    articleTitle: String? = null
): List<HtmlBlock> {
    val blocks = mutableListOf<HtmlBlock>()
    val clean = rawHtml
        .replace("\r\n", "\n")
        .replace(Regex("<script[\\s\\S]*?</script>", RegexOption.IGNORE_CASE), "")
        .replace(Regex("<style[\\s\\S]*?</style>", RegexOption.IGNORE_CASE), "")

    val blockPattern = Pattern.compile(
        "<(p|h1|h2|h3|h4|h5|h6|blockquote|ul|ol|figure|img|hr|iframe|video|strong|b)([^>]*)>([\\s\\S]*?)</\\1>|<img([^>]*)/?>|<hr\\s*/?>|<iframe([^>]*)/?>|<video([^>]*)/?>",
        Pattern.CASE_INSENSITIVE
    )
    val youtubePattern = Pattern.compile(
        "(?:youtube\\.com\\/(?:embed\\/|watch\\?v=|v\\/|shorts\\/)|youtu\\.be\\/)([a-zA-Z0-9_-]{11})",
        Pattern.CASE_INSENSITIVE
    )
    val videoSrcPattern = Pattern.compile(
        "src=[\"']([^\"']+\\.(?:mp4|webm|m4v)[^\"']*)[\"']",
        Pattern.CASE_INSENSITIVE
    )
    val figcaptionPattern = Pattern.compile("<figcaption[^>]*>([\\s\\S]*?)</figcaption>", Pattern.CASE_INSENSITIVE)

    val matcher = blockPattern.matcher(clean)
    var foundAny = false
    var hasEmbeddedVideo = false

    while (matcher.find()) {
        foundAny = true
        val tag = (matcher.group(1) ?: if (matcher.group(4) != null) "img" else if (matcher.group(5) != null) "iframe" else if (matcher.group(6) != null) "video" else "hr").lowercase()
        val attrs = matcher.group(2) ?: matcher.group(4) ?: matcher.group(5) ?: matcher.group(6) ?: ""
        val inner = matcher.group(3) ?: ""

        when {
            tag == "hr" -> blocks.add(HtmlBlock.Divider)

            tag == "strong" || tag == "b" -> {
                val stripped = HtmlUtils.stripHtml(inner).trim()
                if (stripped.isNotBlank() && stripped.length <= 220) {
                    blocks.add(HtmlBlock.Subheading(stripped))
                } else if (stripped.isNotBlank()) {
                    blocks.add(HtmlBlock.Paragraph("<$tag>$inner</$tag>"))
                }
            }

            tag == "figure" -> {
                val fullFigure = "$attrs $inner"
                // Check if figure is a video embed
                val ytMatcher = youtubePattern.matcher(fullFigure)
                val vSrcMatcher = videoSrcPattern.matcher(fullFigure)
                if (ytMatcher.find()) {
                    val ytId = ytMatcher.group(1) ?: ""
                    blocks.add(HtmlBlock.VideoEmbed(ytId, articleTitle, null))
                    hasEmbeddedVideo = true
                } else if (vSrcMatcher.find()) {
                    val vUrl = vSrcMatcher.group(1) ?: ""
                    blocks.add(HtmlBlock.VideoEmbed("", articleTitle, vUrl))
                    hasEmbeddedVideo = true
                } else {
                    // Extract image and caption
                    val imgSrc = extractImageSrc(fullFigure)
                    if (!imgSrc.isNullOrBlank()) {
                        val capMatcher = figcaptionPattern.matcher(inner)
                        val caption = if (capMatcher.find()) HtmlUtils.stripHtml(capMatcher.group(1) ?: "") else null
                        val altMatcher = Pattern.compile("alt=[\"']([^\"']*)[\"']", Pattern.CASE_INSENSITIVE).matcher(fullFigure)
                        val alt = if (altMatcher.find()) altMatcher.group(1) else null
                        blocks.add(HtmlBlock.ImageBlock(imgSrc, alt, caption?.ifBlank { null }))
                    }
                }
            }

            tag == "iframe" || tag == "video" -> {
                val fullSnippet = "$attrs $inner"
                val ytMatcher = youtubePattern.matcher(fullSnippet)
                if (ytMatcher.find()) {
                    val ytId = ytMatcher.group(1) ?: ""
                    blocks.add(HtmlBlock.VideoEmbed(ytId, articleTitle, null))
                    hasEmbeddedVideo = true
                } else {
                    val vSrcMatcher = videoSrcPattern.matcher(fullSnippet)
                    if (vSrcMatcher.find()) {
                        val vUrl = vSrcMatcher.group(1) ?: ""
                        blocks.add(HtmlBlock.VideoEmbed("", articleTitle, vUrl))
                        hasEmbeddedVideo = true
                    }
                }
            }

            tag == "img" -> {
                val imgSrc = extractImageSrc(attrs)
                if (!imgSrc.isNullOrBlank()) {
                    val altMatch = Pattern.compile("alt=[\"']([^\"']*)[\"']", Pattern.CASE_INSENSITIVE).matcher(attrs)
                    val alt = if (altMatch.find()) altMatch.group(1) else null
                    blocks.add(HtmlBlock.ImageBlock(imgSrc, alt, null))
                }
            }

            tag == "p" || tag == "blockquote" -> {
                // 1. Check if paragraph contains an embedded video or YouTube link
                val ytMatcher = youtubePattern.matcher(inner)
                val vSrcMatcher = videoSrcPattern.matcher(inner)
                if (ytMatcher.find() && (inner.contains("iframe") || inner.contains("youtube.com") || inner.contains("youtu.be") || inner.trim().startsWith("http"))) {
                    val ytId = ytMatcher.group(1) ?: ""
                    blocks.add(HtmlBlock.VideoEmbed(ytId, articleTitle, null))
                    hasEmbeddedVideo = true
                } else if (vSrcMatcher.find()) {
                    val vUrl = vSrcMatcher.group(1) ?: ""
                    blocks.add(HtmlBlock.VideoEmbed("", articleTitle, vUrl))
                    hasEmbeddedVideo = true
                } else if (inner.contains("<img", ignoreCase = true)) {
                    // Extract all images within this paragraph
                    val imgPattern = Pattern.compile("<img[^>]*>", Pattern.CASE_INSENSITIVE)
                    val imgM = imgPattern.matcher(inner)
                    while (imgM.find()) {
                        val imgTag = imgM.group(0) ?: ""
                        val src = extractImageSrc(imgTag)
                        if (!src.isNullOrBlank()) {
                            val altM = Pattern.compile("alt=[\"']([^\"']*)[\"']", Pattern.CASE_INSENSITIVE).matcher(imgTag)
                            val alt = if (altM.find()) altM.group(1) else null
                            blocks.add(HtmlBlock.ImageBlock(src, alt, null))
                        }
                    }
                    val strippedText = inner.replace(Regex("<img[^>]*>", RegexOption.IGNORE_CASE), "").trim()
                    if (strippedText.isNotBlank()) {
                        if (isSubheadingParagraph(strippedText)) {
                            blocks.add(HtmlBlock.Subheading(HtmlUtils.stripHtml(strippedText).trim()))
                        } else {
                            blocks.add(HtmlBlock.Paragraph(strippedText))
                        }
                    }
                } else if (tag == "blockquote") {
                    blocks.add(HtmlBlock.Blockquote(inner.trim()))
                } else {
                    val text = inner.trim()
                    if (text.isNotBlank()) {
                        if (isSubheadingParagraph(text)) {
                            blocks.add(HtmlBlock.Subheading(HtmlUtils.stripHtml(text).trim()))
                        } else {
                            blocks.add(HtmlBlock.Paragraph(text))
                        }
                    }
                }
            }

            tag.startsWith("h") -> {
                val level = tag.substring(1).toIntOrNull() ?: 2
                blocks.add(HtmlBlock.Heading(level, inner.trim()))
            }

            tag == "ul" || tag == "ol" -> {
                val isOrdered = tag == "ol"
                val liPattern = Pattern.compile("<li[^>]*>([\\s\\S]*?)</li>", Pattern.CASE_INSENSITIVE)
                val liMatcher = liPattern.matcher(inner)
                var index = 1
                while (liMatcher.find()) {
                    val liInner = liMatcher.group(1) ?: ""
                    blocks.add(HtmlBlock.ListItem(isOrdered, index, liInner.trim()))
                    index++
                }
            }
        }
    }

    if (!foundAny || blocks.isEmpty()) {
        val stripped = HtmlUtils.stripHtml(clean)
        if (stripped.isNotBlank()) {
            blocks.add(HtmlBlock.Paragraph(stripped))
        }
    }

    // If post has an attached video but it was not present as an inline iframe/video tag,
    // seamlessly insert it into the article text flow (after 1st paragraph or at index 1)
    if (!hasEmbeddedVideo && (!attachedVideoYtId.isNullOrBlank() || !attachedVideoUrl.isNullOrBlank())) {
        val embed = HtmlBlock.VideoEmbed(
            youtubeId = attachedVideoYtId ?: "",
            title = articleTitle,
            videoUrl = attachedVideoUrl
        )
        if (blocks.size >= 2) {
            blocks.add(1, embed)
        } else {
            blocks.add(embed)
        }
    }

    return blocks
}
