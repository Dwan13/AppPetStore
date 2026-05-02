package com.project.apppetstore.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.project.apppetstore.data.model.AttachmentType
import com.project.apppetstore.data.model.ChatMessage

@Composable
fun ChatBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 260.dp),
            shape = RoundedCornerShape(16.dp),
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = if (message.isUser) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.primaryContainer
                }
            )
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                message.attachment?.let { attachment ->
                    when (attachment.type) {
                        AttachmentType.IMAGE -> {
                            AsyncImage(
                                model = attachment.uri,
                                contentDescription = "Imagen adjunta",
                                modifier = Modifier
                                    .width(180.dp)
                                    .size(120.dp)
                            )
                        }
                        AttachmentType.VIDEO -> {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Rounded.Videocam, contentDescription = null)
                                Text("Video adjunto")
                            }
                        }
                        AttachmentType.AUDIO -> {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Rounded.Mic, contentDescription = null)
                                Text("Audio adjunto")
                            }
                        }
                    }
                }

                if (message.message.isNotBlank()) {
                    Text(text = message.message)
                }
            }
        }
    }
}