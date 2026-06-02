package com.project.apppetstore.ui.feature.adoption

import android.net.Uri
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.WriteBatch
import com.google.firebase.storage.FirebaseStorage
import com.project.apppetstore.data.model.AttachmentType
import com.project.apppetstore.data.model.ChatAttachment
import com.project.apppetstore.data.model.ChatMessage
import com.project.apppetstore.data.model.Pet
import com.project.apppetstore.data.repository.FirestorePetsRepository
import com.project.apppetstore.data.repository.MockPetShopRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class AdoptionUiState(
    val pets              : List<Pet>         = emptyList(),
    val messages          : List<ChatMessage> = emptyList(),
    val currentInput      : String            = "",
    val pendingAttachment : ChatAttachment?   = null,
    val isUploading       : Boolean           = false,
    val isLoadingMessages : Boolean           = true,
    val isLoadingPets     : Boolean           = true,
    /** UID del dueño si el chat es con un usuario real; null = auto-reply del sistema. */
    val currentPetOwnerUid: String?           = null,
    /** true cuando el dueño abre el chat directamente desde una notificación. */
    val isOwnerViewingChat: Boolean           = false
)

class AdoptionViewModel(app: Application) : AndroidViewModel(app) {

    private val auth       = FirebaseAuth.getInstance()
    private val firestore  = FirebaseFirestore.getInstance()
    private val storageRef = FirebaseStorage.getInstance().reference

    var uiState by mutableStateOf(AdoptionUiState())
        private set

    /** Catálogo admin — carga una vez (cambia raramente). */
    private var adminPets: List<Pet> = emptyList()

    /** Listener en tiempo real sobre adoptionListings. */
    private var listingsListener: ListenerRegistration? = null

    private var currentChatId: String? = null
    private var messagesListener: ListenerRegistration? = null

    init {
        loadPets()
    }

    // ── Carga del catálogo admin + inicia listener de adoptionListings ─────────

    fun loadPets() {
        uiState = uiState.copy(isLoadingPets = true)
        viewModelScope.launch {
            val ctx = getApplication<Application>()
            adminPets = try {
                FirestorePetsRepository.getPets(ctx)
            } catch (_: Exception) {
                MockPetShopRepository.getPets()
            }
            // Merge inicial: admin + lo que ya tenemos de listings (puede estar vacío)
            mergePets(emptyList())
            // Ahora activa el listener en tiempo real para adoptionListings
            startListingsListener()
        }
    }

    /**
     * Listener en tiempo real sobre "adoptionListings".
     * Cada vez que un usuario publica o retira una mascota, el catálogo se actualiza
     * automáticamente en todos los dispositivos sin necesidad de reiniciar la app.
     */
    private fun startListingsListener() {
        listingsListener?.remove()
        listingsListener = firestore
            .collection(FirestorePetsRepository.ADOPTION_COL)
            .addSnapshotListener { snap, error ->
                if (error != null) return@addSnapshotListener

                val userPets = snap?.documents?.mapNotNull { doc ->
                    runCatching {
                        Pet(
                            id           = doc.id,
                            name         = doc.getString("name")        ?: return@runCatching null,
                            age          = doc.getString("age")         ?: "",
                            breed        = doc.getString("breed")       ?: "",
                            gender       = doc.getString("gender")      ?: "",
                            size         = doc.getString("size")        ?: "",
                            health       = doc.getString("health")      ?: "",
                            vaccines     = doc.getString("vaccines")    ?: "",
                            personality  = doc.getString("personality") ?: "",
                            requirements = doc.getString("requirements")?: "",
                            imageUrl     = doc.getString("imageUrl"),
                            imageRes     = null,
                            ownerUid     = doc.getString("ownerUid")
                        )
                    }.getOrNull()
                } ?: emptyList()

                mergePets(userPets)
            }
    }

    /** Combina el catálogo admin con las mascotas de usuarios, sin duplicados. */
    private fun mergePets(userListings: List<Pet>) {
        val adminIds = adminPets.map { it.id }.toSet()
        val merged   = adminPets + userListings.filter { it.id !in adminIds }
        uiState = uiState.copy(pets = merged, isLoadingPets = false)
    }

    override fun onCleared() {
        super.onCleared()
        messagesListener?.remove()
        listingsListener?.remove()
    }

    // ── Abrir chat directamente por chatId (dueño desde notificación) ──────

    /**
     * El dueño de la mascota abre el chat de adopción directamente desde
     * una notificación. Usa el chatId ya formado (intUid_ownerUid_petId).
     */
    fun openChatDirect(chatId: String) {
        if (chatId.isBlank()) return   // ← guardia: chatId vacío crashea Firestore
        val shouldReload = chatId != currentChatId
        currentChatId = chatId
        uiState = uiState.copy(
            currentPetOwnerUid = null,   // él ES el dueño, no notifica a nadie
            isOwnerViewingChat = true,
            isLoadingMessages  = true
        )
        if (shouldReload || messagesListener == null) {
            listenToMessages(chatId)
        }
    }

