package com.example.boxboxd.view.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.boxboxd.R
import com.example.boxboxd.core.inner.collectAsStateDelegate
import com.example.boxboxd.view.widgets.EntryCardOnUserScreen
import com.example.boxboxd.viewmodel.AccountViewModel
import com.example.boxboxd.viewmodel.RacesViewModel

@Composable
fun UserEntriesScreen (
    racesViewModel: RacesViewModel,
    accountViewModel: AccountViewModel
) {

    val userEntries by accountViewModel.userEntries.collectAsStateDelegate()

    Box {
        Column(modifier = Modifier
            .fillMaxSize()
            .align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                if (userEntries.isNullOrEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = stringResource(R.string.no_lists_found),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(userEntries ?: emptyList()) { entry ->
                        EntryCardOnUserScreen(
                            entry = entry,
                            racesViewModel = racesViewModel,
                            accountViewModel = accountViewModel
                        )
                    }
                }
            }
        }
    }
}