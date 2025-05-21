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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.boxboxd.R
import com.example.boxboxd.core.inner.User
import com.example.boxboxd.ui.theme.White
import com.example.boxboxd.viewmodel.AccountViewModel
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

    // Collect userObject and isLoading states
    val userObject = accountViewModel.userObject.collectAsState().value
    val isLoading = accountViewModel.isLoading.collectAsState().value
    val scope = rememberCoroutineScope()

    // Log user state for debugging
    Log.d("TopBar", "Recomposing with UserObject: id=${userObject.id}, username=${userObject.username}, email=${userObject.email}, isLoading=$isLoading")

    if (isUserPage) {
        title = userObject.username ?: "User"
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
                            Log.d("TopBar", "Back button clicked")
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
                            Log.d("TopBar", "Search button clicked")
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
                    if (isLoading) {
                        AsyncImage(
                            model = R.drawable.user,
                            contentDescription = "Loading Profile",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .padding(10.dp)
                                .height(35.dp)
                                .width(35.dp),
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.tertiary)
                        )
                    } else {
                        UserImage(
                            user = userObject,
                            modifier = Modifier
                                .padding(10.dp)
                                .height(35.dp)
                                .width(35.dp)
                                .clickable(onClick = {
                                    scope.launch {
                                        if (userObject.id != null) {
                                            Log.d("TopBar", "Navigating to UserScreen for user: ${userObject.id}")
                                            accountViewModel.requestNavigateToUserScreen(userObject)
                                        } else {
                                            Log.d("TopBar", "Navigating to LoginScreen")
                                            accountViewModel.requestNavigateToLoginScreen()
                                        }
                                    }
                                })
                        )
                    }
                }
            }
        },
        modifier = Modifier.height(80.dp),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    )
}