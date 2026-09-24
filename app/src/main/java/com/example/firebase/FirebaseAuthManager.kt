package com.example.firebase

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

data class AppUser(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
    val role: String = "USER", // USER or ADMIN
    val isAnonymous: Boolean = false
)

class FirebaseAuthManager(private val context: Context) {

    private val credentialManager = CredentialManager.create(context)
    private var auth: FirebaseAuth? = null

    private val _currentUser = MutableStateFlow<AppUser?>(null)
    val currentUser: StateFlow<AppUser?> = _currentUser.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    init {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                val firebaseAuth = FirebaseAuth.getInstance()
                auth = firebaseAuth
                firebaseAuth.currentUser?.let { user ->
                    _currentUser.value = toAppUser(user)
                }
                firebaseAuth.addAuthStateListener { fa ->
                    _currentUser.value = fa.currentUser?.let { toAppUser(it) }
                }
            } else {
                Log.w("FirebaseAuthManager", "FirebaseApp not initialized, using local session state.")
                // Set default demo authenticated researcher
                _currentUser.value = AppUser(
                    uid = "phishguard-default-user",
                    email = "researcher@phishguard.internal",
                    displayName = "Cybersecurity Analyst",
                    photoUrl = null,
                    role = "USER"
                )
            }
        } catch (e: Exception) {
            Log.e("FirebaseAuthManager", "Firebase Auth init fallback: ${e.message}")
            _currentUser.value = AppUser(
                uid = "phishguard-default-user",
                email = "analyst@phishguard.internal",
                displayName = "Cybersecurity Analyst",
                photoUrl = null,
                role = "USER"
            )
        }
    }

    private fun toAppUser(user: FirebaseUser): AppUser {
        val role = if (user.email?.contains("admin", ignoreCase = true) == true) "ADMIN" else "USER"
        return AppUser(
            uid = user.uid,
            email = user.email,
            displayName = user.displayName ?: user.email?.substringBefore('@') ?: "Security Analyst",
            photoUrl = user.photoUrl?.toString(),
            role = role,
            isAnonymous = user.isAnonymous
        )
    }

    suspend fun signInWithGoogle(webClientId: String? = null): Result<AppUser> {
        _authError.value = null
        try {
            val serverClientId = webClientId ?: "default-client-id.apps.googleusercontent.com"
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val response: GetCredentialResponse = credentialManager.getCredential(
                request = request,
                context = context
            )

            val credential = response.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                val firebaseAuth = auth
                if (firebaseAuth != null) {
                    val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                    val authResult = firebaseAuth.signInWithCredential(authCredential).await()
                    val user = authResult.user
                    if (user != null) {
                        val appUser = toAppUser(user)
                        _currentUser.value = appUser
                        return Result.success(appUser)
                    }
                } else {
                    // FirebaseApp not configured with google-services.json: simulated Google Sign-in success
                    val appUser = AppUser(
                        uid = googleIdTokenCredential.id,
                        email = googleIdTokenCredential.id,
                        displayName = googleIdTokenCredential.displayName ?: "Google User",
                        photoUrl = googleIdTokenCredential.profilePictureUri?.toString(),
                        role = "USER"
                    )
                    _currentUser.value = appUser
                    return Result.success(appUser)
                }
            }
            return Result.failure(Exception("Unsupported credential type"))
        } catch (e: GetCredentialException) {
            Log.e("FirebaseAuthManager", "Credential Manager Error: ${e.message}")
            _authError.value = "Sign-in cancelled or unavailable: ${e.message}"
            return Result.failure(e)
        } catch (e: Exception) {
            Log.e("FirebaseAuthManager", "Google Sign In Error: ${e.message}")
            _authError.value = e.message
            return Result.failure(e)
        }
    }

    suspend fun signInWithEmail(email: String, pass: String): Result<AppUser> {
        _authError.value = null
        try {
            val firebaseAuth = auth
            if (firebaseAuth != null) {
                val result = firebaseAuth.signInWithEmailAndPassword(email, pass).await()
                val user = result.user ?: throw Exception("Authentication returned empty user")
                val appUser = toAppUser(user)
                _currentUser.value = appUser
                return Result.success(appUser)
            } else {
                // Local mock fallback for testing without active Firebase backend
                val role = if (email.contains("admin", ignoreCase = true)) "ADMIN" else "USER"
                val appUser = AppUser(
                    uid = "email-user-${email.hashCode()}",
                    email = email,
                    displayName = email.substringBefore('@'),
                    photoUrl = null,
                    role = role
                )
                _currentUser.value = appUser
                return Result.success(appUser)
            }
        } catch (e: Exception) {
            _authError.value = e.message
            return Result.failure(e)
        }
    }

    suspend fun registerWithEmail(email: String, pass: String): Result<AppUser> {
        _authError.value = null
        try {
            val firebaseAuth = auth
            if (firebaseAuth != null) {
                val result = firebaseAuth.createUserWithEmailAndPassword(email, pass).await()
                val user = result.user ?: throw Exception("User creation returned empty user")
                val appUser = toAppUser(user)
                _currentUser.value = appUser
                return Result.success(appUser)
            } else {
                val role = if (email.contains("admin", ignoreCase = true)) "ADMIN" else "USER"
                val appUser = AppUser(
                    uid = "new-user-${email.hashCode()}",
                    email = email,
                    displayName = email.substringBefore('@'),
                    photoUrl = null,
                    role = role
                )
                _currentUser.value = appUser
                return Result.success(appUser)
            }
        } catch (e: Exception) {
            _authError.value = e.message
            return Result.failure(e)
        }
    }

    fun signOut() {
        try {
            auth?.signOut()
        } catch (e: Exception) {
            Log.e("FirebaseAuthManager", "Sign out error: ${e.message}")
        }
        _currentUser.value = null
    }

    fun switchUserRole(newRole: String) {
        val current = _currentUser.value ?: return
        _currentUser.value = current.copy(role = newRole)
    }

    fun clearError() {
        _authError.value = null
    }
}
