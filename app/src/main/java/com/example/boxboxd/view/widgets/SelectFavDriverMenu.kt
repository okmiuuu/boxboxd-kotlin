package com.example.boxboxd.view.widgets

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.boxboxd.R
import com.example.boxboxd.viewmodel.AccountViewModel
import com.example.boxboxd.viewmodel.RacesViewModel

@Composable
fun SelectFavDriverMenu(
    accountViewModel: AccountViewModel,
    racesViewModel: RacesViewModel
) {
    val driverNames = remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(Unit) {
        driverNames.value = racesViewModel.getListOfDriverNames()
    }

    Text(
        text = stringResource(R.string.select_fav_driver),
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center
    )

    AutocompleteTextField(
        suggestions = driverNames.value,
        label = stringResource(R.string.type_the_name),
        modifier = Modifier.padding(16.dp)
    )
}