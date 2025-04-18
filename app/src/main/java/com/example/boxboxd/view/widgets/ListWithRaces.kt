package com.example.boxboxd.view.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.boxboxd.core.inner.CustomList
import com.example.boxboxd.viewmodel.AccountViewModel
import com.example.boxboxd.viewmodel.RacesViewModel

@Composable
fun ListWithRaces(
    customList: CustomList,
    accountViewModel: AccountViewModel,
    racesViewModel: RacesViewModel,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row {
            Text(
                text = customList.name ?: "",
                style = MaterialTheme.typography.titleMedium
            )
        }

        RacesRow(
            raceItems = customList.listItems ?: emptyList(),
            title = null,
            racesViewModel = racesViewModel,
            accountViewModel = accountViewModel
        )


    }
}