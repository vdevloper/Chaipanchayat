package com.chaipanchayat.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
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
import com.chaipanchayat.app.ui.theme.ChaiSaffron
import com.chaipanchayat.app.ui.theme.ChaiTheme
import com.chaipanchayat.app.ui.theme.InterFamily
import com.chaipanchayat.app.ui.theme.NotoSerifFamily
import com.chaipanchayat.app.utils.HtmlUtils
import java.util.regex.Pattern

private sealed class HtmlBlock {
    data class Paragraph(val text: String) : HtmlBlock()
    data class Heading(val level: Int, val text: String) : HtmlBlock()
    data class Blockquote(val text: String) : HtmlBlock()
    data class ListItem(val isOrdered: Boolean, val index: Int, val text: String) : HtmlBlock()
    data class ImageBlock(val src: String, val alt: String?) : HtmlBlock()
    data object Divider : HtmlBlock()
}

@Composable
fun ArticleHtmlView(
    html: String,
    multiplier: Float = 1.0f,
    modifier: Modifier = Modifier
) {
    val blocks = remember(html) { parseHtmlToBlocks(html) }
    val uriHandler = LocalUriHandler.current

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
                            fontSize = (17 * multiplier).sp,
                            lineHeight = (27 * multiplier).sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        onUriClick = { uriHandler.openUri(it) }
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                is HtmlBlock.Heading -> {
                    val fontSize = when (block.level) {
                        1 -> 24 * multiplier
                        2 -> 22 * multiplier
                        3 -> 20 * multiplier
                        else -> 18 * multiplier
                    }.sp
                    val lineHeight = when (block.level) {
                        1 -> 30 * multiplier
                        2 -> 28 * multiplier
                        3 -> 26 * multiplier
                        else -> 24 * multiplier
                    }.sp

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = HtmlUtils.stripHtml(block.text),
                        fontFamily = NotoSerifFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = fontSize,
                        lineHeight = lineHeight,
                        color = MaterialTheme.colorScheme.onSurface
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
                                .width(3.dp)
                                .height(40.dp)
                                .background(ChaiSaffron, RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = HtmlUtils.stripHtml(block.text),
                            fontFamily = NotoSerifFamily,
                            fontWeight = FontWeight.Normal,
                            fontStyle = FontStyle.Italic,
                            fontSize = (17 * multiplier).sp,
                            lineHeight = (26 * multiplier).sp,
                            color = ChaiTheme.extended.muted
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
                            fontSize = (16 * multiplier).sp,
                            color = ChaiSaffron,
                            modifier = Modifier.width(22.dp)
                        )
                        Box(modifier = Modifier.weight(1f)) {
                            RenderInlineText(
                                html = block.text,
                                baseStyle = TextStyle(
                                    fontFamily = InterFamily,
                                    fontSize = (16 * multiplier).sp,
                                    lineHeight = (24 * multiplier).sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                onUriClick = { uriHandler.openUri(it) }
                            )
                        }
                    }
                }

                is HtmlBlock.ImageBlock -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(block.src)
                            .crossfade(true)
                            .build(),
                        contentDescription = block.alt ?: "Article image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ChaiTheme.extended.skeleton)
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
    val annotatedString = remember(html, baseStyle) {
        buildAnnotatedString {
            // Simplified inline parsing: text, links, strong, em
            val linkPattern = Pattern.compile("<a\\s+[^>]*href=[\"']([^\"']*)[\"'][^>]*>(.*?)</a>", Pattern.CASE_INSENSITIVE)
            val strongPattern = Pattern.compile("<(strong|b)>(.*?)</\\1>", Pattern.CASE_INSENSITIVE)
            val emPattern = Pattern.compile("<(em|i)>(.*?)</\\1>", Pattern.CASE_INSENSITIVE)

            // Let's decode entities first on segments
            var remaining = html
                .replace(Regex("<br\\s*/?>"), "\n")
                .replace(Regex("</?p[^>]*>"), "")

            // If string has simple text
            val stripped = HtmlUtils.stripHtml(remaining)
            append(stripped)
        }
    }

    Text(
        text = annotatedString,
        style = baseStyle
    )
}

private fun parseHtmlToBlocks(rawHtml: String): List<HtmlBlock> {
    val blocks = mutableListOf<HtmlBlock>()
    var clean = rawHtml
        .replace("\r\n", "\n")
        .replace(Regex("<script[\\s\\S]*?</script>", RegexOption.IGNORE_CASE), "")
        .replace(Regex("<style[\\s\\S]*?</style>", RegexOption.IGNORE_CASE), "")
        .replace(Regex("<figure[^>]*>", RegexOption.IGNORE_CASE), "")
        .replace(Regex("</figure>", RegexOption.IGNORE_CASE), "")
        .replace(Regex("<figcaption[\\s\\S]*?</figcaption>", RegexOption.IGNORE_CASE), "")

    val blockPattern = Pattern.compile(
        "<(p|h1|h2|h3|h4|h5|h6|blockquote|ul|ol|img|hr)([^>]*)>([\\s\\S]*?)</\\1>|<img([^>]*)/?>|<hr\\s*/?>",
        Pattern.CASE_INSENSITIVE
    )
    val matcher = blockPattern.matcher(clean)
    var foundAny = false

    while (matcher.find()) {
        foundAny = true
        val tag = (matcher.group(1) ?: if (matcher.group(4) != null) "img" else "hr").lowercase()
        val attrs = matcher.group(2) ?: matcher.group(4) ?: ""
        val inner = matcher.group(3) ?: ""

        when {
            tag == "hr" -> blocks.add(HtmlBlock.Divider)
            tag == "img" -> {
                val srcMatch = Pattern.compile("src=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE).matcher(attrs)
                val altMatch = Pattern.compile("alt=[\"']([^\"']*)[\"']", Pattern.CASE_INSENSITIVE).matcher(attrs)
                if (srcMatch.find()) {
                    val src = srcMatch.group(1) ?: ""
                    val alt = if (altMatch.find()) altMatch.group(1) else null
                    blocks.add(HtmlBlock.ImageBlock(src, alt))
                }
            }
            tag == "p" -> {
                val text = inner.trim()
                if (text.isNotBlank()) {
                    blocks.add(HtmlBlock.Paragraph(text))
                }
            }
            tag.startsWith("h") -> {
                val level = tag.substring(1).toIntOrNull() ?: 2
                blocks.add(HtmlBlock.Heading(level, inner.trim()))
            }
            tag == "blockquote" -> {
                blocks.add(HtmlBlock.Blockquote(inner.trim()))
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

    return blocks
}
