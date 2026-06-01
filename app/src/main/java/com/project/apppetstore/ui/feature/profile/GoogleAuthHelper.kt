package com.project.apppetstore.ui.feature.profile

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.GoogleAuthProvider

// ─── Web Client ID ────────────────────────────────────────────────────────────
// Firebase Console → Configuración del proyecto ⚙️ → General
// → OAuth 2.0 web client → Client ID
// ─────────────────────────────────────────────────────────────────────────────
internal const val GOOGLE_WEB_CLIENT_ID =
    "1003439074308-edhrhud53r4hmld0e9pqrvfhfjm9q7bu.apps.googleusercontent.com"

/**
 * Abre el selector de cuentas de Google y devuelve la credencial de Firebase.
 *
 * Estrategia en dos pasos:
 *  1. Intenta [GetSignInWithGoogleOption] (muestra el selector estándar de Google — más fiable).
 *  2. Si falla, cae en [GetGoogleIdOption] con [filterByAuthorizedAccounts]=false.
 *
 * Llama [onSuccess] con la credencial si todo va bien.
 * Llama [onError] con un mensaje amigable si hay un problema real.
 * No hace nada si el usuario cancela.
 */
suspend fun launchGoogleSignIn(
    context  : Context,
    onSuccess: (AuthCredential) -> Unit,
    onError  : (String) -> Unit
) {
    val credentialManager = CredentialManager.create(context)

    // ── Paso 1: GetSignInWithGoogleOption (selector estándar) ─────────────────
    val signInOption = GetSignInWithGoogleOption.Builder(GOOGLE_WEB_CLIENT_ID).build()
    val primaryRequest = GetCredentialRequest.Builder()
        .addCredentialOption(signInOption)
        .build()

    try {
        val result = credentialManager.getCredential(context, primaryRequest)
        handleCredentialResult(result.credential, onSuccess, onError)
        return   // éxito — salimos
    } catch (_: GetCredentialCancellationException) {
        return   // usuario canceló — silencioso
    } catch (_: NoCredentialException) {
        // Sin cuenta configurada en este paso → intenta el fallback
    } catch (e: GetCredentialException) {
        // Otro error en el paso 1 → intenta el fallback
    }

    // ── Paso 2: GetGoogleIdOption (One Tap, sin filtro de cuentas guardadas) ──
    try {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(GOOGLE_WEB_CLIENT_ID)
            .setAutoSelectEnabled(false)
            .build()

        val fallbackRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val result = credentialManager.getCredential(context, fallbackRequest)
        handleCredentialResult(result.credential, onSuccess, onError)

    } catch (_: GetCredentialCancellationException) {
        // Usuario canceló — silencioso
    } catch (_: NoCredentialException) {
        onError(
            "No hay cuentas de Google en este dispositivo.\n" +
            "Ve a Ajustes → Cuentas y agrega una cuenta de Google."
        )
    } catch (e: GetCredentialException) {
        onError(friendlyGoogleError(e))
    } catch (e: Exception) {
        onError("Error inesperado con Google Sign-In: ${e.localizedMessage}")
    }
}

// ─────────────────────────────────────────────────────────────────────────────

private fun handleCredentialResult(
    credential: androidx.credentials.Credential,
    onSuccess : (AuthCredential) -> Unit,
    onError   : (String) -> Unit
) {
    if (credential is CustomCredential &&
        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
    ) {
        val googleCred   = GoogleIdTokenCredential.createFrom(credential.data)
        val authCredential = GoogleAuthProvider.getCredential(googleCred.idToken, null)
        onSuccess(authCredential)
    } else {
        onError("Tipo de credencial no reconocido. Intenta de nuevo.")
    }
}

private fun friendlyGoogleError(e: GetCredentialException): String {
    val msg = e.message?.lowercase() ?: ""
    return when {
        "interrupted"   in msg -> "El inicio de sesión fue interrumpido. Intenta de nuevo."
        "configuration" in msg ||
        "provider"      in msg -> "Google Sign-In no está configurado en este dispositivo."
        "network"       in msg -> "Sin conexión a internet. Verifica tu red e intenta de nuevo."
        "timeout"       in msg -> "Se agotó el tiempo de espera. Intenta de nuevo."
        else                   -> "No se pudo iniciar sesión con Google. Intenta de nuevo."
    }
}
