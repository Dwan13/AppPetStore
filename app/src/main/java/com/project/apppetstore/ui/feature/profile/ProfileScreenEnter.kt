package com.project.apppetstore.ui.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.project.apppetstore.R
import com.project.apppetstore.data.model.UserPet
import com.project.apppetstore.ui.components.SecondaryButton

@Composable
fun ProfileScreenEnter(
    userName           : String,
    userEmail          : String,
    profilePhotoUri    : String?,
    pets               : List<UserPet>,
    onTakeProfilePhoto : () -> Unit,
    onPickProfilePhoto : () -> Unit,
    onMisMascotasClick : () -> Unit,
    onOrdersClick      : () -> Unit,
    onFavoritesClick   : () -> Unit,
    onSettingsClick    : () -> Unit,
    onLogout           : () -> Unit,
    modifier           : Modifier = Modifier
) {
    var showProfilePhotoOptions by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // ── Card usuario ──────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.large)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape = MaterialTheme.shapes.large)
                .padding(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (profilePhotoUri.isNullOrBlank()) {
                        Icon(
                            painter            = painterResource(R.drawable.ic_user_round),
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.primary,
                            modifier           = Modifier.size(42.dp)
                        )
                    } else {
                        AsyncImage(
                            model              = profilePhotoUri,
                            contentDescription = "Foto de perfil",
                            modifier           = Modifier.size(52.dp).clip(CircleShape)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(userName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(userEmail, color = MaterialTheme.colorScheme.secondary, fontSize = 14.sp)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                SecondaryButton(
                    text     = "Editar foto de perfil",
                    onClick  = { showProfilePhotoOptions = !showProfilePhotoOptions },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                )
                if (showProfilePhotoOptions) {
                    Row(
                        modifier              = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SecondaryButton(
                            text     = "Tomar foto",
                            onClick  = { showProfilePhotoOptions = false; onTakeProfilePhoto() },
                            modifier = Modifier.weight(1f)
                        )
                        SecondaryButton(
                            text     = "Seleccionar foto",
                            onClick  = { showProfilePhotoOptions = false; onPickProfilePhoto() },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Opciones de cuenta ────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.large)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape = MaterialTheme.shapes.large)
                .padding(8.dp)
        ) {
            Column {
                // Mis mascotas (con badge de cantidad)
                SecondaryButton(
                    onClick  = onMisMascotasClick,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    content  = {
                        Row(
                            modifier          = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector        = Icons.Rounded.Pets,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.primary,
                                modifier           = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Mis mascotas", fontSize = 15.sp, modifier = Modifier.weight(1f))
                            if (pets.isNotEmpty()) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor   = MaterialTheme.colorScheme.primary
                                ) {
                                    Text("${pets.size}", style = MaterialTheme.typography.labelSmall)
                                }
                                Spacer(Modifier.width(4.dp))
                            }
                            Icon(
                                painter            = painterResource(R.drawable.ic_chevron_right),
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.primary,
                                modifier           = Modifier.size(20.dp)
                            )
                        }
                    }
                )

                SecondaryButton(
                    onClick  = onOrdersClick,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    content  = {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter            = painterResource(R.drawable.ic_shopping_bag),
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.primary,
                                modifier           = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Mis pedidos", fontSize = 15.sp, modifier = Modifier.weight(1f))
                            Icon(
                                painter            = painterResource(R.drawable.ic_chevron_right),
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.primary,
                                modifier           = Modifier.size(20.dp)
                            )
                        }
                    }
                )

                SecondaryButton(
                    onClick  = onFavoritesClick,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    content  = {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter            = painterResource(R.drawable.ic_heart),
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.primary,
                                modifier           = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Favoritos", fontSize = 15.sp, modifier = Modifier.weight(1f))
                            Icon(
                                painter            = painterResource(R.drawable.ic_chevron_right),
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.primary,
                                modifier           = Modifier.size(20.dp)
                            )
                        }
                    }
                )

                SecondaryButton(
                    onClick  = onSettingsClick,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    content  = {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter            = painterResource(R.drawable.ic_settings),
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.primary,
                                modifier           = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Configuración", fontSize = 15.sp, modifier = Modifier.weight(1f))
                            Icon(
                                painter            = painterResource(R.drawable.ic_chevron_right),
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.primary,
                                modifier           = Modifier.size(20.dp)
                            )
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        SecondaryButton(
            text     = "Cerrar sesión",
            onClick  = onLogout,
            modifier = Modifier.fillMaxWidth(),
            content  = {
                Icon(
                    painter            = painterResource(R.drawable.ic_square_arrow_right_exit),
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.error,
                    modifier           = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar sesión", color = MaterialTheme.colorScheme.error)
            }
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}
