package com.example.boxboxd.view.widgets

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.boxboxd.R
import com.example.boxboxd.core.inner.User
import com.example.boxboxd.viewmodel.AccountViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    isMainPage: Boolean = false,
    isUserPage: Boolean = false,
    accountViewModel: AccountViewModel,
    onSearchClick: () -> Unit = {}
) {
    var title = stringResource(R.string.app_name).uppercase()
    var fontSize = 28.sp

    // Reactive FirebaseAuth state
    val currentUserState = rememberFirebaseAuthState()
    val currentUser = currentUserState.value
    val userObject = accountViewModel.userObject.collectAsState().value
    val scope = rememberCoroutineScope()

    // Log user state for debugging
    Log.d("TopBar", "CurrentUser: ${currentUser?.email}, UserObject.id: ${userObject.id}")

    // Update userObject when Firebase user changes
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            accountViewModel.fetchUserData()
        } else {
            accountViewModel.clearUserObject()
        }
    }

    if (isUserPage) {
        title = userObject.username ?: ""
        fontSize = 24.sp
    }

    TopAppBar(
        title = {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isMainPage) {
                    IconButton(
                        onClick = {
                            accountViewModel.requestNavigateBack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Back",
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                } else {
                    IconButton(
                        onClick = {
                            onSearchClick()
                        }
                    ) {
                        Image(
                            painter = painterResource(R.drawable.search),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(40.dp),
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.tertiary)
                        )
                    }
                }
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontSize = fontSize)
                Spacer(modifier = Modifier.weight(1f))
                if (isMainPage) {
                    UserImage(
                        user = userObject,
                        modifier = Modifier
                            .padding(10.dp)
                            .height(35.dp)
                            .width(35.dp)
                            .clickable(onClick = {
                                scope.launch {
                                    // Delay to allow fetchUserData to complete
                                    delay(500)
                                    if (currentUser != null && userObject.id != null) {
                                        Log.d("TopBar", "Navigating to UserScreen for user: ${userObject.id}")
                                        accountViewModel.requestNavigateToUserScreen(userObject)
                                    } else {
                                        Log.d("TopBar", "Navigating to LoginScreen (currentUser: ${currentUser?.email}, userObject.id: ${userObject.id})")
                                        accountViewModel.requestNavigateToLoginScreen()
                                    }
                                }
                            })
                    )
                }
            }
        },
        modifier = Modifier.height(80.dp),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
fun rememberFirebaseAuthState(): State<FirebaseUser?> {
    val auth = FirebaseAuth.getInstance()
    val userState = remember { MutableStateFlow(auth.currentUser) }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            Log.d("TopBar", "Auth state changed: ${firebaseAuth.currentUser?.email}")
            userState.value = firebaseAuth.currentUser
        }
        auth.addAuthStateListener(listener)
        onDispose {
            Log.d("TopBar", "Removing AuthStateListener")
            auth.removeAuthStateListener(listener)
        }
    }

    return userState.collectAsState()
}