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
import android.util.Log
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.layout.onGloballyPositioned
import com.example.boxboxd.core.jolpica.Race
import com.example.boxboxd.model.TintedPainter
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
    var isExpanded by remember { mutableStateOf(customList.id?.let { accountViewModel.getListExpandedState(it) } ?: false) }
    var showMenu by remember { mutableStateOf(false) }
    var showCardMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDeleteRaceDialog by remember { mutableStateOf(false) }
    val racesCount = customList.listItems?.size ?: 0
    var cardWidth by remember { mutableFloatStateOf(0f) }
    var cardHeight by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current
    val raceToDelete = remember { mutableStateOf<Race?>(null) }

    var cardCenterX by remember { mutableFloatStateOf(0f) }

    val placeholderPainter = TintedPainter(
        painter = painterResource(R.drawable.placeholder),
        tint = MaterialTheme.colorScheme.tertiary
    )
    val errorPainter = TintedPainter(
        painter = painterResource(R.drawable.placeholder),
        tint = MaterialTheme.colorScheme.tertiary
    )

    Box {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .onGloballyPositioned { coordinates ->
                    cardWidth = coordinates.size.width.toFloat()
                    cardHeight = coordinates.size.height.toFloat()
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            if (isMainVersion && racesCount > 0) {
                                val newExpanded = !isExpanded
                                isExpanded = newExpanded
                                customList.id?.let { listId ->
                                    accountViewModel.setListExpandedState(listId, newExpanded)
                                }
                            }
                            onClick()
                        },
                        onLongPress = {
                            if (isMainVersion) {
                                showMenu = true
                            }
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
                        placeholder = placeholderPainter,
                        error = errorPainter,
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
                    isLoading = false,
                    raceItems = customList.listItems ?: emptyList(),
                    title = null,
                    racesViewModel = racesViewModel,
                    accountViewModel = accountViewModel,
                    forCardLongPress = { raceWithPosition ->
                        cardCenterX = raceWithPosition.position
                        showCardMenu = true
                        raceToDelete.value = raceWithPosition.race
                    }
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

                Log.i("cardHeight", cardHeight.toDp().toString())
                Log.i("offsetY", (cardHeight.toDp() - 200.dp).toString())

                DpOffset(
                    x = (cardWidth.toDp() - 120.dp),
                    y = (-cardHeight.toDp())
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


        DropdownMenu(
            expanded = showCardMenu,
            onDismissRequest = { showCardMenu = false },
            offset = with(density) {
                DpOffset(
                    x = (cardCenterX.toDp()),
                    y = (cardHeight.toDp() - 52.dp)
                )
            }
        ) {
            DropdownMenuItem(
                modifier = Modifier.width(112.dp),
                text = {
                    Text(
                        text = "Delete race from list",
                        style = MaterialTheme.typography.titleSmall
                    )
                },
                onClick = {
                    showDeleteRaceDialog = true
                    showCardMenu = false
                }
            )
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete List", style = MaterialTheme.typography.bodyMedium) },
                text = { Text("Are you sure you want to delete ${customList.name ?: "this list"}?", style = MaterialTheme.typography.bodySmall) },
                confirmButton = {
                    TextButton(
                        onClick = {

                            accountViewModel.deleteList(
                                customList = customList,
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

        if (showDeleteRaceDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteRaceDialog = false },
                title = { Text("Delete race from list", style = MaterialTheme.typography.bodyMedium) },
                text = { Text("Are you sure you want to delete race from list?", style = MaterialTheme.typography.bodySmall) },
                confirmButton = {
                    TextButton(
                        onClick = {

                            val delRaceValue = raceToDelete.value

                            if (delRaceValue != null) {


                                accountViewModel.deleteRaceFromList(
                                    race = delRaceValue,
                                    customList = customList,
                                )
                            }


                            showDeleteRaceDialog = false
                        }
                    ) {
                        Text("Delete", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteRaceDialog = false }) {
                        Text("Cancel", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            )
        }
    }
}