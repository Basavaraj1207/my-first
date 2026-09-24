package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.model.ScanEntity
import com.example.data.repository.ScanRepository
import com.example.firebase.AppUser
import com.example.firebase.FirebaseAuthManager
import com.example.firebase.FirestoreManager
import com.example.ml.DetectionResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ScanUiState {
    data object Idle : ScanUiState
    data class Scanning(val progressText: String) : ScanUiState
    data class Success(val result: DetectionResult) : ScanUiState
    data class Error(val message: String) : ScanUiState
}

class PhishGuardViewModel(application: Application) : AndroidViewModel(application) {

    private val authManager = FirebaseAuthManager(application)
    private val firestoreManager = FirestoreManager(application)
    private val repository: ScanRepository

    val currentUser: StateFlow<AppUser?> = authManager.currentUser
    val authError: StateFlow<String?> = authManager.authError

    private val _urlInput = MutableStateFlow("")
    val urlInput: StateFlow<String> = _urlInput.asStateFlow()

    private val _scanState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val scanState: StateFlow<ScanUiState> = _scanState.asStateFlow()

    private val _historySearchQuery = MutableStateFlow("")
    val historySearchQuery: StateFlow<String> = _historySearchQuery.asStateFlow()

    private val _historyFilter = MutableStateFlow("ALL") // ALL, PHISHING, SUSPICIOUS, LEGITIMATE
    val historyFilter: StateFlow<String> = _historyFilter.asStateFlow()

    private val _isAuthDialogOpen = MutableStateFlow(false)
    val isAuthDialogOpen: StateFlow<Boolean> = _isAuthDialogOpen.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ScanRepository(database.scanDao(), firestoreManager)

        viewModelScope.launch {
            repository.allScans.collect { list ->
                if (list.isEmpty()) {
                    seedInitialDemoData()
                }
            }
        }
    }

    private suspend fun seedInitialDemoData() {
        val samples = listOf(
            "https://google.com",
            "http://192.168.1.105/bank-login.php?verify=1",
            "http://secure-update-paypal-verify.info/auth",
            "https://github.com/torvalds/linux",
            "http://appleid.apple.com-recover-portal.xyz/signin",
            "https://stackoverflow.com"
        )
        val uid = currentUser.value?.uid
        for (url in samples) {
            repository.performScan(url, uid)
        }
    }

    val scans: StateFlow<List<ScanEntity>> = repository.allScans
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredScans: StateFlow<List<ScanEntity>> = combine(
        scans,
        _historySearchQuery,
        _historyFilter
    ) { all, query, filter ->
        all.filter { scan ->
            val matchesQuery = query.isBlank() || scan.url.contains(query, ignoreCase = true)
            val matchesFilter = when (filter) {
                "PHISHING" -> scan.verdict.equals("PHISHING", ignoreCase = true)
                "SUSPICIOUS" -> scan.verdict.equals("SUSPICIOUS", ignoreCase = true)
                "LEGITIMATE" -> scan.verdict.equals("LEGITIMATE", ignoreCase = true)
                else -> true
            }
            matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onUrlChanged(newUrl: String) {
        _urlInput.value = newUrl
    }

    fun onSearchQueryChanged(q: String) {
        _historySearchQuery.value = q
    }

    fun onFilterSelected(filter: String) {
        _historyFilter.value = filter
    }

    fun setAuthDialogOpen(open: Boolean) {
        _isAuthDialogOpen.value = open
    }

    fun toggleRole() {
        val current = currentUser.value ?: return
        val newRole = if (current.role == "ADMIN") "USER" else "ADMIN"
        authManager.switchUserRole(newRole)
    }

    fun setAdminRole(isAdmin: Boolean) {
        authManager.switchUserRole(if (isAdmin) "ADMIN" else "USER")
    }

    fun signInWithGoogle() {
        viewModelScope.launch {
            authManager.signInWithGoogle()
        }
    }

    fun signInWithEmail(email: String, pass: String) {
        viewModelScope.launch {
            authManager.signInWithEmail(email, pass)
        }
    }

    fun registerWithEmail(email: String, pass: String) {
        viewModelScope.launch {
            authManager.registerWithEmail(email, pass)
        }
    }

    fun signOut() {
        authManager.signOut()
    }

    fun clearAuthError() {
        authManager.clearError()
    }

    fun resetScanState() {
        _scanState.value = ScanUiState.Idle
    }

    fun scanUrl(targetUrl: String? = null) {
        val url = (targetUrl ?: _urlInput.value).trim()
        if (url.isBlank()) {
            _scanState.value = ScanUiState.Error("Please enter a valid website URL.")
            return
        }

        viewModelScope.launch {
            try {
                _scanState.value = ScanUiState.Scanning("Deconstructing URL structure...")
                delay(250)
                _scanState.value = ScanUiState.Scanning("Extracting 18 lexical & domain features...")
                delay(300)
                _scanState.value = ScanUiState.Scanning("Running Random Forest threat evaluation & Firestore sync...")
                delay(300)

                val uid = currentUser.value?.uid
                val result = repository.performScan(url, uid)
                _scanState.value = ScanUiState.Success(result)
            } catch (e: Exception) {
                _scanState.value = ScanUiState.Error("Analysis error: ${e.localizedMessage ?: "Unknown failure"}")
            }
        }
    }

    fun deleteScan(id: Long) {
        viewModelScope.launch {
            repository.deleteScan(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }
}
