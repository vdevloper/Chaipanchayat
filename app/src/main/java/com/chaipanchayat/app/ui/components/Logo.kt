package com.chaipanchayat.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chaipanchayat.app.R
import com.chaipanchayat.app.ui.theme.ChaiBrandGradient
import com.chaipanchayat.app.ui.theme.ChaiSaffron
import com.chaipanchayat.app.ui.theme.InterFamily
import com.chaipanchayat.app.ui.theme.NotoSerifFamily

@Composable
fun Logo(
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    showWordmark: Boolean = true
) {
    Row(
        modifier = modifier.testTag("app-logo"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logo with radiant gradient ring
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .border(width = 1.5.dp, brush = ChaiBrandGradient, shape = CircleShape)
                .padding(2.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = "Chai Panchayat Logo",
                modifier = Modifier
                    .matchParentSize()
                    .clip(CircleShape)
            )
        }

        if (showWordmark) {
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "चाय पंचायत",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = NotoSerifFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 19.sp,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verified",
                        tint = ChaiSaffron,
                        modifier = Modifier.size(15.dp)
                    )
                }
                Text(
                    text = "CHAI PANCHAYAT DIGITAL",
                    color = ChaiSaffron,
                    fontFamily = InterFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                    letterSpacing = 0.6.sp
                )
            }
        }
    }
}
