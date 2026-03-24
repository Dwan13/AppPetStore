package com.project.adopetshop.ui.feature.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.adopetshop.R
import com.project.adopetshop.ui.components.PrimaryButton

@Composable
fun ProfileScreen(
    onOpenLoginSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        // Icono usuario
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFFF3F4F6)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_user_round),
                contentDescription = null,
                tint = Color(0xFFB0B0B0),
                modifier = Modifier.size(48.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.profile_create_title),
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily(Font(R.font.roboto_bold)),
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.profile_create_desc),
            color = Color(0xFF6B7280),
            fontFamily = FontFamily(Font(R.font.roboto_regular)),
            fontSize = 15.sp,
            modifier = Modifier.padding(horizontal = 16.dp),
            lineHeight = 20.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        PrimaryButton(
            text = stringResource(R.string.profile_login),
            onClick = {
                onOpenLoginSheet()
            },
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(44.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        // Imagen principal
        Image(
            painter = painterResource(R.drawable.img_home),
            contentDescription = null,
            modifier = Modifier
                .size(200.dp)
                .clip(MaterialTheme.shapes.medium)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.profile_welcome),
            color = Color(0xFF444444),
            fontFamily = FontFamily(Font(R.font.inria_sans_regular)),
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 16.dp),
            lineHeight = 18.sp
        )
    }
}
