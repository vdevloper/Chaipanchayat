package com.chaipanchayat.app.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chaipanchayat.app.ui.theme.ChaiBrandGradient
import com.chaipanchayat.app.ui.theme.ChaiCrimson
import com.chaipanchayat.app.ui.theme.ChaiGold
import com.chaipanchayat.app.ui.theme.ChaiSaffron
import com.chaipanchayat.app.ui.theme.ChaiTheme
import com.chaipanchayat.app.ui.theme.InterFamily
import com.chaipanchayat.app.ui.theme.NotoSerifFamily

/**
 * Google Play News & Magazines Policy Compliant Contact Page & In-App Declaration.
 *
 * Requirements fulfilled:
 * 1. Dedicated, easily findable in-app contact section.
 * 2. Official website link: https://chaipanchayat.com
 * 3. News publication email: chaipanchayat@gmail.com
 * 4. Technical / Developer contact: kvashudev934@gmail.com
 * 5. Editorial bureau & Headquarters address in Gorakhpur, UP.
 * 6. Grievance Redressal and Content Ownership Statement.
 */
@Composable
fun ContactUsDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(ChaiSaffron.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Email,
                        contentDescription = null,
                        tint = ChaiSaffron,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    Text(
                        text = "संपर्क एवं प्रकाशक विवरण",
                        fontFamily = NotoSerifFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Contact & Publisher Information",
                        fontFamily = InterFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.5.sp,
                        color = ChaiTheme.extended.textSecondary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(vertical = 4.dp)
            ) {
                // Official Publication Badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ChaiTheme.extended.surfaceSecondary,
                    border = BorderStroke(1.dp, ChaiTheme.extended.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Verified,
                            contentDescription = null,
                            tint = ChaiSaffron,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "चाय पंचायत (Chai Panchayat)",
                                fontFamily = NotoSerifFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "डिजिटल समाचार एवं विचार मंच (Digital News Publication)",
                                fontFamily = InterFamily,
                                fontSize = 11.sp,
                                color = ChaiTheme.extended.textSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Item 1: Official News Website
                ContactInfoCard(
                    icon = Icons.Outlined.Language,
                    title = "आधिकारिक वेबसाइट (Official Website)",
                    value = "chaipanchayat.com",
                    actionLabel = "वेबसाइट खोलें",
                    onClick = {
                        openUrl(context, "https://chaipanchayat.com")
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Item 2: News Publication Email
                ContactInfoCard(
                    icon = Icons.Outlined.AlternateEmail,
                    title = "संपादकीय एवं समाचार ईमेल (Editorial Email)",
                    value = "chaipanchayat@gmail.com",
                    actionLabel = "ईमेल भेजें",
                    onClick = {
                        sendEmail(context, "chaipanchayat@gmail.com", "Chai Panchayat News Inquiry")
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Item 3: Developer / Technical Contact
                ContactInfoCard(
                    icon = Icons.Outlined.Code,
                    title = "डेवलपर एवं तकनीकी संपर्क (App Developer)",
                    value = "kvashudev934@gmail.com",
                    actionLabel = "ईमेल भेजें",
                    onClick = {
                        sendEmail(context, "kvashudev934@gmail.com", "Chai Panchayat App Support")
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Item 4: Bureau & Office Location
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ChaiTheme.extended.surfaceSecondary,
                    border = BorderStroke(1.dp, ChaiTheme.extended.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = ChaiTheme.extended.brandText,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "संपादकीय कार्यालय (Editorial Office)",
                                fontFamily = InterFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "गोरखपुर, उत्तर प्रदेश, भारत (Gorakhpur, Uttar Pradesh, India - 273001)",
                                fontFamily = InterFamily,
                                fontSize = 12.sp,
                                color = ChaiTheme.extended.textSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Editorial Policy & Freshness Statement (News Policy compliance)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ChaiTheme.extended.surfaceSecondary,
                    border = BorderStroke(0.8.dp, ChaiTheme.extended.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Policy,
                                contentDescription = null,
                                tint = ChaiTheme.extended.brandText,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "पत्रकारिता नीति एवं सामग्री अद्यतन",
                                fontFamily = InterFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "चाय पंचायत पर प्रकाशित सभी समाचार ग्राउंड रिपोर्ट्स एवं अधिकृत स्रोतों पर आधारित हैं। सामग्री नियमित रूप से दैनिक अद्यतित की जाती है। किसी भी संशोधन, प्रतिक्रिया अथवा शिकायत निवारण हेतु कृपया हमारे संपादकीय ईमेल पर संपर्क करें।",
                            fontFamily = InterFamily,
                            fontSize = 11.5.sp,
                            color = ChaiTheme.extended.muted,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("contact-dialog-close")
            ) {
                Text(
                    text = "बंद करें (Close)",
                    color = ChaiSaffron,
                    fontFamily = InterFamily,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}

@Composable
private fun ContactInfoCard(
    icon: ImageVector,
    title: String,
    value: String,
    actionLabel: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = ChaiTheme.extended.surfaceSecondary,
        border = BorderStroke(1.dp, ChaiTheme.extended.border),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ChaiSaffron,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = title,
                        fontFamily = InterFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.5.sp,
                        color = ChaiTheme.extended.textSecondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = value,
                        fontFamily = InterFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(ChaiSaffron.copy(alpha = 0.12f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = actionLabel,
                        color = ChaiSaffron,
                        fontFamily = InterFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.5.sp
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                        contentDescription = null,
                        tint = ChaiSaffron,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }
        }
    }
}

private fun openUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "ब्राउज़र नहीं मिल सका: $url", Toast.LENGTH_SHORT).show()
    }
}

private fun sendEmail(context: Context, email: String, subject: String) {
    try {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
            putExtra(Intent.EXTRA_SUBJECT, subject)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "ईमेल ऐप नहीं मिल सका: $email", Toast.LENGTH_SHORT).show()
    }
}
