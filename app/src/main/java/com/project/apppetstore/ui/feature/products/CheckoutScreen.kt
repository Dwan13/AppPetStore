package com.project.apppetstore.ui.feature.products

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.project.apppetstore.data.model.Order
import com.project.apppetstore.data.model.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    product: Product,
    quantity: Int,
    onBack: () -> Unit,
    onOrderConfirmed: () -> Unit,
    onPlaceOrder: (Order) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val basePrice = product.price.removePrefix("$").toDoubleOrNull() ?: 0.0
    val unitPrice =
        if (product.discount > 0) basePrice * (1 - product.discount / 100.0) else basePrice
    val subtotal = unitPrice * quantity
    val shipping = if (subtotal >= 30.0) 0.0 else 5.99
    val total = subtotal + shipping

    var address by remember { mutableStateOf("") }
    var addressError by remember { mutableStateOf(false) }
    var paymentMethod by remember { mutableStateOf("Efectivo") }
    var isOrdering by remember { mutableStateOf(false) }
    var orderSuccess by remember { mutableStateOf(false) }

    val paymentOptions = listOf("Efectivo", "Tarjeta de débito", "Tarjeta de crédito")

    // ── Pantalla de éxito ─────────────────────────────────────────────────────
    if (orderSuccess) {
        OrderSuccessScreen(onContinue = onOrderConfirmed)
        return
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Confirmar pedido", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                    // Total en la barra inferior
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Total a pagar",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "$${"%.2f".format(total)}",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Button(
                            onClick = {
                                if (address.isBlank()) {
                                    addressError = true
                                } else {
                                    isOrdering = true
                                    onPlaceOrder(
                                        Order(
                                            productId = product.id,
                                            productName = product.name,
                                            productImageUrl = product.imageUrl,
                                            quantity = quantity,
                                            unitPrice = unitPrice,
                                            total = total,
                                            address = address.trim(),
                                            paymentMethod = paymentMethod,
                                            status = "Confirmado",
                                            timestamp = System.currentTimeMillis()
                                        )
                                    )
                                    orderSuccess = true
                                }
                            },
                            modifier = Modifier
                                .height(52.dp)
                                .widthIn(min = 160.dp),
                            shape = RoundedCornerShape(50),
                            enabled = !isOrdering
                        ) {
                            if (isOrdering) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    "Confirmar pedido",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // ── Resumen del pedido ────────────────────────────────────────────
            ElevatedCard(shape = MaterialTheme.shapes.large) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Resumen del pedido",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Miniatura
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            if (product.imageRes != null) {
                                Image(
                                    painter = painterResource(product.imageRes),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(product.name, style = MaterialTheme.typography.titleSmall)
                            Text(
                                product.category,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Cantidad: $quantity",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        Text(
                            "$${"%.2f".format(unitPrice)}",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Desglose de precios
                    CheckoutPriceRow("Subtotal", "$${"%.2f".format(subtotal)}")
                    CheckoutPriceRow(
                        label = "Envío",
                        value = if (shipping == 0.0) "Gratis" else "$${"%.2f".format(shipping)}",
                        valueColor = if (shipping == 0.0) MaterialTheme.colorScheme.primary else null
                    )
                    if (product.discount > 0) {
                        CheckoutPriceRow(
                            label = "Descuento aplicado",
                            value = "-${product.discount}%",
                            valueColor = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // ── Envío gratis ─────────────────────────────────────────────────
            if (shipping == 0.0) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            "¡Envío gratuito en pedidos mayores a \$30!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // ── Dirección de entrega ──────────────────────────────────────────
            ElevatedCard(shape = MaterialTheme.shapes.large) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Dirección de entrega", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = address,
                        onValueChange = {
                            address = it
                            addressError = false
                        },
                        label = { Text("Calle, número, ciudad") },
                        isError = addressError,
                        supportingText = if (addressError) {
                            { Text("Por favor ingresa una dirección") }
                        } else null,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        minLines = 2
                    )
                }
            }

            // ── Método de pago ────────────────────────────────────────────────
            ElevatedCard(shape = MaterialTheme.shapes.large) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .selectableGroup()
                ) {
                    Text("Método de pago", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    paymentOptions.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = paymentMethod == option,
                                    onClick = { paymentMethod = option },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            RadioButton(
                                selected = paymentMethod == option,
                                onClick = null
                            )
                            Text(option, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// ── Fila de precio en el resumen ──────────────────────────────────────────────

@Composable
private fun CheckoutPriceRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = valueColor ?: MaterialTheme.colorScheme.onSurface
        )
    }
}

// ── Pantalla de confirmación de pedido ────────────────────────────────────────

@Composable
private fun OrderSuccessScreen(onContinue: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(100.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(56.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            "¡Pedido confirmado!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Tu pedido ha sido recibido. Te enviaremos una confirmación con el seguimiento.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(50)
        ) {
            Text("Seguir comprando", style = MaterialTheme.typography.labelLarge)
        }
    }
}
