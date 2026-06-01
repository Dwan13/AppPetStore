package com.project.apppetstore.ui.feature.notifications

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.project.apppetstore.data.model.AppNotification
import com.project.apppetstore.data.repository.SettingsRepository

data class NotificationsUiState(
    val notifications : List<AppNotification> = emptyList(),
    val unreadCount   : Int                   = 0,
    val lastSeenTs    : Long                  = 0L,
    val isLoading     : Boolean               = true
)

class NotificationsViewModel(app: Application) : AndroidViewModel(app) {

    private val auth         = FirebaseAuth.getInstance()
    private val firestore    = FirebaseFirestore.getInstance()
    private val settings     = SettingsRepository.getInstance(app)
    private var listenerReg  : ListenerRegistration? = null
    private val authListener : FirebaseAuth.AuthStateListener

    var uiState by mutableStateOf(NotificationsUiState())
        private set

    init {
        // ── Reaccionar a cambios de sesión (resuelve el timing de Firebase Auth) ─
        authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val uid = firebaseAuth.currentUser?.uid
            if (uid != null) {
                // Usuario autenticado y listener aún no iniciado
                if (listenerReg == null) {
                    seedPromotionsIfNeeded(uid)
                    listenNotifications(uid)
                }
            } else {
                // Sin sesión: limpiar listener y estado
                listenerReg?.remove()
                listenerReg = null
                uiState = NotificationsUiState(isLoading = false)
            }
        }
        auth.addAuthStateListener(authListener)
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authListener)
        listenerReg?.remove()
    }

    // ── Añadir notificación local (optimista) ─────────────────────────────────

    fun addLocalNotification(title: String, message: String, type: String) {
        val uid = auth.currentUser?.uid ?: return
        val now    = System.currentTimeMillis()
        val tempId = "local_${now}"

        val newNotif = AppNotification(
            id        = tempId,
            title     = title,
            message   = message,
            type      = type,
            timestamp = now
        )

        // Actualización optimista: insertar al inicio (más reciente primero)
        val updatedList = listOf(newNotif) + uiState.notifications
        uiState = uiState.copy(
            notifications = updatedList,
            unreadCount   = uiState.unreadCount + 1
        )

        // Persistir en Firestore
        firestore.collection("users").document(uid)
            .collection("notifications")
            .add(
                mapOf(
                    "title"     to title,
                    "message"   to message,
                    "type"      to type,
                    "timestamp" to now
                )
            )
            .addOnFailureListener { e ->
                Log.e("NotificationsVM", "Error guardando notificación local", e)
                // Rollback: quitar la entrada optimista
                uiState = uiState.copy(
                    notifications = uiState.notifications.filter { it.id != tempId },
                    unreadCount   = maxOf(0, uiState.unreadCount - 1)
                )
            }
    }

    // ── Marcar todas como leídas ──────────────────────────────────────────────

    fun markAllSeen() {
        settings.markNotificationsSeen()
        val now = System.currentTimeMillis()
        uiState = uiState.copy(unreadCount = 0, lastSeenTs = now)
    }

    // ── Escuchar colección en tiempo real ─────────────────────────────────────

    private fun listenNotifications(uid: String) {
        listenerReg = firestore
            .collection("users").document(uid)
            .collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    Log.e("NotificationsVM", "Error escuchando notificaciones", error)
                    uiState = uiState.copy(isLoading = false)
                    return@addSnapshotListener
                }
                val notifs = snap?.documents?.mapNotNull { doc ->
                    runCatching {
                        AppNotification(
                            id        = doc.id,
                            title     = doc.getString("title")   ?: "",
                            message   = doc.getString("message") ?: "",
                            type      = doc.getString("type")    ?: "promo",
                            timestamp = doc.getLong("timestamp") ?: 0L
                        )
                    }.getOrNull()
                } ?: emptyList()

                val lastSeen = settings.lastSeenTimestamp.value
                uiState = NotificationsUiState(
                    notifications = notifs,
                    unreadCount   = notifs.count { it.timestamp > lastSeen },
                    lastSeenTs    = lastSeen,
                    isLoading     = false
                )
            }
    }

    // ── Poblar notificaciones la primera vez ──────────────────────────────────

    private fun seedPromotionsIfNeeded(uid: String) {
        val ref = firestore.collection("users").document(uid).collection("notifications")
        ref.limit(1).get().addOnSuccessListener { snap ->
            if (snap.isEmpty) {
                val now = System.currentTimeMillis()
                listOf(

                    // ── Descuentos en productos ───────────────────────────────
                    mapOf(
                        "title"     to "🎮 Juguete de Goma con 15% OFF",
                        "message"   to "El Juguete de Goma resistente baja de \$9.99 a \$8.49. No tóxico, ideal para perros de todas las razas. ¡Solo por tiempo limitado!",
                        "type"      to "promo",
                        "timestamp" to now - 1_800_000L           // hace 30 min
                    ),
                    mapOf(
                        "title"     to "🛏️ Cama Cómoda con 20% de descuento",
                        "message"   to "La Cama Ortopédica con espuma viscoelástica bajó de \$39.99 a \$31.99. Funda lavable incluida. ¡Solo quedan 8 unidades!",
                        "type"      to "promo",
                        "timestamp" to now - 7_200_000L           // hace 2 horas
                    ),
                    mapOf(
                        "title"     to "🐕 Correa Suave con 10% OFF",
                        "message"   to "La Correa ergonómica con mango acolchado está de \$15.00 a \$13.50. Nylon reforzado para perros hasta 40 kg.",
                        "type"      to "promo",
                        "timestamp" to now - 21_600_000L          // hace 6 horas
                    ),

                    // ── Ofertas de servicios ──────────────────────────────────
                    mapOf(
                        "title"     to "🏠 ¡Primera consulta a domicilio sin costo de envío!",
                        "message"   to "Agenda con el Dr. Carlos Ruiz, Vet Norte Cedritos u otros servicios a domicilio y el primer desplazamiento corre por nuestra cuenta.",
                        "type"      to "promo",
                        "timestamp" to now - 43_200_000L          // hace 12 horas
                    ),
                    mapOf(
                        "title"     to "✂️ Baño + Aromaterapia en Spa Patitas Felices",
                        "message"   to "Esta semana el pack Baño + Aromaterapia tiene precio especial. Dale un mimo a tu mascota con los mejores profesionales.",
                        "type"      to "promo",
                        "timestamp" to now - 86_400_000L          // hace 1 día
                    ),
                    mapOf(
                        "title"     to "🚑 Domicilio gratis en urgencias veterinarias",
                        "message"   to "Vet Domicilio Salitre y Clínica Vet Central ofrecen desplazamiento sin costo en urgencias nocturnas. Disponible 24/7.",
                        "type"      to "promo",
                        "timestamp" to now - 172_800_000L         // hace 2 días
                    ),

                    // ── Recordatorio general ──────────────────────────────────
                    mapOf(
                        "title"     to "💊 ¿Ya tienes las vacunas al día?",
                        "message"   to "Encuentra clínicas veterinarias cerca de ti con disponibilidad inmediata. Fauna Clínica Usaquén y Vet & Más Engativá tienen agenda libre esta semana.",
                        "type"      to "promo",
                        "timestamp" to now - 259_200_000L         // hace 3 días
                    )

                ).forEach { ref.add(it) }
            }
        }.addOnFailureListener { e ->
            Log.e("NotificationsVM", "Error al verificar seed de notificaciones", e)
        }
    }
}
