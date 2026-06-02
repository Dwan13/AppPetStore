package com.project.apppetstore.ui.feature.adoption

import android.Manifest
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FiberManualRecord
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.project.apppetstore.ui.components.NoPermissionCard
import com.project.apppetstore.ui.components.RequestPermissionCard
import com.project.apppetstore.utils.createAudioRecordingFile
import java.io.File

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ChatAudioDialog(
    onDismiss: () -> Unit,
    onAudioRecorded: (Uri) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val audioPermission = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    val outputFile = remember { createAudioRecordingFile("APPPETSTORE_AUDIO_${System.currentTimeMillis()}", context) }
    var recorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var isRecording by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!audioPermission.status.isGranted) {
            audioPermission.launchPermissionRequest()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            recorder?.runCatching { stop() }
            recorder?.runCatching { reset() }
            recorder?.release()
            recorder = null
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Grabar audio",
                style = MaterialTheme.typography.titleMedium
            )

            when (val status = audioPermission.status) {
                PermissionStatus.Granted -> {
                    Text(
                        text = if (isRecording) {
                            "Grabando audio... toca detener para adjuntarlo."
                        } else {
                            "Toca grabar para capturar un audio del micrófono."
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Button(
                        onClick = {
                            if (isRecording) {
                                val stopped = stopAudioRecording(recorder)
                                recorder = null
                                isRecording = false
                                if (stopped && outputFile.exists() && outputFile.length() > 0L) {
                                    onAudioRecorded(outputFile.toUri())
                                    onDismiss()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "No se pudo guardar el audio.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                val newRecorder = startAudioRecording(context, outputFile)
                                if (newRecorder != null) {
                                    recorder = newRecorder
                                    isRecording = true
                                } else {
                                    Toast.makeText(
                                        context,
                                        "No se pudo iniciar la grabacion.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isRecording) Icons.Rounded.Stop else Icons.Rounded.FiberManualRecord,
                            contentDescription = null
                        )
                        Text(if (isRecording) "Detener y adjuntar" else "Grabar audio")
                    }
                }

                is PermissionStatus.Denied -> {
                    RequestPermissionCard(
                        title = "Permiso de microfono requerido",
                        message = if (status.shouldShowRationale) {
                            "Necesitamos el micrófono para grabar audios desde el chat."
                        } else {
                            "El permiso fue bloqueado. Puedes habilitarlo desde Ajustes para grabar audio."
                        },
                        actionLabel = if (status.shouldShowRationale) "Solicitar permiso" else "Entendido",
                        onRequestPermission = {
                            if (status.shouldShowRationale) {
                                audioPermission.launchPermissionRequest()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (!status.shouldShowRationale) {
                        NoPermissionCard(
                            message = "Sin permiso de micrófono no es posible grabar audio. Aún puedes seleccionar un audio existente.",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    }
}

private fun startAudioRecording(
    context: android.content.Context,
    outputFile: File
): MediaRecorder? {
    return runCatching {
        val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
        recorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(128_000)
            setAudioSamplingRate(44_100)
            setOutputFile(outputFile.absolutePath)
            prepare()
            start()
        }
    }.getOrNull()
}

private fun stopAudioRecording(recorder: MediaRecorder?): Boolean {
    return runCatching {
        recorder?.stop()
        recorder?.reset()
        recorder?.release()
        true
    }.getOrElse {
        recorder?.release()
        false
    }
}



