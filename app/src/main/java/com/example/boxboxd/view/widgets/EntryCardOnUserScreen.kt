package com.example.boxboxd.view.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.boxboxd.core.inner.Entry
import com.example.boxboxd.core.inner.objects.MapObjects
import com.example.boxboxd.core.jolpica.Race
import com.example.boxboxd.viewmodel.AccountViewModel
import com.example.boxboxd.viewmodel.RacesViewModel

@Composable
fun EntryCardOnUserScreen (
    entry : Entry,
    racesViewModel: RacesViewModel,
    accountViewModel: AccountViewModel
) {

    val race = entry.race

    race?.let {
        Row (
            modifier = Modifier.height(200.dp).padding(vertical = 10.dp, horizontal = 5.dp)
        ) {
            RaceCard(
                item = race,
                racesViewModel = racesViewModel
            ) {
                accountViewModel.navigateToRaceScreen(race)
            }

            val tyreType = MapObjects.gradeToTyre[entry.rating]
            val mood = entry.mood

            Column (
                modifier = Modifier.fillMaxSize(),
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

        }
    }
}
