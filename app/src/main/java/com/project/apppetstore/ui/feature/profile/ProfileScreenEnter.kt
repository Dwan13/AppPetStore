package com.project.adopetshop.ui.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.adopetshop.R
import com.project.adopetshop.ui.components.SecondaryButton

@Composable
fun ProfileScreenEnter(
    userName: String,
    userEmail: String,
    onEditProfile: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF5C9639)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userName.take(2).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily(Font(R.font.roboto_bold)),
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.app_name),
                color = Color(0xFF5C9639),
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily(Font(R.font.roboto_bold)),
                fontSize = 18.sp
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Card usuario
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, shape = RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFFE5E7EB), shape = RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_user_round),
                        contentDescription = null,
                        tint = Color(0xFF5C9639),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = userName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = userEmail,
                            color = Color(0xFF6B7280),
                            fontSize = 14.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                SecondaryButton(
                    text = "Editar Perfil",
                    onClick = onEditProfile,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Card mascotas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, shape = RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFFE5E7EB), shape = RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "Mis mascotas",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min), // Hace que ambos hijos tengan el mismo alto
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight() // Ocupa todo el alto disponible
                            .background(Color(0xFFF3FCD6), shape = RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.Start) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF5C9639))
                            ) {}
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Bobby", fontWeight = FontWeight.Bold)
                            Text("Perro, 3 años", color = Color(0xFF6B7280), fontSize = 13.sp)
                        }
                    }
                    // Botón Agregar mascota
                    SecondaryButton(
                        onClick = { /* TODO: Acción agregar mascota */ },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight() // Ocupa todo el alto disponible
                            .padding(0.dp),
                        content = {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_plus),
                                    contentDescription = "Agregar",
                                    tint = Color(0xFFB0B0B0),
                                    modifier = Modifier.size(24.dp)
                                )
                                Text("Agregar", color = Color(0xFF6B7280), fontSize = 13.sp)
                            }
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Opciones
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, shape = RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFFE5E7EB), shape = RoundedCornerShape(16.dp))
                .padding(8.dp)
        ) {
            Column {
                SecondaryButton(
                    onClick = { /* TODO: Acción mis pedidos */ },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    content = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_shopping_bag),
                                contentDescription = null,
                                tint = Color(0xFF6B7280),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Mis pedidos", fontSize = 15.sp, modifier = Modifier.weight(1f))
                            Icon(
                                painter = painterResource(R.drawable.ic_chevron_right),
                                contentDescription = null,
                                tint = Color(0xFFB0B0B0),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                )
                SecondaryButton(
                    onClick = { /* TODO: Acción favoritos */ },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    content = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_heart),
                                contentDescription = null,
                                tint = Color(0xFF6B7280),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Favoritos", fontSize = 15.sp, modifier = Modifier.weight(1f))
                            Icon(
                                painter = painterResource(R.drawable.ic_chevron_right),
                                contentDescription = null,
                                tint = Color(0xFFB0B0B0),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                )
                SecondaryButton(
                    onClick = { /* TODO: Acción configuración */ },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    content = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_settings),
                                contentDescription = null,
                                tint = Color(0xFF6B7280),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Configuración", fontSize = 15.sp, modifier = Modifier.weight(1f))
                            Icon(
                                painter = painterResource(R.drawable.ic_chevron_right),
                                contentDescription = null,
                                tint = Color(0xFFB0B0B0),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Botón cerrar sesión
        SecondaryButton(
            text = "Cerrar sesión",
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, shape = RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFFFF5A5F), shape = RoundedCornerShape(8.dp)),
            content = {
                Icon(
                    painter = painterResource(R.drawable.ic_square_arrow_right_exit),
                    contentDescription = null,
                    tint = Color(0xFFFF5A5F),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar sesión", color = Color(0xFFFF5A5F))
            }
        )
    }
}
