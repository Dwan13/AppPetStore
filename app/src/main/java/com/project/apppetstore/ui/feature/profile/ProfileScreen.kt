package com.project.apppetstore.ui.feature.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.project.apppetstore.R
import com.project.apppetstore.ui.components.PrimaryButton

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
        Spacer(modifier = Modifier.height(24.dp))
        // Icono usuario
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_user_round),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(48.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.profile_create_title),
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily(Font(R.font.roboto_bold)),
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.profile_create_desc),
            color = MaterialTheme.colorScheme.secondary,
            fontFamily = FontFamily(Font(R.font.roboto_regular)),
            fontSize = 15.sp,
            modifier = Modifier.padding(horizontal = 16.dp),
            lineHeight = 20.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
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
                .clip(RoundedCornerShape(16.dp))
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.profile_welcome),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            fontFamily = FontFamily(Font(R.font.inria_sans_bold_italic)),
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 16.dp),
            lineHeight = 18.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}