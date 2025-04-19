package com.example.boxboxd.view.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.boxboxd.core.jolpica.Driver
import com.example.boxboxd.view.widgets.DriverCard
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

    val racesLastSeason by racesViewModel.racesLastSeason.collectAsStateDelegate()
    val isLoadingLastSeason by racesViewModel.isLoadingLastSeason.collectAsStateDelegate()

    val userLists by accountViewModel.userLists.collectAsStateDelegate()

    LaunchedEffect(Unit) {
        //racesViewModel.fetchAllRaces()
        racesViewModel.fetchRacesForThisSeason(LocalDate.now().year)
        racesViewModel.fetchRacesForLastSeason(LocalDate.now().year)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            val driver = Driver(
                code = "VER",
                url = "http://en.wikipedia.org/wiki/Max_Verstappen",
                dateOfBirth = "1997-09-30",
                driverId = "max_verstappen",
                familyName = "Verstappen",
                givenName = "Max",
                nationality = "Dutch",
                permanentNumber = 33
            )

            DriverCard(driver)
        }


        item {
            RacesRow(
                title = stringResource(id = R.string.this_season),
                raceItems = racesThisSeason,
                isLoading = isLoadingThisSeason,
                racesViewModel = racesViewModel,
                accountViewModel = accountViewModel
            )
        }

        item {
            RacesRow(
                title = stringResource(id = R.string.last_season),
                raceItems = racesLastSeason,
                isLoading = isLoadingLastSeason,
                racesViewModel = racesViewModel,
                accountViewModel = accountViewModel
            )
        }

        userLists?.forEach { list ->
            if (!list.listItems.isNullOrEmpty()) {
                item {
                    RacesRow(
                        title = list.name,
                        raceItems = list.listItems,
                        racesViewModel = racesViewModel,
                        accountViewModel = accountViewModel
                    )
                }
            }
        }
    }

}