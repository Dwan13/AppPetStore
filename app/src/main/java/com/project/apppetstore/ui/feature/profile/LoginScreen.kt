package com.project.apppetstore.ui.feature.profile

import android.util.Patterns
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.AuthCredential
import com.project.apppetstore.R
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// Validation helpers
// ─────────────────────────────────────────────────────────────────────────────

private fun emailError(email: String): String? = when {
    email.isBlank()                                              -> "El correo es obligatorio"
    !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()      -> "Ingresa un correo válido (ej: usuario@mail.com)"
    else                                                         -> null
}

private fun loginPasswordError(password: String): String? = when {
    password.isBlank() -> "La contraseña es obligatoria"
    password.length < 6 -> "Mínimo 6 caracteres"
    else               -> null
}

// ─────────────────────────────────────────────────────────────────────────────
// Screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun LoginScreen(
    uiState             : ProfileUiState,
    onLoginWithEmail    : (String, String) -> Unit,
    onSignInWithGoogle  : (AuthCredential) -> Unit,
    onNavigateToRegister: () -> Unit,
    onClearError        : () -> Unit,
    modifier            : Modifier = Modifier
) {
    val context      = LocalContext.current
    val scope        = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // ── Campos ────────────────────────────────────────────────────────────────
    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var googleError     by remember { mutableStateOf<String?>(null) }

    // ── Errores de campo (solo visibles tras el primer intento de envío) ──────
    var emailFieldError    by remember { mutableStateOf<String?>(null) }
    var passwordFieldError by remember { mutableStateOf<String?>(null) }
    var hasAttempted       by remember { mutableStateOf(false) }

    // Re-valida en tiempo real una vez que el usuario ya intentó enviar
    fun revalidate() {
        if (hasAttempted) {
            emailFieldError    = emailError(email)
            passwordFieldError = loginPasswordError(password)
        }
    }

    fun attemptLogin() {
        hasAttempted = true
        focusManager.clearFocus()
        val eErr = emailError(email)
        val pErr = loginPasswordError(password)
        emailFieldError    = eErr
        passwordFieldError = pErr
        if (eErr == null && pErr == null) {
            onLoginWithEmail(email.trim(), password)
        }
    }

    Box(
        modifier        = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 48.dp),
            verticalArrangement  = Arrangement.spacedBy(12.dp),
            horizontalAlignment  = Alignment.CenterHorizontally
        ) {

            // ── Logo / título ─────────────────────────────────────────────────
            Text(
                text       = "AppPetStore",
                style      = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.primary
            )
            Text(
                text  = "Inicia sesión para continuar",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            // ── Correo ────────────────────────────────────────────────────────
            OutlinedTextField(
                value         = email,
                onValueChange = {
                    email = it
                    onClearError()
                    // Si ya hay un error visible, lo actualiza en tiempo real
                    if (emailFieldError != null) emailFieldError = emailError(it)
                },
                label          = { Text("Correo electrónico") },
                singleLine     = true,
                isError        = emailFieldError != null,
                supportingText = emailFieldError?.let { err -> { Text(err) } },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction    = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        // Valida cuando el usuario abandona el campo
                        if (!focusState.isFocused && email.isNotEmpty()) {
                            emailFieldError = emailError(email)
                        }
                    }
            )

            // ── Contraseña ────────────────────────────────────────────────────
            OutlinedTextField(
                value         = password,
                onValueChange = {
                    password = it
                    onClearError()
                    // Si ya hay un error visible, lo actualiza en tiempo real
                    if (passwordFieldError != null) passwordFieldError = loginPasswordError(it)
                },
                label                = { Text("Contraseña") },
                singleLine           = true,
                isError              = passwordFieldError != null,
                supportingText       = passwordFieldError?.let { err -> { Text(err) } },
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction    = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { attemptLogin() }),
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
                        // Valida cuando el usuario abandona el campo
                        if (!focusState.isFocused && password.isNotEmpty()) {
                            passwordFieldError = loginPasswordError(password)
                        }
                    }
            )

            // ── Error de Firebase ─────────────────────────────────────────────
            AnimatedVisibility(visible = uiState.error != null) {
                Surface(
                    shape  = MaterialTheme.shapes.small,
                    color  = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text     = uiState.error ?: "",
                        color    = MaterialTheme.colorScheme.onErrorContainer,
                        style    = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // ── Botón de login ────────────────────────────────────────────────
            Button(
                onClick  = { attemptLogin() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !uiState.isLoading,
                shape   = MaterialTheme.shapes.medium
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(18.dp),
                        color       = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Iniciar sesión", style = MaterialTheme.typography.labelLarge)
                }
            }

            // ── Divisor ───────────────────────────────────────────────────────
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text  = "  o continúa con  ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            // ── Error de Google Sign-In ───────────────────────────────────────
            AnimatedVisibility(visible = googleError != null) {
                Surface(
                    shape    = MaterialTheme.shapes.small,
                    color    = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text     = googleError ?: "",
                        color    = MaterialTheme.colorScheme.onErrorContainer,
                        style    = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            // ── Google Sign-In ────────────────────────────────────────────────
            OutlinedButton(
                onClick = {
                    googleError = null
                    scope.launch {
                        launchGoogleSignIn(
                            context   = context,
                            onSuccess = { credential ->
                                onSignInWithGoogle(credential)
                            },
                            onError   = { msg -> googleError = msg }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !uiState.isLoading,
                shape   = MaterialTheme.shapes.medium
            ) {
                Icon(
                    painter            = painterResource(id = R.drawable.ic_google),
                    contentDescription = "Google",
                    modifier           = Modifier.size(20.dp),
                    tint               = Color.Unspecified
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Continuar con Google", style = MaterialTheme.typography.labelLarge)
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ── Ir a registro ─────────────────────────────────────────────────
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text  = "¿No tienes cuenta?",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.width(4.dp))
                TextButton(
                    onClick        = onNavigateToRegister,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Regístrate", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
