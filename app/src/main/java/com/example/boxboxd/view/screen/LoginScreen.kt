package com.example.boxboxd.view.screen

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.boxboxd.R
import com.example.boxboxd.core.jolpica.Race
import com.example.boxboxd.view.widgets.ForgotPasswordDialog
import com.example.boxboxd.viewmodel.AccountViewModel
import com.example.boxboxd.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    accountViewModel: AccountViewModel,
) {
    val context = LocalContext.current
    val username = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val showDialog = remember { mutableStateOf(false) }
    val isLoading = authViewModel.isLoading
    val authState = authViewModel.authState.collectAsState()

    // Log authState changes
    LaunchedEffect(authState.value) {
        Log.d("LoginScreen", "Auth state changed: ${authState.value}")
        when (val state = authState.value) {
            is AuthViewModel.AuthState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            }
            is AuthViewModel.AuthState.Success -> {
                Log.d("LoginScreen", "Login successful, waiting for navigation via fetchUserData")
                // Navigation is handled by AccountViewModel.fetchUserData()
            }
            is AuthViewModel.AuthState.Idle -> {
                Log.d("LoginScreen", "Auth state idle")
            }
        }
    }

    // Launcher for Google Sign-In
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            authViewModel.signInWithGoogle(account, context)
        } catch (e: ApiException) {
            authViewModel.handleGoogleSignInError("Google Sign-In failed: ${e.message}", context)
        }
    }

    if (showDialog.value) {
        ForgotPasswordDialog(
            defaultEmail = username.value,
            onDismiss = { showDialog.value = false },
            onReset = { email ->
                authViewModel.resetPassword(email, context)
                showDialog.value = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.app_name),
            color = MaterialTheme.colorScheme.tertiary,
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(id = R.string.login_title),
            color = MaterialTheme.colorScheme.tertiary,
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(32.dp))

        TextField(
            value = username.value,
            onValueChange = { username.value = it },
            label = {
                Text(
                    stringResource(id = R.string.login_email_label),
                    color = MaterialTheme.colorScheme.tertiary,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = password.value,
            onValueChange = { password.value = it },
            label = {
                Text(
                    stringResource(id = R.string.login_password_label),
                    color = MaterialTheme.colorScheme.tertiary,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            enabled = !isLoading
        )

        TextButton(
            onClick = { showDialog.value = true },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(
                text = stringResource(id = R.string.login_forgot_password),
                color = MaterialTheme.colorScheme.tertiary,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Left,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentSize(align = Alignment.TopStart)
            )
        }

        val loginInvalidEmail = stringResource(id = R.string.login_invalid_email)
        val loginEmptyFields = stringResource(id = R.string.login_empty_fields)

        Button(
            onClick = {
                val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
                if (username.value.isNotEmpty() && password.value.isNotEmpty()) {
                    if (username.value.matches(Regex(emailPattern))) {
                        authViewModel.signInWithEmail(username.value, password.value, context)
                    } else {
                        Toast.makeText(context, loginInvalidEmail, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, loginEmptyFields, Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondary),
            shape = RoundedCornerShape(10.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.tertiary
                )
            } else {
                Text(
                    text = stringResource(id = R.string.login_button),
                    color = MaterialTheme.colorScheme.tertiary,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = stringResource(id = R.string.login_or),
            color = MaterialTheme.colorScheme.tertiary,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                val gso = authViewModel.getGoogleSignInIntent(context)
                val googleSignInClient = GoogleSignIn.getClient(context, gso)
                googleSignInLauncher.launch(googleSignInClient.signInIntent)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            ),
            enabled = !isLoading
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.google),
                    contentDescription = "google",
                    modifier = Modifier
                        .size(50.dp)
                        .padding(end = 10.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.tertiary)
                )
                Text(
                    text = stringResource(id = R.string.login_google),
                    color = MaterialTheme.colorScheme.tertiary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.W700
                )
            }
        }

        TextButton(
            onClick = {
                accountViewModel.requestNavigateToRegistrationScreen()
            },
            enabled = !isLoading
        ) {
            Text(
                text = stringResource(id = R.string.login_register),
                color = MaterialTheme.colorScheme.tertiary,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        TextButton(
            onClick = {
                accountViewModel.requestNavigateToMainScreen()
            },
            enabled = !isLoading
        ) {
            Text(
                text = stringResource(id = R.string.use_as_guest),
                color = MaterialTheme.colorScheme.tertiary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}