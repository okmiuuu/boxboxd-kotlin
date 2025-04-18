package com.example.boxboxd.view.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.boxboxd.R
import com.example.boxboxd.core.inner.CustomList
import android.net.Uri
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.layout.onGloballyPositioned
import com.example.boxboxd.viewmodel.AccountViewModel
import com.example.boxboxd.viewmodel.RacesViewModel

@Composable
fun ListCard(
    customList: CustomList,
    isMainVersion: Boolean = true,
    racesViewModel: RacesViewModel,
    accountViewModel: AccountViewModel,
    onEditList: (CustomList) -> Unit,
    onClick: () -> Unit
) {

    var isExpanded by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val racesCount = customList.listItems?.size ?: 0
    var cardWidth by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current

    Box {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .onGloballyPositioned { coordinates ->
                    cardWidth = coordinates.size.width.toFloat()
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            if (isMainVersion) {
                                isExpanded = !isExpanded
                            }
                            onClick()
                        },
                        onLongPress = {
                            showMenu = true
                        }
                    )
                }
        ) {
            Row(
                modifier = Modifier
                    .height(80.dp)
                    .fillMaxSize()
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = customList.picture?.let { uriString ->
                            try {
                                Uri.parse(uriString).also {
                                    println("Parsed Uri: $it")
                                }
                            } catch (e: Exception) {
                                println("Uri parsing failed: ${e.message}")
                                null
                            }
                        },
                        placeholder = painterResource(R.drawable.user),
                        error = painterResource(R.drawable.heart)
                    ),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .height(80.dp)
                        .width(80.dp)
                        .clip(RoundedCornerShape(5.dp))
                )

                Text(
                    text = customList.name ?: "unknown list",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .align(Alignment.CenterVertically)
                )
            }

            if (isMainVersion && isExpanded) {
                RacesRow(
                    raceItems = customList.listItems ?: emptyList(),
                    title = null,
                    racesViewModel = racesViewModel,
                    accountViewModel = accountViewModel
                )
            } else if (isMainVersion) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = customList.description ?: "",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = "$racesCount ${stringResource(R.string.races)}",
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            offset = with(density) {
                DpOffset(
                    x = (cardWidth.toDp() - 120.dp),
                    y = ((-100).dp)
                )
            }
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = "Edit List",
                        style = MaterialTheme.typography.titleSmall
                    )
                },
                onClick = {
                    onEditList(customList)
                    showMenu = false
                }
            )
            DropdownMenuItem(
                text = {
                    Text(
                        text = "Delete List",
                        style = MaterialTheme.typography.titleSmall
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
                title = { Text("Delete List") },
                text = { Text("Are you sure you want to delete ${customList.name ?: "this list"}?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            accountViewModel.deleteList(
                                customList = customList,

                            )
                            showDeleteDialog = false
                        }
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}