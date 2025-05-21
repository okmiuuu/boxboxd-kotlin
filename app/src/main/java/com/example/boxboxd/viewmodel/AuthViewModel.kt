package com.example.boxboxd.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.boxboxd.R
import com.example.boxboxd.core.inner.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(private val accountViewModel: AccountViewModel) : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    var isLoading by mutableStateOf(false)
        private set

    sealed class AuthState {
        object Idle : AuthState()
        object Success : AuthState()
        data class Error(val message: String) : AuthState()
    }

    init {
        auth.addAuthStateListener { firebaseAuth ->
            Log.d("AuthViewModel", "Auth state changed: email=${firebaseAuth.currentUser?.email}, uid=${firebaseAuth.currentUser?.uid}")
            viewModelScope.launch {
                if (firebaseAuth.currentUser == null) {
                    Log.d("AuthViewModel", "No user signed in, setting Idle state")
                    _authState.value = AuthState.Idle
                    accountViewModel.clearUserObject()
                    accountViewModel.requestNavigateToLoginScreen()
                } else {
                    Log.d("AuthViewModel", "User signed in, checking user document")
                    val userId = firebaseAuth.currentUser?.uid ?: return@launch
                    checkUserAndProceed(userId, null)
                    accountViewModel.fetchUserData()
                    _authState.value = AuthState.Success
                }
            }
        }
    }

    fun signInWithEmail(email: String, password: String, context: Context) {
        isLoading = true
        Log.d("AuthViewModel", "signInWithEmail: Attempting login with email=$email")
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                val userId = auth.currentUser?.uid ?: throw Exception("User ID not found")
                Log.d("AuthViewModel", "signInWithEmail: Login successful, userId=$userId")
                checkUserAndProceed(userId, context)
                accountViewModel.fetchUserData()
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                Log.e("AuthViewModel", "signInWithEmail: Failed: ${e.message}", e)
                _authState.value = AuthState.Error(e.message ?: "Authentication failed")
                Toast.makeText(context, "Authentication error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                isLoading = false
                Log.d("AuthViewModel", "signInWithEmail: Completed, isLoading=$isLoading")
            }
        }
    }

    fun createAccount(email: String, password: String, context: Context) {
        isLoading = true
        Log.d("AuthViewModel", "createAccount: Attempting to create account with email=$email")
        viewModelScope.launch {
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
                val userId = auth.currentUser?.uid ?: throw Exception("User ID not found")
                Log.d("AuthViewModel", "createAccount: Account created, userId=$userId")
                checkUserAndProceed(userId, context)
                accountViewModel.fetchUserData()
                _authState.value = AuthState.Success
                Toast.makeText(context, "Registration successful", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "createAccount: Failed: ${e.message}", e)
                _authState.value = AuthState.Error(e.message ?: "Registration failed")
                Toast.makeText(context, "Registration failed: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                isLoading = false
                Log.d("AuthViewModel", "createAccount: Completed, isLoading=$isLoading")
            }
        }
    }

    fun resetPassword(email: String, context: Context) {
        Log.d("AuthViewModel", "resetPassword: Sending reset email to $email")
        viewModelScope.launch {
            try {
                auth.sendPasswordResetEmail(email).await()
                Log.d("AuthViewModel", "resetPassword: Email sent successfully")
                Toast.makeText(context, "Password reset email sent to $email", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "resetPassword: Failed: ${e.message}", e)
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun signInWithGoogle(account: GoogleSignInAccount?, context: Context) {
        isLoading = true
        Log.d("AuthViewModel", "signInWithGoogle: Attempting Google sign-in")
        viewModelScope.launch {
            try {
                val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
                auth.signInWithCredential(credential).await()
                val userId = auth.currentUser?.uid ?: throw Exception("User ID not found")
                Log.d("AuthViewModel", "signInWithGoogle: Login successful, userId=$userId")
                checkUserAndProceed(userId, context)
                // Remove fetchUserData() call here to avoid duplicate
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                Log.e("AuthViewModel", "signInWithGoogle: Failed: ${e.message}", e)
                _authState.value = AuthState.Error(e.message ?: "Google Sign-In failed")
                Toast.makeText(context, "Google Sign-In failed: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                isLoading = false
                Log.d("AuthViewModel", "signInWithGoogle: Completed, isLoading=$isLoading")
            }
        }
    }

    fun signOut(context: Context) {
        isLoading = true
        Log.d("AuthViewModel", "signOut: Signing out")
        viewModelScope.launch {
            try {
                auth.signOut()
                val gso = getGoogleSignInIntent(context)
                val googleSignInClient = GoogleSignIn.getClient(context, gso)
                googleSignInClient.signOut().await()
                Log.d("AuthViewModel", "signOut: Sign-out successful")
                _authState.value = AuthState.Idle
                accountViewModel.clearUserObject()
                accountViewModel.requestNavigateToLoginScreen()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "signOut: Failed: ${e.message}", e)
                Toast.makeText(context, "Sign-out failed: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                isLoading = false
                Log.d("AuthViewModel", "signOut: Completed, isLoading=$isLoading")
            }
        }
    }

    fun handleGoogleSignInError(message: String, context: Context) {
        Log.e("AuthViewModel", "handleGoogleSignInError: $message")
        _authState.value = AuthState.Error(message)
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private suspend fun checkUserAndProceed(userId: String, context: Context?) {
        Log.d("AuthViewModel", "checkUserAndProceed: Checking userId=$userId")
        val db = Firebase.firestore
        val userDoc = db.collection("users").document(userId)
        try {
            val document = userDoc.get().await()
            if (!document.exists()) {
                Log.d("AuthViewModel", "checkUserAndProceed: Creating new user document")
                val photoUrl = auth.currentUser?.photoUrl?.toString()?.replace("sz=50", "sz=200")
                val newUser = User(
                    firstLogin = true,
                    id = userId,
                    picture = photoUrl,
                    email = auth.currentUser?.email,
                    username = auth.currentUser?.email?.substringBefore("@")
                )
                userDoc.set(newUser).await()
                Log.d("AuthViewModel", "checkUserAndProceed: User document created for userId=$userId")
            } else {
                Log.d("AuthViewModel", "checkUserAndProceed: User document exists for userId=$userId")
            }
        } catch (e: Exception) {
            Log.e("AuthViewModel", "checkUserAndProceed: Failed: ${e.message}", e)
            context?.let {
                Toast.makeText(it, "Error checking user: ${e.message}", Toast.LENGTH_LONG).show()
            }
            throw e
        }
    }

    fun getGoogleSignInIntent(context: Context): GoogleSignInOptions {
        Log.d("AuthViewModel", "getGoogleSignInIntent: Creating Google Sign-In intent")
        return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }

    fun resetAuthState() {
        Log.d("AuthViewModel", "resetAuthState: Setting authState to Idle")
        _authState.value = AuthState.Idle
    }
}