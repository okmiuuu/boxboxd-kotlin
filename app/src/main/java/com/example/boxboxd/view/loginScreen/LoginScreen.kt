package com.example.boxboxd.view.loginScreen

import android.content.Intent
import android.widget.GridLayout
import android.widget.Toast
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
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
import androidx.navigation.NavHostController
import com.example.boxboxd.R
import com.example.boxboxd.core.inner.objects.Routes

@Composable
fun LoginScreen(
    onGoogleSignIn: () -> Unit,
    onResetPassword: (String) -> Unit,
    onSignInWithEmail: (String, String) -> Unit,
    navController: NavHostController
) {
    val username = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val showDialog = remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (showDialog.value) {
        ForgotPasswordDialog(onDismiss = { showDialog.value = false }, onReset = { email ->
            onResetPassword(email)
            showDialog.value = false
        })
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
            style = MaterialTheme.typography.titleLarge,
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(id = R.string.login_title),
            color = MaterialTheme.colorScheme.tertiary,
            style = MaterialTheme.typography.titleLarge,
        )

        Spacer(modifier = Modifier.height(32.dp))

        TextField(
            value = username.value,
            onValueChange = { username.value = it },
            label = { Text(stringResource(id = R.string.login_email_label), color = MaterialTheme.colorScheme.tertiary, style = MaterialTheme.typography.bodyMedium) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = password.value,
            onValueChange = { password.value = it },
            label = { Text(stringResource(id = R.string.login_password_label), color = MaterialTheme.colorScheme.tertiary, style = MaterialTheme.typography.bodyMedium) },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )

        TextButton(
            onClick = { showDialog.value = true },
            Modifier.fillMaxWidth()
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
                        onSignInWithEmail(username.value, password.value)
                    } else {
                        Toast.makeText(context, loginInvalidEmail, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, loginEmptyFields, Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondary),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                text = stringResource(id = R.string.login_button),
                color = MaterialTheme.colorScheme.tertiary,
                style = MaterialTheme.typography.titleMedium,
            )
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
            onClick = { onGoogleSignIn() },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            ),
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.google),
                    contentDescription = "google",
                    Modifier
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

        TextButton(onClick = {
            navController.navigate(Routes.REGISTRATION)
        }) {
            Text(
                text = stringResource(id = R.string.login_register),
                color = MaterialTheme.colorScheme.tertiary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}