package com.project.apppetstore.ui.feature.adoption

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import com.project.apppetstore.data.model.AttachmentType
import com.project.apppetstore.data.model.ChatAttachment
import com.project.apppetstore.data.model.ChatMessage
import com.project.apppetstore.data.model.Pet
import android.app.Application
import com.project.apppetstore.data.repository.FirestorePetsRepository
import com.project.apppetstore.data.repository.MockPetShopRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class AdoptionUiState(
    val pets: List<Pet> = emptyList(),
    val messages: List<ChatMessage> = emptyList(),
    val currentInput: String = "",
    val pendingAttachment: ChatAttachment? = null,
    val isUploading: Boolean = false,
    val isLoadingMessages: Boolean = true   // true hasta que llega el 1er snapshot de Firestore
)

class AdoptionViewModel(app: Application) : AndroidViewModel(app) {

    private val auth       = FirebaseAuth.getInstance()
    private val firestore  = FirebaseFirestore.getInstance()
    private val storageRef = FirebaseStorage.getInstance().reference

    var uiState by mutableStateOf(AdoptionUiState())
        private set

    init {
        viewModelScope.launch {
            val ctx = getApplication<Application>()
            val pets = try {
                FirestorePetsRepository.getPets(ctx)
            } catch (_: Exception) {
                MockPetShopRepository.getPets()
            }
            uiState = uiState.copy(pets = pets)
        }
    }

    private var currentChatId: String? = null
    private var messagesListener: ListenerRegistration? = null

    // ── Inicializar el chat para una mascota (o chat general) ───────────────

    fun setCurrentPet(petId: String?) {
        val uid = auth.currentUser?.uid ?: return
        val chatId = if (petId != null) "${uid}_${petId}" else "${uid}_general"
        if (chatId == currentChatId) return   // ya está escuchando este chat
        currentChatId = chatId
        listenToMessages(chatId)
    }

    // ── Escuchar mensajes en tiempo real desde Firestore ────────────────────

    private fun listenToMessages(chatId: String) {
        messagesListener?.remove()
        messagesListener = firestore
            .collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                val messages = snapshot?.documents?.mapNotNull { doc ->
                    val rawType = doc.getString("attachmentType")
                    val attachmentType = rawType?.let {
                        runCatching { AttachmentType.valueOf(it) }.getOrNull()
                    }
                    val attachmentUrl = doc.getString("attachmentUrl")

                    ChatMessage(
                        id         = doc.id,
                        message    = doc.getString("message") ?: "",
                        isUser     = doc.getBoolean("isUser") ?: true,
                        attachment = if (attachmentType != null && !attachmentUrl.isNullOrBlank()) {
                            ChatAttachment(type = attachmentType, uri = attachmentUrl)
                        } else null,
                        timestamp  = doc.getLong("timestamp") ?: 0L,
                        senderId   = doc.getString("senderId") ?: ""
                    )
                } ?: emptyList()

                uiState = uiState.copy(
                    messages = messages,
                    isLoadingMessages = false   // primer snapshot recibido → dejar de mostrar skeleton
                )
            }
    }

    override fun onCleared() {
        super.onCleared()
        messagesListener?.remove()
    }

    // ── Input y adjuntos ────────────────────────────────────────────────────

    fun onInputChange(input: String) {
        uiState = uiState.copy(currentInput = input)
    }

    fun attachMedia(type: AttachmentType, uri: String) {
        uiState = uiState.copy(pendingAttachment = ChatAttachment(type = type, uri = uri))
    }

    fun removePendingAttachment() {
        uiState = uiState.copy(pendingAttachment = null)
    }

    // ── Enviar mensaje ──────────────────────────────────────────────────────

    fun sendMessage() {
        val uid    = auth.currentUser?.uid ?: return
        val chatId = currentChatId         ?: return
        val text   = uiState.currentInput.trim()
        val attachment = uiState.pendingAttachment

        if (text.isEmpty() && attachment == null) return

        // Limpiar input inmediatamente para buena UX
        uiState = uiState.copy(
            currentInput    = "",
            pendingAttachment = null,
            isUploading     = attachment != null
        )

        if (attachment != null) {
            uploadAttachmentAndSave(uid, chatId, text, attachment)
        } else {
            saveMessage(chatId, uid, text, null)
            triggerAutoReply(chatId)
        }
    }

    // ── Subir adjunto a Storage y luego guardar en Firestore ────────────────

    private fun uploadAttachmentAndSave(
        uid: String,
        chatId: String,
        text: String,
        attachment: ChatAttachment
    ) {
        val extension = when (attachment.type) {
            AttachmentType.IMAGE -> "jpg"
            AttachmentType.VIDEO -> "mp4"
            AttachmentType.AUDIO -> "aac"
        }
        val fileName = "${System.currentTimeMillis()}.$extension"
        val ref = storageRef.child("chats/$chatId/$fileName")

        ref.putFile(Uri.parse(attachment.uri))
            .continueWithTask { task ->
                if (!task.isSuccessful) throw (task.exception ?: Exception("Error al subir archivo"))
                ref.downloadUrl
            }
            .addOnSuccessListener { downloadUri ->
                val uploaded = ChatAttachment(type = attachment.type, uri = downloadUri.toString())
                saveMessage(chatId, uid, text, uploaded)
                triggerAutoReply(chatId)
                uiState = uiState.copy(isUploading = false)
            }
            .addOnFailureListener {
                uiState = uiState.copy(isUploading = false)
            }
    }

    // ── Guardar mensaje en Firestore ────────────────────────────────────────

    private fun saveMessage(
        chatId: String,
        uid: String,
        text: String,
        attachment: ChatAttachment?
    ) {
        val data = hashMapOf(
            "message"        to text,
            "isUser"         to true,
            "senderId"       to uid,
            "timestamp"      to System.currentTimeMillis(),
            "attachmentType" to attachment?.type?.name,
            "attachmentUrl"  to attachment?.uri
        )
        firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .add(data)
    }

    // ── Respuesta automática simulada (también se persiste) ─────────────────

    private fun triggerAutoReply(chatId: String) {
        viewModelScope.launch {
            delay(900)
            val replyData = hashMapOf(
                "message"        to "¡Gracias por tu mensaje! Pronto te contactaremos para la adopción.",
                "isUser"         to false,
                "senderId"       to "system",
                "timestamp"      to System.currentTimeMillis(),
                "attachmentType" to null,
                "attachmentUrl"  to null
            )
            firestore.collection("chats")
                .document(chatId)
                .collection("messages")
                .add(replyData)
        }
    }
}
