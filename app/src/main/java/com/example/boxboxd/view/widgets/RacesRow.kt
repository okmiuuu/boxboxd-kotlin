package com.example.boxboxd.view.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.boxboxd.core.inner.objects.Routes
import com.example.boxboxd.core.jolpica.Race
import com.example.boxboxd.viewmodel.AccountViewModel
import com.example.boxboxd.viewmodel.RacesViewModel
import com.google.gson.Gson
import java.net.URLEncoder

@Composable
fun RacesRow(
    raceItems: List<Race>,
    title : String?,
    modifier: Modifier = Modifier,
    racesViewModel: RacesViewModel,
    accountViewModel: AccountViewModel
) {
    Column(
        modifier = modifier
    ) {
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        LazyRow(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items = raceItems) { raceItem ->
                RaceCard(
                    item = raceItem,
                    racesViewModel = racesViewModel,
                    onClick = {
                        accountViewModel.navigateToRaceScreen(raceItem)
                    }
                )
            }
        }
    }
}