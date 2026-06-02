package com.project.apppetstore.ui.feature.profile

import android.util.Patterns
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.AuthCredential
import com.project.apppetstore.R
import kotlinx.coroutines.launch

 // Validation helpers
 
private fun nameError(name: String): String? = when {
    name.isBlank() -> "El nombre es obligatorio"
    name.trim().length < 3 -> "El nombre debe tener al menos 3 caracteres"
    else -> null
}

private fun regEmailError(email: String): String? = when {
    email.isBlank() -> "El correo es obligatorio"
    !Patterns.EMAIL_ADDRESS.matcher(email.trim())
        .matches() -> "Ingresa un correo válido (ej: usuario@mail.com)"

    else -> null
}

private fun regPasswordError(password: String): String? = when {
    password.isBlank() -> "La contraseña es obligatoria"
    password.length < 8 -> "Mínimo 8 caracteres"
    !password.any { it.isDigit() } -> "Incluye al menos un número"
    !password.any { it.isUpperCase() } -> "Incluye al menos una mayúscula"
    else -> null
}

private fun confirmError(password: String, confirm: String): String? = when {
    confirm.isBlank() -> "Confirma tu contraseña"
    confirm != password -> "Las contraseñas no coinciden"
    else -> null
}

/**
 * Calcula la fortaleza de la contraseña (0..4) y devuelve (fracción, etiqueta, color).
 *   0 = vacía      → sin indicador
 *   1 = muy corta  → rojo
 *   2 = débil      → naranja
 *   3 = aceptable  → amarillo
 *   4 = fuerte     → verde
 */
private fun passwordStrength(password: String): Triple<Float, String, Color> {
    if (password.isEmpty()) return Triple(0f, "", Color.Transparent)
    val hasDigit = password.any { it.isDigit() }
    val hasUpper = password.any { it.isUpperCase() }
    val hasSpecial = password.any { !it.isLetterOrDigit() }
    val score = when {
        password.length < 6 -> 1
        password.length < 8 -> 2
        !hasDigit && !hasUpper -> 2
        password.length >= 8 && hasDigit && !hasUpper && !hasSpecial -> 3
        password.length >= 8 && hasDigit && hasUpper && !hasSpecial -> 3
        password.length >= 10 && hasDigit && hasUpper && hasSpecial -> 4
        else -> 3
    }
    return when (score) {
        1 -> Triple(0.20f, "Muy corta", Color(0xFFD32F2F))
        2 -> Triple(0.45f, "Débil", Color(0xFFF57C00))
        3 -> Triple(0.72f, "Aceptable", Color(0xFFFBC02D))
        else -> Triple(1.00f, "Fuerte", Color(0xFF388E3C))
    }
}

 // Screen
 
