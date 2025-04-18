package com.example.boxboxd

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import co.yml.charts.common.extensions.isNotNull
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.example.boxboxd.core.inner.objects.Routes.LOGIN
import com.example.boxboxd.core.inner.objects.Routes.REGISTRATION
import com.example.boxboxd.core.inner.User
import com.example.boxboxd.ui.theme.BoxboxdTheme
import com.example.boxboxd.view.loginScreen.LoginScreen
import com.example.boxboxd.view.loginScreen.RegistrationScreen

class LoginActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        auth = Firebase.auth

        if (auth.currentUser.isNotNull()) {
            navigateToMainActivity()
            return
        }

        setContent {
            val context = LocalContext.current
            BoxboxdTheme(dynamicColor = false) {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = LOGIN) {
                    composable(LOGIN) {
                        LoginScreen(
                            onGoogleSignIn = { signInWithGoogle() },
                            onResetPassword = { email -> resetPassword(email, context, auth) },
                            onSignInWithEmail = { email, password ->
                                signInWithEmail(email, password, auth, context)
                            },
                            navController
                        )
                    }
                    composable(REGISTRATION) {
                        RegistrationScreen(context, ::createAccount)
                    }
                }
            }
        }
    }

    private fun createAccount(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        baseContext,
                        getString(R.string.registration_success),
                        Toast.LENGTH_SHORT,
                    ).show()
                    navigateToMainActivity()
                } else {
                    Toast.makeText(
                        baseContext,
                        getString(R.string.registration_failed),
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
    }

    private fun signInWithGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, 200)
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        checkUserAndProceed(userId)
                    } else {
                        Toast.makeText(
                            this,
                            "Failed to retrieve user ID",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.google_sign_in_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("GoogleSignIn", "Authentication failed", task.exception)
                }
            }
    }

    private fun checkUserAndProceed(userId: String) {
        val db = Firebase.firestore
        val userDoc = db.collection("users").document(userId)

        userDoc.get().addOnSuccessListener { document ->
            if (!document.exists()) {
                val photoUrl = auth.currentUser?.photoUrl?.toString()?.replace("sz=50", "sz=200")
                val newUser = User(
                    firstLogin = true,
                    id = userId,
                    picture = photoUrl,
                    email = auth.currentUser?.email
                )

                userDoc.set(newUser)
                    .addOnSuccessListener {
                        navigateToMainActivity()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Failed to save user: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            } else {
                navigateToMainActivity()
            }
        }.addOnFailureListener { e ->
            Toast.makeText(
                this,
                "Error checking user: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun resetPassword(email: String, context: Context, auth: FirebaseAuth) {
        auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(
                    context, getString(R.string.password_reset_email_sent, email), Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    context,
                    getString(R.string.error_message, task.exception?.message),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun signInWithEmail(
        email: String,
        password: String,
        auth: FirebaseAuth,
        context: Context,
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        checkUserAndProceed(userId)
                    }
                } else {
                    Toast.makeText(
                        context,
                        getString(R.string.authentication_error),
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
    }

    // go to the main activity after login
    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    // try to login as user
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 200) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                Log.e("GoogleSignIn", "Google Sign-In failed", e)
                Toast.makeText(
                    this,
                    "Google Sign-In failed: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}