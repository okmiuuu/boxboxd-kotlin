package com.example.boxboxd.view.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.boxboxd.core.jolpica.Race
import com.example.boxboxd.view.widgets.RaceCard
import com.example.boxboxd.viewmodel.AccountViewModel
import com.example.boxboxd.viewmodel.RacesViewModel

@Composable
fun RacesScreen(
    racesList: List<Race>,
    racesViewModel: RacesViewModel,
    accountViewModel: AccountViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(racesList.chunked(2)) { pair ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RaceCard(
                    item = pair[0],
                    racesViewModel = racesViewModel,
                    onClick = {

                        accountViewModel.requestNavigateToRaceScreen(pair[0])
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )

                if (pair.size > 1) {
                    RaceCard(
                        item = pair[1],
                        racesViewModel = racesViewModel,
                        onClick = { accountViewModel.requestNavigateToRaceScreen(pair[1]) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}