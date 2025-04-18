package com.example.boxboxd.view.widgets

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.boxboxd.R
import com.example.boxboxd.core.jolpica.Race
import com.example.boxboxd.viewmodel.AccountViewModel
import com.example.boxboxd.viewmodel.RacesViewModel

@Composable
fun ReviewColumn(
    race: Race,
    racesViewModel: RacesViewModel,
    accountViewModel: AccountViewModel
) {
    val raceEntries = racesViewModel.raceEntries.collectAsState()

    LaunchedEffect(race, accountViewModel.userId) {
        racesViewModel.getRaceEntries(race, accountViewModel.userId)
    }

    Text(
        text = stringResource(R.string.entries),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier
            .padding(top = 20.dp, bottom = 10.dp)
    )

    LazyColumn {
        if (raceEntries.value.isEmpty()) {
            item {
                Text(
                    text ="No entries found for ${race.raceName}",
                    style = MaterialTheme.typography.bodyMedium
                )

            }
        } else {
            items(raceEntries.value) { entry ->
                EntryCard(entry, accountViewModel, racesViewModel)
            }
        }
    }
}