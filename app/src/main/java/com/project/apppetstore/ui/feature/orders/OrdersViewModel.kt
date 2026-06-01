package com.project.apppetstore.ui.feature.orders

import android.app.Application
import android.util.Log
import com.project.apppetstore.utils.AppNotificationHelper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.project.apppetstore.data.model.Order

data class OrdersUiState(
    val orders    : List<Order> = emptyList(),
    val isLoading : Boolean     = true
)

class OrdersViewModel(app: Application) : AndroidViewModel(app) {

    private val auth         = FirebaseAuth.getInstance()
    private val firestore    = FirebaseFirestore.getInstance()
    private var listenerReg  : ListenerRegistration? = null
    private val authListener : FirebaseAuth.AuthStateListener

    var uiState by mutableStateOf(OrdersUiState())
        private set

    init {
        // ── Reaccionar a cambios de sesión (resuelve el timing de Firebase Auth) ─
        authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val uid = firebaseAuth.currentUser?.uid
            if (uid != null) {
                if (listenerReg == null) listenOrders(uid)
            } else {
                listenerReg?.remove()
                listenerReg = null
                uiState = OrdersUiState(isLoading = false)
            }
        }
        auth.addAuthStateListener(authListener)
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authListener)
        listenerReg?.remove()
    }

    // ── Guardar pedido en Firestore ───────────────────────────────────────────

    fun placeOrder(order: Order) {
        val uid = auth.currentUser?.uid ?: return
        val map = mapOf(
            "productId"        to order.productId,
            "productName"      to order.productName,
            "productImageUrl"  to order.productImageUrl,
            "quantity"         to order.quantity,
            "unitPrice"        to order.unitPrice,
            "total"            to order.total,
            "address"          to order.address,
            "paymentMethod"    to order.paymentMethod,
            "status"           to order.status,
            "timestamp"        to order.timestamp
        )
        firestore
            .collection("users").document(uid)
            .collection("orders")
            .add(map)

        // Notificación en la barra del sistema
        AppNotificationHelper.showOrderNotification(getApplication(), order.productName)

        // Notificación en la campana de la app (Firestore)
        firestore
            .collection("users").document(uid)
            .collection("notifications")
            .add(
                mapOf(
                    "title"     to "✅ Pedido confirmado",
                    "message"   to "Tu pedido de ${order.productName} fue recibido y está siendo procesado.",
                    "type"      to "order",
                    "timestamp" to System.currentTimeMillis()
                )
            )
    }

    // ── Escuchar pedidos en tiempo real ───────────────────────────────────────

    private fun listenOrders(uid: String) {
        listenerReg = firestore
            .collection("users").document(uid)
            .collection("orders")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    Log.e("OrdersVM", "Error escuchando pedidos", error)
                    uiState = uiState.copy(isLoading = false)
                    return@addSnapshotListener
                }
                val orders = snap?.documents?.mapNotNull { doc ->
                    runCatching {
                        Order(
                            id              = doc.id,
                            productId       = doc.getString("productId")      ?: "",
                            productName     = doc.getString("productName")    ?: "",
                            productImageUrl = doc.getString("productImageUrl"),
                            quantity        = (doc.getLong("quantity") ?: 1L).toInt(),
                            unitPrice       = doc.getDouble("unitPrice")      ?: 0.0,
                            total           = doc.getDouble("total")          ?: 0.0,
                            address         = doc.getString("address")        ?: "",
                            paymentMethod   = doc.getString("paymentMethod")  ?: "",
                            status          = doc.getString("status")         ?: "Confirmado",
                            timestamp       = doc.getLong("timestamp")        ?: 0L
                        )
                    }.getOrNull()
                } ?: emptyList()

                uiState = OrdersUiState(orders = orders, isLoading = false)
            }
    }
}
