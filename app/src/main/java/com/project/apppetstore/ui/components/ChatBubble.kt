package com.project.apppetstore.ui.components

import android.media.MediaPlayer
import android.net.Uri
import android.widget.Toast
import android.widget.VideoView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil3.compose.AsyncImage
import com.project.apppetstore.data.model.AttachmentType
import com.project.apppetstore.data.model.ChatMessage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

@Composable
fun ChatBubble(
    message: ChatMessage,
    onDeleteMessage: (() -> Unit)? = null
) {
    val isUser = message.isUser

    // Forma de burbuja: esquina inferior redondeada según quien envía
    val bubbleShape = if (isUser) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = bubbleShape,
            color = if (isUser)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = if (isUser) 0.dp else 1.dp,
            modifier = Modifier.widthIn(max = 260.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                // Adjunto
                message.attachment?.let { attachment ->
                    when (attachment.type) {
                        AttachmentType.IMAGE -> {
                            AsyncImage(
                                model = attachment.uri,
                                contentDescription = "Imagen adjunta",
                                modifier = Modifier
                                    .width(200.dp)
                                    .height(140.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            if (message.message.isNotBlank()) Spacer(modifier = Modifier.height(6.dp))
                        }
                        AttachmentType.VIDEO -> {
                            VideoAttachmentPlayer(
                                videoUri = attachment.uri,
                                isUser = isUser
                            )
                            if (message.message.isNotBlank()) Spacer(modifier = Modifier.height(6.dp))
                        }
                        AttachmentType.AUDIO -> {
                            AudioAttachmentPlayer(
                                audioUri = attachment.uri,
                                isUser = isUser
                            )
                            if (message.message.isNotBlank()) Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }

                // Texto
                if (message.message.isNotBlank()) {
                    Text(
                        text = message.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isUser)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (onDeleteMessage != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    TextButton(
                        onClick = onDeleteMessage,
                        modifier = Modifier.align(Alignment.End),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = "Eliminar mensaje",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Eliminar", style = MaterialTheme.typography.labelSmall)
                    }
                }

                // Hora
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = timeFormatter.format(Date(message.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isUser)
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
private fun VideoAttachmentPlayer(
    videoUri: String,
    isUser: Boolean
) {
    val context = LocalContext.current
    var videoView by remember(videoUri) { mutableStateOf<VideoView?>(null) }
    var isPrepared by remember(videoUri) { mutableStateOf(false) }
    var isPlaying by remember(videoUri) { mutableStateOf(false) }
    var shouldAutoPlay by remember(videoUri) { mutableStateOf(false) }

    DisposableEffect(videoUri) {
        onDispose {
            videoView?.stopPlayback()
            videoView = null
        }
    }

    Surface(
        color = if (isUser)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(8.dp)),
                factory = { androidContext ->
                    VideoView(androidContext).apply {
                        setVideoURI(Uri.parse(videoUri))
                        setOnPreparedListener { mediaPlayer ->
                            isPrepared = true
                            mediaPlayer.isLooping = false
                            if (shouldAutoPlay) {
                                start()
                                isPlaying = true
                                shouldAutoPlay = false
                            }
                        }
                        setOnCompletionListener {
                            isPlaying = false
                            shouldAutoPlay = false
                            seekTo(0)
                        }
                        setOnErrorListener { _, _, _ ->
                            isPrepared = false
                            isPlaying = false
                            shouldAutoPlay = false
                            Toast.makeText(
                                context,
                                "No se pudo reproducir el video.",
                                Toast.LENGTH_SHORT
                            ).show()
                            true
                        }
                        videoView = this
                    }
                },
                update = { view ->
                    videoView = view
                }
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledIconButton(
                    onClick = {
                        val currentView = videoView ?: return@FilledIconButton
                        when {
                            isPlaying -> {
                                currentView.pause()
                                isPlaying = false
                                shouldAutoPlay = false
                            }

                            isPrepared -> {
                                currentView.start()
                                isPlaying = true
                            }

                            else -> {
                                shouldAutoPlay = true
                                runCatching { currentView.start() }
                                    .onFailure {
                                        Toast.makeText(
                                            context,
                                            "Cargando video...",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (isPlaying) "Pausar video" else "Reproducir video"
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Videocam,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = when {
                                isPlaying -> "Reproduciendo video"
                                isPrepared -> "Video listo"
                                else -> "Cargando video..."
                            },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Text(
                        text = if (isPlaying) "Toca para pausar" else "Toca reproducir para verlo",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun AudioAttachmentPlayer(
    audioUri: String,
    isUser: Boolean
) {
    val context = LocalContext.current
    var mediaPlayer by remember(audioUri) { mutableStateOf<MediaPlayer?>(null) }
    var isPlaying by remember(audioUri) { mutableStateOf(false) }
    var isPreparing by remember(audioUri) { mutableStateOf(false) }

    DisposableEffect(audioUri) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    Surface(
        color = if (isUser)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledIconButton(
                onClick = {
                    val currentPlayer = mediaPlayer
                    when {
                        isPreparing -> Unit
                        currentPlayer == null -> {
                            val player = MediaPlayer()
                            mediaPlayer = player
                            isPreparing = true
                            runCatching {
                                player.setDataSource(context, Uri.parse(audioUri))
                                player.setOnPreparedListener {
                                    isPreparing = false
                                    isPlaying = true
                                    it.start()
                                }
                                player.setOnCompletionListener {
                                    isPlaying = false
                                    isPreparing = false
                                }
                                player.setOnErrorListener { mp, _, _ ->
                                    mp.release()
                                    mediaPlayer = null
                                    isPlaying = false
                                    isPreparing = false
                                    Toast.makeText(
                                        context,
                                        "No se pudo reproducir el audio.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    true
                                }
                                player.prepareAsync()
                            }.onFailure {
                                player.release()
                                mediaPlayer = null
                                isPreparing = false
                                isPlaying = false
                                Toast.makeText(
                                    context,
                                    "No se pudo abrir el audio.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        currentPlayer.isPlaying -> {
                            currentPlayer.pause()
                            isPlaying = false
                        }

                        else -> {
                            currentPlayer.start()
                            isPlaying = true
                        }
                    }
                },
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = if (isPlaying) "Pausar audio" else "Reproducir audio"
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Rounded.Mic,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = when {
                            isPreparing -> "Cargando audio..."
                            isPlaying -> "Reproduciendo audio"
                            else -> "Audio adjunto"
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text(
                    text = if (isPlaying) "Toca para pausar" else "Toca reproducir para escucharlo",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

