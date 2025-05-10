package com.example.boxboxd.view.widgets

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.example.boxboxd.R
import com.example.boxboxd.core.inner.Entry
import com.example.boxboxd.core.inner.User
import com.example.boxboxd.core.inner.collectAsStateDelegate
import com.example.boxboxd.core.inner.objects.MapObjects
import com.example.boxboxd.viewmodel.AccountViewModel
import com.example.boxboxd.viewmodel.RacesViewModel

@Composable
fun EntryCard(
    entry: Entry,
    accountViewModel: AccountViewModel,
    racesViewModel: RacesViewModel
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var cardPosition by remember { mutableStateOf(DpOffset(0.dp, 0.dp)) }
    var cardSize by remember { mutableStateOf(Pair(0f, 0f)) } // Width and height
    val density = LocalDensity.current


    val isAdminDelegate by accountViewModel.isAdmin.collectAsStateDelegate()
    val isAdmin = isAdminDelegate ?: false


    //val colorActive = MaterialTheme.colorScheme.error
    //val colorNotActive = MaterialTheme.colorScheme.background

    //val isEntryLikedByCurrentUser = remember { mutableStateOf(racesViewModel.isEntryLikedByCurrentUser(entry, accountViewModel.userObject.value )) }

    //val firstColor = if(isEntryLikedByCurrentUser.value) { colorActive } else { colorNotActive }

    //val colorLike = remember { mutableStateOf(firstColor) }

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
                    .padding(vertical = 10.dp, horizontal = 2.dp)
                    .height(35.dp)
                    .width(35.dp)
                    .clickable(onClick = {
                        val entryUser = entry.user
                        if (entryUser != null) {
                            accountViewModel.requestNavigateToUserScreen(entry.user)
                        }
                    })
            )

            Text(
                text = entry.text ?: "No comment",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth(0.7f)

            )

            Box(
                modifier = Modifier
                    .padding(10.dp)
            ) {
                val tyreGrade = MapObjects.gradeToTyre[entry.rating]
                tyreGrade?.let {
                    TyreComponent(
                        tyreGrade,
                    )
                }
            }

        }

        if (isAdmin) {
            Icon(
                painter = painterResource(R.drawable.more_horiz),
                contentDescription = "Options",
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier
                    .size(20.dp)
                    .clickable {
                        showMenu = true
                    }
            )
        }




        //likes for the future. now works kinda bad
//        Row (
//            modifier = Modifier
//                .padding(horizontal = 12.dp)
//                .clickable {
//                    if (!isEntryLikedByCurrentUser.value) {
//                        Log.i("entryCard", "adding like")
//                        accountViewModel.addLikeToEntry(entry)
//                        colorLike.value = colorActive
//                    } else {
//                        Log.i("entryCard", "removing like")
//                        accountViewModel.removeLikeFromEntry(entry)
//                        colorLike.value = colorNotActive
//                    }
//                }
//        ){
//            Icon(
//                painter = painterResource(R.drawable.heart),
//                contentDescription = "Likes",
//                tint = colorLike.value,
//                modifier = Modifier.size(16.dp)
//            )
//            Spacer(modifier = Modifier.width(4.dp))
//            Text(
//                text = (entry.likesFrom?.size ?: 0).toString(),
//                style = MaterialTheme.typography.bodySmall,
//                color = colorLike.value
//            )
//        }
    }

    DropdownMenu(
        expanded = showMenu,
        onDismissRequest = { showMenu = false },
        offset = with(density) {
            DpOffset(
                x = cardPosition.x + cardSize.first.toDp() - 140.dp,
                y = cardPosition.y
            )
        }
    ) {
        DropdownMenuItem(
            text = {
                Text(
                    text = "Delete entry",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            onClick = {
                showDeleteDialog = true
                showMenu = false
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete entry", style = MaterialTheme.typography.bodyMedium) },
            text = { Text("Are you sure you want to delete this entry?", style = MaterialTheme.typography.bodySmall) },
            confirmButton = {
                TextButton(
                    onClick = {
                        accountViewModel.deleteEntry(
                            entry = entry,
                        )
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", style = MaterialTheme.typography.bodyMedium)
                }
            }
        )
    }
}