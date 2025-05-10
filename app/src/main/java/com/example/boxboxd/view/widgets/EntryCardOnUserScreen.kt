package com.example.boxboxd.view.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.example.boxboxd.R
import com.example.boxboxd.core.inner.Entry
import com.example.boxboxd.core.inner.objects.MapObjects
import com.example.boxboxd.core.jolpica.Race
import com.example.boxboxd.viewmodel.AccountViewModel
import com.example.boxboxd.viewmodel.RacesViewModel

@Composable
fun EntryCardOnUserScreen(
    entry: Entry,
    racesViewModel: RacesViewModel,
    accountViewModel: AccountViewModel
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var cardPosition by remember { mutableStateOf(DpOffset(0.dp, 0.dp)) }
    var cardSize by remember { mutableStateOf(Pair(0f, 0f)) }
    val density = LocalDensity.current

    val race = entry.race

    race?.let {
        Row(
            modifier = Modifier
                .height(200.dp)
                .padding(vertical = 10.dp, horizontal = 5.dp)
                .onGloballyPositioned { coordinates ->
                    // Get the global position of the card
                    val position = coordinates.positionInRoot()
                    cardPosition = with(density) {
                        DpOffset(
                            x = position.x.toDp(),
                            y = position.y.toDp()
                        )
                    }
                    // Get the size of the card
                    cardSize = Pair(
                        coordinates.size.width.toFloat(),
                        coordinates.size.height.toFloat()
                    )
                }
        ) {
            RaceCard(
                item = race,
                racesViewModel = racesViewModel
            ) {
                accountViewModel.requestNavigateToRaceScreen(race)
            }

            val tyreType = MapObjects.gradeToTyre[entry.rating]
            val mood = entry.mood

            Column(
                modifier = Modifier.fillMaxWidth(0.9f),
                verticalArrangement = Arrangement.Center
            ) {
                tyreType?.let {
                    TyreWithReactionRow(
                        tyreType
                    )
                }

                mood?.let {
                    MoodWithNameRow(
                        mood
                    )
                }
            }

            Icon(
                painter = painterResource(R.drawable.more_vert),
                contentDescription = "Options",
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier
                    .size(20.dp)
                    .clickable {
                        showMenu = true
                    }
            )
        }
    }

    DropdownMenu(
        expanded = showMenu,
        onDismissRequest = { showMenu = false },
        offset = with(density) {
            DpOffset(
                x = cardPosition.x + cardSize.first.toDp() - 140.dp, // Align to right side of card
                y = cardPosition.y // Align to top of card
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