    // ── Seleccionar mascota y abrir el chat correcto ────────────────────────

    fun setCurrentPet(petId: String?) {
        val uid = auth.currentUser?.uid ?: return

        if (petId == null) {
            val chatId = "${uid}_general"
            if (chatId == currentChatId) return
            currentChatId = chatId
            uiState = uiState.copy(
                currentPetOwnerUid = null,
                isOwnerViewingChat = false,
                isLoadingMessages = true
            )
            listenToMessages(chatId)
            return
        }

        val pet      = uiState.pets.find { it.id == petId }
        val ownerUid = pet?.ownerUid

        // Si la mascota tiene dueño y ese dueño NO soy yo → chat bidireccional
        val chatId = if (ownerUid != null && ownerUid != uid) {
            "${uid}_${ownerUid}_${petId}"
        } else {
            "${uid}_${petId}"
        }

        if (chatId == currentChatId) return
        currentChatId = chatId
        uiState = uiState.copy(
            currentPetOwnerUid = ownerUid?.takeIf { it != uid },
            isOwnerViewingChat = false,
            isLoadingMessages = true
        )
        listenToMessages(chatId)
    }

    // ── Escuchar mensajes en tiempo real ────────────────────────────────────

    private fun listenToMessages(chatId: String) {
        if (chatId.isBlank()) return   // ← guardia: evita crash por path inválido en Firestore
        messagesListener?.remove()
        val currentUid = auth.currentUser?.uid ?: ""
        messagesListener = firestore
            .collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                val messages = snapshot?.documents?.mapNotNull { doc ->
                    val rawType    = doc.getString("attachmentType")
                    val attachType = rawType?.let {
                        runCatching { AttachmentType.valueOf(it) }.getOrNull()
                    }
                    val attachUrl  = doc.getString("attachmentUrl")
                    val senderId   = doc.getString("senderId") ?: ""

                    ChatMessage(
                        id         = doc.id,
                        message    = doc.getString("message") ?: "",
                        isUser     = senderId == currentUid,
                        attachment = if (attachType != null && !attachUrl.isNullOrBlank()) {
                            ChatAttachment(type = attachType, uri = attachUrl)
                        } else null,
                        timestamp  = doc.getLong("timestamp") ?: 0L,
                        senderId   = senderId
                    )
                } ?: emptyList()

                uiState = uiState.copy(messages = messages, isLoadingMessages = false)
            }
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
        val uid        = auth.currentUser?.uid ?: return
        val chatId     = currentChatId         ?: return
        val text       = uiState.currentInput.trim()
        val attachment = uiState.pendingAttachment
        val ownerUid   = uiState.currentPetOwnerUid

        if (text.isEmpty() && attachment == null) return

        uiState = uiState.copy(
            currentInput      = "",
            pendingAttachment = null,
            isUploading       = attachment != null
        )

