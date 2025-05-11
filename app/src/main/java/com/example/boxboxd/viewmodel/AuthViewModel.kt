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
        // Reset authState when auth state changes to null
        auth.addAuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser == null) {
                Log.d("AuthViewModel", "FirebaseAuth user is null, resetting authState to Idle")
                _authState.value = AuthState.Idle
            }
        }
    }

    fun signInWithEmail(email: String, password: String, context: Context) {
        isLoading = true
        Log.d("AuthViewModel", "Attempting email sign-in with email: $email")
        viewModelScope.launch {
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                Log.d("AuthViewModel", "Email sign-in successful, user: ${result.user?.uid}")
                val userId = auth.currentUser?.uid ?: throw Exception("User ID not found")
                checkUserAndProceed(userId, context)
                _authState.value = AuthState.Success
                Log.d("AuthViewModel", "Set authState to Success")
                accountViewModel.fetchUserData()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Email sign-in failed: ${e.message}", e)
                _authState.value = AuthState.Error(e.message ?: "Authentication failed")
                Toast.makeText(context, "Authentication error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                isLoading = false
            }
        }
    }

    fun createAccount(email: String, password: String, context: Context) {
        isLoading = true
        Log.d("AuthViewModel", "Attempting to create account with email: $email")
        viewModelScope.launch {
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                Log.d("AuthViewModel", "Account creation successful, user: ${result.user?.uid}")
                val userId = auth.currentUser?.uid ?: throw Exception("User ID not found")
                checkUserAndProceed(userId, context)
                _authState.value = AuthState.Success
                Log.d("AuthViewModel", "Set authState to Success")
                accountViewModel.fetchUserData()
                Toast.makeText(context, "Registration successful", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Account creation failed: ${e.message}", e)
                _authState.value = AuthState.Error(e.message ?: "Registration failed")
                Toast.makeText(context, "Registration failed: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                isLoading = false
            }
        }
    }

    fun resetPassword(email: String, context: Context) {
        Log.d("AuthViewModel", "Attempting to reset password for email: $email")
        viewModelScope.launch {
            try {
                auth.sendPasswordResetEmail(email).await()
                Log.d("AuthViewModel", "Password reset email sent to $email")
                Toast.makeText(context, "Password reset email sent to $email", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Password reset failed: ${e.message}", e)
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun signInWithGoogle(account: GoogleSignInAccount?, context: Context) {
        isLoading = true
        Log.d("AuthViewModel", "Attempting Google sign-in")
        viewModelScope.launch {
            try {
                val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
                val result = auth.signInWithCredential(credential).await()
                Log.d("AuthViewModel", "Google sign-in successful, user: ${result.user?.uid}")
                val userId = auth.currentUser?.uid ?: throw Exception("User ID not found")
                checkUserAndProceed(userId, context)
                _authState.value = AuthState.Success
                Log.d("AuthViewModel", "Set authState to Success")
                accountViewModel.fetchUserData()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Google sign-in failed: ${e.message}", e)
                _authState.value = AuthState.Error(e.message ?: "Google Sign-In failed")
                Toast.makeText(context, "Google Sign-In failed: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                isLoading = false
            }
        }
    }

    fun handleGoogleSignInError(message: String, context: Context) {
        Log.e("AuthViewModel", "Google sign-in error: $message")
        _authState.value = AuthState.Error(message)
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private suspend fun checkUserAndProceed(userId: String, context: Context) {
        Log.d("AuthViewModel", "Checking user: $userId")
        val db = Firebase.firestore
        val userDoc = db.collection("users").document(userId)
        try {
            val document = userDoc.get().await()
            if (!document.exists()) {
                Log.d("AuthViewModel", "User does not exist, creating new user")
                val photoUrl = auth.currentUser?.photoUrl?.toString()?.replace("sz=50", "sz=200")
                val newUser = User(
                    firstLogin = true,
                    id = userId,
                    picture = photoUrl,
                    email = auth.currentUser?.email,
                    username = auth.currentUser?.email?.substringBefore("@")
                )
                userDoc.set(newUser).await()
                Log.d("AuthViewModel", "User created: $userId")
            } else {
                Log.d("AuthViewModel", "User exists: $userId")
            }
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Error checking user: ${e.message}", e)
            Toast.makeText(context, "Error checking user: ${e.message}", Toast.LENGTH_LONG).show()
            throw e
        }
    }

    fun getGoogleSignInIntent(context: Context): GoogleSignInOptions {
        Log.d("AuthViewModel", "Creating Google Sign-In intent")
        return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }

    fun resetAuthState() {
        Log.d("AuthViewModel", "Resetting authState to Idle")
        _authState.value = AuthState.Idle
    }
}