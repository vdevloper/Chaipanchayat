package com.chaipanchayat.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chaipanchayat.app.data.repository.SettingsRepository
import com.chaipanchayat.app.data.repository.TextSizePreference
import com.chaipanchayat.app.ui.components.Logo
import com.chaipanchayat.app.ui.theme.ChaiSaffron
import com.chaipanchayat.app.ui.theme.ChaiTheme
import com.chaipanchayat.app.ui.theme.InterFamily
import com.chaipanchayat.app.ui.theme.NotoSerifFamily

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settingsRepo = SettingsRepository.getInstance(context)
    val textSize by settingsRepo.textSize.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Settings",
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = NotoSerifFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 28.sp,
            lineHeight = 34.sp
        )

        Text(
            text = "Preferences & about",
            color = ChaiTheme.extended.muted,
            fontFamily = InterFamily,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Brand Banner Card
        val brandCardShape = RoundedCornerShape(12.dp)
        Card(
            shape = brandCardShape,
            colors = CardDefaults.cardColors(containerColor = ChaiTheme.extended.surfaceSecondary),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 0.5.dp, color = ChaiTheme.extended.border, shape = brandCardShape)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Logo(size = 40.dp)

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Chai Panchayat delivers authentic, independent Indian journalism covering national politics, grassroots developments, society, and culture.",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = InterFamily,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section: Reading Preferences
        SettingsSectionHeader(title = "READING EXPERIENCE")

        val cardShape = RoundedCornerShape(10.dp)
        Card(
            shape = cardShape,
            colors = CardDefaults.cardColors(containerColor = ChaiTheme.extended.surfaceSecondary),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 0.5.dp, color = ChaiTheme.extended.border, shape = cardShape)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FormatSize,
                        contentDescription = null,
                        tint = ChaiSaffron,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Article Text Size",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = InterFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Segmented control
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ChaiTheme.extended.surfaceTertiary, RoundedCornerShape(8.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextSizePreference.entries.forEach { sizeOption ->
                        val isSelected = sizeOption == textSize
                        Surface(
                            onClick = { settingsRepo.setTextSize(sizeOption) },
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) ChaiSaffron else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .testTag("text-size-${sizeOption.key}")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = sizeOption.displayName,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontFamily = InterFamily,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Live preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(6.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Preview: चाय पंचायत निष्पक्ष पत्रकारिता और ज़मीनी हकीकत को समर्पित है।",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = NotoSerifFamily,
                        fontSize = (15 * textSize.multiplier).sp,
                        lineHeight = (22 * textSize.multiplier).sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section: About & Links
        SettingsSectionHeader(title = "ABOUT")

        Card(
            shape = cardShape,
            colors = CardDefaults.cardColors(containerColor = ChaiTheme.extended.surfaceSecondary),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 0.5.dp, color = ChaiTheme.extended.border, shape = cardShape)
        ) {
            Column {
                SettingsLinkRow(
                    icon = Icons.AutoMirrored.Outlined.OpenInNew,
                    title = "Visit chaipanchayat.com",
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://chaipanchayat.com"))
                        context.startActivity(intent)
                    }
                )

                HorizontalDivider(thickness = 0.5.dp, color = ChaiTheme.extended.border)

                SettingsLinkRow(
                    icon = Icons.Outlined.PrivacyTip,
                    title = "Privacy Policy",
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://chaipanchayat.com/privacy-policy"))
                        context.startActivity(intent)
                    }
                )

                HorizontalDivider(thickness = 0.5.dp, color = ChaiTheme.extended.border)

                SettingsLinkRow(
                    icon = Icons.Outlined.Info,
                    title = "App Version",
                    subtitle = "1.0.0 (Native Android Compose)",
                    onClick = {}
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Made with चाय · Chai Panchayat",
            color = ChaiTheme.extended.muted,
            fontFamily = InterFamily,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        color = ChaiTheme.extended.muted,
        fontFamily = InterFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun SettingsLinkRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ChaiTheme.extended.muted,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = InterFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    color = ChaiTheme.extended.muted,
                    fontFamily = InterFamily,
                    fontSize = 12.sp
                )
            }
        }
    }
}