        val isOwnerChat = uiState.isOwnerViewingChat
        val previewText = text.ifBlank { "[adjunto]" }
        if (attachment != null) {
            upsertChatHeader(chatId, uid, previewText)
            uploadAttachmentAndSave(uid, chatId, text, attachment, ownerUid, isOwnerChat)
        } else {
            upsertChatHeader(chatId, uid, previewText)
            saveMessage(chatId, uid, text, null)
            when {
                ownerUid   != null -> notifyPetOwner(ownerUid, chatId, text) // interesado → notifica al dueño
                !isOwnerChat       -> triggerAutoReply(chatId)                // mascota admin → auto-reply
                else               -> resolveOwnerRequest(chatId)              // dueño responde → cierra solicitud
            }
        }
    }

    // ── Subir adjunto ───────────────────────────────────────────────────────

    private fun uploadAttachmentAndSave(
        uid: String, chatId: String, text: String,
        attachment: ChatAttachment, ownerUid: String?, isOwnerChat: Boolean
    ) {
        val ext = when (attachment.type) {
            AttachmentType.IMAGE -> "jpg"
            AttachmentType.VIDEO -> "mp4"
            AttachmentType.AUDIO -> "aac"
        }
        val ref = storageRef.child("chats/$chatId/${System.currentTimeMillis()}.$ext")

        ref.putFile(Uri.parse(attachment.uri))
            .continueWithTask { task ->
                if (!task.isSuccessful) throw (task.exception ?: Exception("Upload error"))
                ref.downloadUrl
            }
            .addOnSuccessListener { url ->
                val uploaded = ChatAttachment(type = attachment.type, uri = url.toString())
                saveMessage(chatId, uid, text, uploaded)
                if (ownerUid != null) notifyPetOwner(ownerUid, chatId, text.ifBlank { "[adjunto]" })
                else if (!isOwnerChat) triggerAutoReply(chatId)
                else                    resolveOwnerRequest(chatId)
                uiState = uiState.copy(isUploading = false)
            }
            .addOnFailureListener {
                uiState = uiState.copy(isUploading = false)
            }
    }

    // ── Guardar mensaje ─────────────────────────────────────────────────────

    private fun saveMessage(chatId: String, uid: String, text: String, attachment: ChatAttachment?) {
        firestore.collection("chats").document(chatId).collection("messages")
            .add(hashMapOf(
                "message"        to text,
                "isUser"         to true,
                "senderId"       to uid,
                "timestamp"      to System.currentTimeMillis(),
                "attachmentType" to attachment?.type?.name,
                "attachmentUrl"  to attachment?.uri
            ))
    }

    fun deleteMessage(messageId: String) {
        val uid = auth.currentUser?.uid ?: return
        val chatId = currentChatId ?: return
        if (messageId.isBlank()) return

        val ref = firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .document(messageId)

        // Validación de autor en cliente para evitar borrados accidentales.
        ref.get().addOnSuccessListener { snap ->
            if (!snap.exists()) return@addOnSuccessListener
            val senderId = snap.getString("senderId") ?: return@addOnSuccessListener
            if (senderId != uid) return@addOnSuccessListener
            ref.delete()
        }
    }

    // Mantiene un resumen del chat para listarlo rápido en Mis Mascotas.
    private fun upsertChatHeader(chatId: String, senderUid: String, preview: String) {
        val now   = System.currentTimeMillis()
        val parts = chatId.split("_", limit = 3)
        val isPetChat = parts.size == 3 && parts[2].isNotBlank()
        val petId     = if (isPetChat) parts[2] else null

        val participants = if (isPetChat) {
            listOf(parts[0], parts[1]).distinct()
        } else {
            listOf(senderUid)
        }

        val petName = petId?.let { pid -> uiState.pets.find { it.id == pid }?.name }
        val title = when {
            isPetChat && !petName.isNullOrBlank() -> "Solicitud para $petName"
            isPetChat -> "Solicitud de adopción"
            else -> "Chat general"
        }

        firestore.collection("chats").document(chatId)
            .set(
                mapOf(
                    "chatId" to chatId,
                    "chatType" to if (isPetChat) "pet" else "general",
                    "participants" to participants,
                    "petId" to petId,
                    "title" to title,
                    "lastMessage" to preview.take(120),
                    "lastTimestamp" to now,
                    "updatedAt" to now
                ),
                SetOptions.merge()
            )
    }

    // ── Notificar al dueño (solo primera vez) ───────────────────────────────

    private fun notifyPetOwner(ownerUid: String, chatId: String, preview: String) {
        val petId   = chatId.substringAfterLast("_")
        val petName = uiState.pets.find { it.id == petId }?.name ?: "tu mascota"

        firestore.collection("users").document(ownerUid)
            .collection("notifications")
            .add(mapOf(
                "title"     to "Alguien está interesado en $petName",
                "message"   to "\"${preview.take(80)}\"",
                "type"      to "pet",
                "chatId"    to chatId,
                "senderId"  to (auth.currentUser?.uid ?: ""),
                "resolved"  to false,
                "timestamp" to System.currentTimeMillis()
            ))
    }

    // Cuando el dueño responde, las solicitudes de ese chat se consideran atendidas.
    private fun resolveOwnerRequest(chatId: String) {
        val ownerUid = auth.currentUser?.uid ?: return
        val notificationsRef = firestore.collection("users").document(ownerUid)
            .collection("notifications")

        notificationsRef
            .whereEqualTo("type", "pet")
            .whereEqualTo("chatId", chatId)
            .get()
            .addOnSuccessListener { snap ->
                if (snap.isEmpty) return@addOnSuccessListener
                val now = System.currentTimeMillis()
                val batch: WriteBatch = firestore.batch()
                snap.documents.forEach { doc ->
                    batch.update(doc.reference, mapOf(
                        "resolved" to true,
                        "resolvedAt" to now
                    ))
                }
                batch.commit()
            }
    }

    // ── Auto-reply del sistema ──────────────────────────────────────────────

    private fun triggerAutoReply(chatId: String) {
        viewModelScope.launch {
            delay(900)
            firestore.collection("chats").document(chatId).collection("messages")
                .add(hashMapOf(
                    "message"        to "¡Gracias por tu mensaje! Pronto te contactaremos.",
                    "isUser"         to false,
                    "senderId"       to "system",
                    "timestamp"      to System.currentTimeMillis(),
                    "attachmentType" to null,
                    "attachmentUrl"  to null
                ))
        }
    }
}
