package com.project.apppetstore.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.apppetstore.R
import com.project.apppetstore.data.model.AttachmentType
import com.project.apppetstore.data.model.ChatAttachment
import com.project.apppetstore.data.model.ChatMessage

@Composable
fun ChatSection(
    messages: List<ChatMessage>,
    currentInput: String,
    pendingAttachment: ChatAttachment?,
    onInputChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onRemovePendingAttachment: () -> Unit,
    onTakePhoto: () -> Unit,
    onPickImage: () -> Unit,
    onRecordVideo: () -> Unit,
    onPickVideo: () -> Unit,
    onRecordAudio: () -> Unit,
    onPickAudio: () -> Unit
) {
    var showAttachMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.onSecondary, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            Text(
                "Chat informativo",
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier.heightIn(max = 180.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    ChatBubble(message)
                }
            }

            pendingAttachment?.let { attachment ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = when (attachment.type) {
                            AttachmentType.IMAGE -> "Imagen lista para enviar"
                            AttachmentType.VIDEO -> "Video listo para enviar"
                            AttachmentType.AUDIO -> "Audio listo para enviar"
                        },
                        style = MaterialTheme.typography.labelMedium
                    )
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Quitar adjunto",
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onRemovePendingAttachment() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable { showAttachMenu = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Adjuntar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    DropdownMenu(
                        expanded = showAttachMenu,
                        onDismissRequest = { showAttachMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Tomar foto") },
                            onClick = {
                                showAttachMenu = false
                                onTakePhoto()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Seleccionar imagen") },
                            onClick = {
                                showAttachMenu = false
                                onPickImage()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Grabar video") },
                            onClick = {
                                showAttachMenu = false
                                onRecordVideo()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Seleccionar video") },
                            onClick = {
                                showAttachMenu = false
                                onPickVideo()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Grabar audio") },
                            onClick = {
                                showAttachMenu = false
                                onRecordAudio()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Seleccionar audio") },
                            onClick = {
                                showAttachMenu = false
                                onPickAudio()
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedTextField(
                    value = currentInput,
                    onValueChange = onInputChange,
                    placeholder = { Text("Escribe tu pregunta...") },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable { onSendMessage() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_send),
                        contentDescription = "Enviar",
                        tint = Color.Black
                    )
                }
            }
        }
    }
}