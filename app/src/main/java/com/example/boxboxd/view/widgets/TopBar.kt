package com.example.boxboxd.view.widgets

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import coil.transform.CircleCropTransformation
import com.example.boxboxd.R
import com.example.boxboxd.core.inner.objects.Routes
import com.example.boxboxd.viewmodel.AccountViewModel
import com.google.gson.Gson
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    isMainPage : Boolean = false,
    isUserPage : Boolean = false,
    accountViewModel: AccountViewModel
) {
    var title = stringResource(R.string.app_name).uppercase()
    var fontSize  = 28.sp

    val userState = accountViewModel.userObject.collectAsState()
    val currentUser = userState.value

    if (isUserPage) {
        title = currentUser.username ?: ""
        fontSize = 24.sp
    }

    TopAppBar(
        title = {
            Row(
                modifier = Modifier
                    .fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,

            ) {
                if (!isMainPage) {
                    IconButton(
                        onClick = {
                            accountViewModel.navigateBack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Back",
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontSize = fontSize)
                Spacer(modifier = Modifier.weight(1f))
                if (isMainPage) {
                    UserImage(user = currentUser,
                        modifier = Modifier
                            .padding(10.dp)
                            .height(35.dp)
                            .width(35.dp)
                            .clickable(onClick = {
                                accountViewModel.navigateToUserScreen(currentUser)
                            })
                    )
                }
            }
        },
        modifier = Modifier
            .height(80.dp),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    )
}