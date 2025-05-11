package com.example.boxboxd.view.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.boxboxd.R
import com.example.boxboxd.viewmodel.AccountViewModel
import com.example.boxboxd.viewmodel.AuthViewModel

@Composable
fun RegistrationScreen(
    authViewModel: AuthViewModel,
    accountViewModel: AccountViewModel
) {
    val context = LocalContext.current
    val username = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val isLoading = authViewModel.isLoading

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.registration_title),
            color = MaterialTheme.colorScheme.tertiary,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        TextField(
            value = username.value,
            onValueChange = { username.value = it },
            label = {
                Text(
                    stringResource(id = R.string.registration_username_label),
                    color = MaterialTheme.colorScheme.tertiary
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
                    stringResource(id = R.string.registration_password_label),
                    color = MaterialTheme.colorScheme.tertiary
                )
            },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        val registrationInvalidEmail = stringResource(id = R.string.login_invalid_email)
        val registrationEmptyFields = stringResource(id = R.string.registration_empty_fields)

        Button(
            onClick = {
                val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
                if (username.value.isNotEmpty() && password.value.isNotEmpty()) {
                    if (username.value.matches(Regex(emailPattern))) {
                        authViewModel.createAccount(username.value, password.value, context)
                    } else {
                        Toast.makeText(context, registrationInvalidEmail, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, registrationEmptyFields, Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondary),
            shape = RoundedCornerShape(30.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.tertiary
                )
            } else {
                Text(
                    text = stringResource(id = R.string.registration_button),
                    color = MaterialTheme.colorScheme.tertiary,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = {
                accountViewModel.requestNavigateToLoginScreen()
            },
            enabled = !isLoading
        ) {
            Text(
                text = stringResource(id = R.string.registration_return_to_main),
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}