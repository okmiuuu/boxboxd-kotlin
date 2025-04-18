package com.example.boxboxd.view.widgets

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.boxboxd.R
import com.example.boxboxd.core.inner.Entry
import com.example.boxboxd.core.inner.User
import com.example.boxboxd.core.inner.objects.Routes
import com.example.boxboxd.core.jolpica.Race
import com.example.boxboxd.viewmodel.AccountViewModel
import com.example.boxboxd.viewmodel.RacesViewModel
import com.google.gson.Gson
import java.net.URLEncoder

@Composable
fun EntryCard(
    entry: Entry,
    accountViewModel: AccountViewModel,
    racesViewModel: RacesViewModel
) {

    val colorActive = MaterialTheme.colorScheme.error
    val colorNotActive = MaterialTheme.colorScheme.background


    val isEntryLikedByCurrentUser = remember { mutableStateOf(racesViewModel.isEntryLikedByCurrentUser(entry, accountViewModel.userObject.value )) }

    val firstColor = if(isEntryLikedByCurrentUser.value) { colorActive } else { colorNotActive }

    val colorLike = remember { mutableStateOf(firstColor) }

    Column (
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(2.dp)
            )
            .padding(12.dp)
    ) {
        Row {
            UserImage(
                user = entry.user ?: User(),
                modifier = Modifier
                    .padding(10.dp)
                    .height(35.dp)
                    .width(35.dp)
                    .clickable(onClick = {
                        val entryUser = entry.user
                        if (entryUser != null) {
                            accountViewModel.navigateToUserScreen(entry.user)
                        }
                    })
            )


            Text(
                text = entry.text ?: "No comment",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(10.dp)
            )
        }

        Row (
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .clickable {
                    if (!isEntryLikedByCurrentUser.value) {
                        Log.i("entryCard", "adding like")
                        accountViewModel.addLikeToEntry(entry)
                        colorLike.value = colorActive
                    } else {
                        Log.i("entryCard", "removing like")
                        accountViewModel.removeLikeFromEntry(entry)
                        colorLike.value = colorNotActive
                    }
                }
        ){
            Icon(
                painter = painterResource(R.drawable.heart),
                contentDescription = "Likes",
                tint = colorLike.value,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = (entry.likesFrom?.size ?: 0).toString(),
                style = MaterialTheme.typography.bodySmall,
                color = colorLike.value
            )
        }
    }
}