@Composable
fun RegisterScreen(
    uiState: ProfileUiState,
    onRegister: (String, String, String) -> Unit,
    onSignInWithGoogle: (AuthCredential) -> Unit,
    onBackToLogin: () -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // ── Valores de campo ──────────────────────────────────────────────────────
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }
    var googleError by remember { mutableStateOf<String?>(null) }

    // ── Errores de campo ──────────────────────────────────────────────────────
    var nameFieldError by remember { mutableStateOf<String?>(null) }
    var emailFieldError by remember { mutableStateOf<String?>(null) }
    var passFieldError by remember { mutableStateOf<String?>(null) }
    var confirmFieldError by remember { mutableStateOf<String?>(null) }
    var hasAttempted by remember { mutableStateOf(false) }

    // Re-valida en tiempo real tras el primer intento
    fun revalidate() {
        if (!hasAttempted) return
        nameFieldError = nameError(fullName)
        emailFieldError = regEmailError(email)
        passFieldError = regPasswordError(password)
        confirmFieldError = confirmError(password, confirmPassword)
    }

    fun attemptRegister() {
        hasAttempted = true
        focusManager.clearFocus()
        val nErr = nameError(fullName)
        val eErr = regEmailError(email)
        val pErr = regPasswordError(password)
        val cErr = confirmError(password, confirmPassword)
        nameFieldError = nErr
        emailFieldError = eErr
        passFieldError = pErr
        confirmFieldError = cErr
        if (nErr == null && eErr == null && pErr == null && cErr == null) {
            onRegister(fullName.trim(), email.trim(), password)
        }
    }

    // ── Fortaleza de contraseña ───────────────────────────────────────────────
    val (strengthFraction, strengthLabel, strengthColor) = passwordStrength(password)
    val animatedStrength by animateFloatAsState(
        targetValue = strengthFraction,
        label = "pw_strength"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // ── Encabezado ────────────────────────────────────────────────────────
        Text(
            text = "Crear cuenta",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Completa los campos para registrarte",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        // ── Nombre completo ───────────────────────────────────────────────────
        OutlinedTextField(
            value = fullName,
            onValueChange = {
                fullName = it
                onClearError()
                if (nameFieldError != null) nameFieldError = nameError(it)
            },
            label = { Text("Nombre completo") },
            singleLine = true,
            isError = nameFieldError != null,
            supportingText = nameFieldError?.let { err -> { Text(err) } },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (!focusState.isFocused && fullName.isNotEmpty()) {
                        nameFieldError = nameError(fullName)
                    }
                }
        )

        // ── Correo ────────────────────────────────────────────────────────────
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                onClearError()
                if (emailFieldError != null) emailFieldError = regEmailError(it)
            },
            label = { Text("Correo electrónico") },
            singleLine = true,
            isError = emailFieldError != null,
            supportingText = emailFieldError?.let { err -> { Text(err) } },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (!focusState.isFocused && email.isNotEmpty()) {
                        emailFieldError = regEmailError(email)
                    }
                }
        )

        // ── Contraseña ────────────────────────────────────────────────────────
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                onClearError()
                if (passFieldError != null) passFieldError = regPasswordError(it)
            },
            label = { Text("Contraseña") },
            singleLine = true,
            isError = passFieldError != null,
            supportingText = passFieldError?.let { err -> { Text(err) } },
            visualTransformation = if (passwordVisible)
                VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible)
                            Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "Ocultar" else "Mostrar"
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (!focusState.isFocused && password.isNotEmpty()) {
                        passFieldError = regPasswordError(password)
                    }
                }
        )

        // ── Indicador de fortaleza ────────────────────────────────────────────
        AnimatedVisibility(visible = password.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress = { animatedStrength },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp),
                    color = strengthColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Text(
                    text = "Contraseña: $strengthLabel",
                    style = MaterialTheme.typography.labelSmall,
                    color = strengthColor
                )
                if (password.isNotEmpty() && passFieldError == null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = Color(0xFF388E3C)
                        )
                        Text(
                            text = "Mínimo 8 caracteres, un número y una mayúscula",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // ── Confirmar contraseña ──────────────────────────────────────────────
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                if (confirmFieldError != null) confirmFieldError = confirmError(password, it)
            },
            label = { Text("Confirmar contraseña") },
            singleLine = true,
            isError = confirmFieldError != null,
            supportingText = confirmFieldError?.let { err -> { Text(err) } },
            visualTransformation = if (confirmVisible)
                VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { attemptRegister() }),
            trailingIcon = {
                IconButton(onClick = { confirmVisible = !confirmVisible }) {
                    Icon(
                        imageVector = if (confirmVisible)
                            Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (confirmVisible) "Ocultar" else "Mostrar"
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (!focusState.isFocused && confirmPassword.isNotEmpty()) {
                        confirmFieldError = confirmError(password, confirmPassword)
                    }
                }
        )

        // ── Política de requisitos (resumen visual) ───────────────────────────
        AnimatedVisibility(visible = !hasAttempted && password.isEmpty()) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "Requisitos de contraseña:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    listOf(
                        "Mínimo 8 caracteres",
                        "Al menos un número (0-9)",
                        "Al menos una mayúscula (A-Z)"
                    )
                        .forEach { req ->
                            Text(
                                text = "• $req",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                }
            }
        }

        // ── Error de Firebase ─────────────────────────────────────────────────
        AnimatedVisibility(visible = uiState.error != null) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = uiState.error ?: "",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // ── Botón de registro ─────────────────────────────────────────────────
        Button(
            onClick = { attemptRegister() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = !uiState.isLoading,
            shape = MaterialTheme.shapes.medium
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Crear cuenta", style = MaterialTheme.typography.labelLarge)
            }
        }

        // ── Divisor ───────────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                text = "  o regístrate con  ",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }

        // ── Error de Google Sign-In ───────────────────────────────────────────
        AnimatedVisibility(visible = googleError != null) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = googleError ?: "",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }

        // ── Google Sign-In ────────────────────────────────────────────────────
        OutlinedButton(
            onClick = {
                googleError = null
                scope.launch {
                    launchGoogleSignIn(
                        context = context,
                        onSuccess = { credential -> onSignInWithGoogle(credential) },
                        onError = { msg -> googleError = msg }
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = !uiState.isLoading,
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_google),
                contentDescription = "Google",
                modifier = Modifier.size(20.dp),
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text("Continuar con Google", style = MaterialTheme.typography.labelLarge)
        }

        Spacer(modifier = Modifier.height(4.dp))

        // ── Volver al login ───────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "¿Ya tienes cuenta?",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.width(4.dp))
            TextButton(
                onClick = onBackToLogin,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("Inicia sesión", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
