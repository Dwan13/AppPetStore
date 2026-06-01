package com.project.apppetstore.ui.feature.profile

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.project.apppetstore.data.model.PetProfile
import com.project.apppetstore.data.model.UserProfile
import com.project.apppetstore.utils.AppNotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ProfileUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val user: UserProfile? = null,
    val error: String? = null
)

class ProfileViewModel(app: Application) : AndroidViewModel(app) {

    private val auth        = FirebaseAuth.getInstance()
    private val firestore   = FirebaseFirestore.getInstance()
    private val storageRef  = FirebaseStorage.getInstance().reference

    private val _uiState = MutableStateFlow(
        ProfileUiState(
            isLoggedIn = auth.currentUser != null,
            user = auth.currentUser?.let { fu ->
                UserProfile(
                    fullName = fu.displayName ?: fu.email?.substringBefore("@") ?: "Usuario",
                    email    = fu.email ?: "",
                    profilePhotoUri = fu.photoUrl?.toString()
                )
            }
        )
    )
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                _uiState.update { it.copy(isLoggedIn = true, isLoading = true) }
                loadUserData(firebaseUser)
            } else {
                _uiState.update { ProfileUiState(isLoggedIn = false, isLoading = false) }
            }
        }
    }

    // ── Cargar perfil desde Firestore ───────────────────────────────────────

    private fun loadUserData(firebaseUser: FirebaseUser) {
        firestore.collection("users").document(firebaseUser.uid).get()
            .addOnSuccessListener { doc ->
                val petData = doc.get("petProfile") as? Map<*, *>

                val petTraits = (petData?.get("traits") as? List<*>)
                    ?.filterIsInstance<String>()
                    ?: emptyList()

                val profile = UserProfile(
                    fullName = doc.getString("fullName")
                        ?: firebaseUser.displayName
                        ?: firebaseUser.email?.substringBefore("@")
                        ?: "Usuario",
                    email = doc.getString("email") ?: firebaseUser.email ?: "",
                    profilePhotoUri = doc.getString("profilePhotoUrl")
                        ?.takeIf { it.isNotBlank() }
                        ?: firebaseUser.photoUrl?.toString(),
                    petProfile = PetProfile(
                        name     = petData?.get("name")     as? String ?: "Bobby",
                        species  = petData?.get("species")  as? String ?: "Perro",
                        age      = petData?.get("age")      as? String ?: "3 años",
                        photoUri = (petData?.get("photoUrl") as? String)?.takeIf { it.isNotBlank() },
                        traits   = petTraits
                    )
                )
                _uiState.update { it.copy(isLoading = false, user = profile) }
            }
            .addOnFailureListener {
                // Fallback: usar datos básicos de Firebase Auth
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        user = state.user ?: UserProfile(
                            fullName = firebaseUser.displayName
                                ?: firebaseUser.email?.substringBefore("@")
                                ?: "Usuario",
                            email = firebaseUser.email ?: "",
                            profilePhotoUri = firebaseUser.photoUrl?.toString()
                        )
                    )
                }
            }
    }

    // ── Guardar perfil completo en Firestore ────────────────────────────────

    private fun saveUserToFirestore(uid: String, user: UserProfile) {
        val data = hashMapOf(
            "fullName"        to user.fullName,
            "email"           to user.email,
            "profilePhotoUrl" to (user.profilePhotoUri ?: ""),
            "petProfile"      to hashMapOf(
                "name"     to user.petProfile.name,
                "species"  to user.petProfile.species,
                "age"      to user.petProfile.age,
                "photoUrl" to (user.petProfile.photoUri ?: ""),
                "traits"   to user.petProfile.traits
            )
        )
        firestore.collection("users").document(uid).set(data)
    }

    // ── Email / Contraseña ──────────────────────────────────────────────────

    fun loginWithEmail(email: String, password: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        auth.signInWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener { result ->
                val name = result.user?.displayName
                    ?: result.user?.email?.substringBefore("@")
                    ?: "Usuario"
                AppNotificationHelper.showLoginNotification(getApplication(), name)
                _uiState.update { it.copy(isLoading = false) }
            }
            .addOnFailureListener { e ->
                _uiState.update { it.copy(isLoading = false, error = friendlyError(e.message)) }
            }
    }

    fun registerWithEmail(fullName: String, email: String, password: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        auth.createUserWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener { result ->
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(fullName.trim())
                    .build()
                result.user?.updateProfile(profileUpdates)
                    ?.addOnSuccessListener {
                        result.user?.uid?.let { uid ->
                            saveUserToFirestore(
                                uid,
                                UserProfile(fullName = fullName.trim(), email = email.trim())
                            )
                        }
                        AppNotificationHelper.showRegisterNotification(
                            getApplication(), fullName.trim()
                        )
                        _uiState.update { it.copy(isLoading = false) }
                    }
            }
            .addOnFailureListener { e ->
                _uiState.update { it.copy(isLoading = false, error = friendlyError(e.message)) }
            }
    }

    // ── Google Sign-In ──────────────────────────────────────────────────────

    fun signInWithGoogle(credential: AuthCredential) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        auth.signInWithCredential(credential)
            .addOnSuccessListener { result ->
                val name = result.user?.displayName
                    ?: result.user?.email?.substringBefore("@")
                    ?: "Usuario"
                AppNotificationHelper.showLoginNotification(getApplication(), name)
                _uiState.update { it.copy(isLoading = false) }
            }
            .addOnFailureListener { e ->
                _uiState.update { it.copy(isLoading = false, error = friendlyError(e.message)) }
            }
    }

    // ── Subir foto de perfil ────────────────────────────────────────────────

    fun uploadProfilePhoto(uri: Uri) {
        val uid = auth.currentUser?.uid ?: return
        _uiState.update { it.copy(isLoading = true, error = null) }
        val ref = storageRef.child("users/$uid/profile.jpg")
        ref.putFile(uri)
            .continueWithTask { task ->
                if (!task.isSuccessful) throw (task.exception ?: Exception("Error al subir"))
                ref.downloadUrl
            }
            .addOnSuccessListener { downloadUri ->
                val url = downloadUri.toString()
                _uiState.update { state ->
                    val updated = state.user?.copy(profilePhotoUri = url)
                    updated?.let { saveUserToFirestore(uid, it) }
                    state.copy(isLoading = false, user = updated)
                }
            }
            .addOnFailureListener { e ->
                _uiState.update {
                    it.copy(isLoading = false, error = "Error al subir foto de perfil: ${e.message}")
                }
            }
    }

    // ── Subir foto de mascota ───────────────────────────────────────────────

    fun uploadPetPhoto(uri: Uri) {
        val uid = auth.currentUser?.uid ?: return
        _uiState.update { it.copy(isLoading = true, error = null) }
        val ref = storageRef.child("users/$uid/pet.jpg")
        ref.putFile(uri)
            .continueWithTask { task ->
                if (!task.isSuccessful) throw (task.exception ?: Exception("Error al subir"))
                ref.downloadUrl
            }
            .addOnSuccessListener { downloadUri ->
                val url = downloadUri.toString()
                _uiState.update { state ->
                    val updated = state.user?.copy(
                        petProfile = state.user.petProfile.copy(photoUri = url)
                    )
                    updated?.let { saveUserToFirestore(uid, it) }
                    state.copy(isLoading = false, user = updated)
                }
            }
            .addOnFailureListener { e ->
                _uiState.update {
                    it.copy(isLoading = false, error = "Error al subir foto de mascota: ${e.message}")
                }
            }
    }

    // ── Actualizar características de mascota ───────────────────────────────

    fun updatePetCharacteristics(
        name: String,
        species: String,
        age: String,
        traits: List<String>
    ) {
        val uid = auth.currentUser?.uid ?: return
        _uiState.update { state ->
            val updated = state.user?.copy(
                petProfile = state.user.petProfile.copy(
                    name = name, species = species, age = age, traits = traits
                )
            )
            updated?.let { saveUserToFirestore(uid, it) }
            state.copy(user = updated)
        }
    }

    // ── Cerrar sesión ───────────────────────────────────────────────────────

    fun logout() {
        AppNotificationHelper.showLogoutNotification(getApplication())
        auth.signOut()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    // ── Errores amigables ───────────────────────────────────────────────────

    private fun friendlyError(message: String?): String = when {
        message?.contains("email address is already in use") == true ->
            "Este correo ya está registrado. ¿Ya tienes cuenta? Inicia sesión."
        message?.contains("INVALID_LOGIN_CREDENTIALS") == true ||
        message?.contains("password is invalid") == true ||
        message?.contains("no user record") == true ||
        message?.contains("INVALID_EMAIL") == true ->
            "Correo o contraseña incorrectos. Verifica tus datos."
        message?.contains("badly formatted") == true ->
            "El correo no tiene un formato válido"
        message?.contains("password should be at least") == true ||
        message?.contains("weak-password") == true ->
            "La contraseña es demasiado débil. Usa mínimo 8 caracteres, un número y una mayúscula."
        message?.contains("user-not-found") == true ->
            "No existe una cuenta con este correo"
        message?.contains("too-many-requests") == true ||
        message?.contains("too many attempts") == true ->
            "Demasiados intentos fallidos. Espera unos minutos."
        message?.contains("network") == true ||
        message?.contains("NETWORK") == true ->
            "Sin conexión a internet. Revisa tu red."
        message?.contains("account-exists-with-different-credential") == true ->
            "Este correo ya está registrado con otro método de inicio de sesión."
        else -> "Error de autenticación. Intenta de nuevo."
    }
}
