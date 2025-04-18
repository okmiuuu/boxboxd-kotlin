package com.example.boxboxd.view.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.boxboxd.R
import com.example.boxboxd.core.inner.collectAsStateDelegate
import com.example.boxboxd.view.widgets.RacesRow
import com.example.boxboxd.viewmodel.AccountViewModel
import com.example.boxboxd.viewmodel.RacesViewModel
import java.time.LocalDate

@Composable
fun MainScreen (
    navController: NavHostController,
    racesViewModel: RacesViewModel,
    accountViewModel: AccountViewModel
) {

    val racesThisSeason by racesViewModel.racesThisSeason.collectAsStateDelegate()
    val isLoadingThisSeason by racesViewModel.isLoadingThisSeason.collectAsStateDelegate()

    LaunchedEffect(Unit) {
        //racesViewModel.fetchAllRaces()
        racesViewModel.fetchRacesForSeason(LocalDate.now().year)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLoadingThisSeason) {
            CircularProgressIndicator(
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Loading...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(8.dp)
            )
        } else if (racesThisSeason.isEmpty()) {
            Text(
                text = "No races found for this season",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            RacesRow(
                raceItems = racesThisSeason,
                title = stringResource(id = R.string.this_season),
                racesViewModel = racesViewModel,
                accountViewModel = accountViewModel
            )
        }
    }

}