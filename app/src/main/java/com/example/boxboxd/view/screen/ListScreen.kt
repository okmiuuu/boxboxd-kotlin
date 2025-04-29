package com.example.boxboxd.view.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.boxboxd.view.widgets.ChooseListMenu
import com.example.boxboxd.viewmodel.AccountViewModel
import com.example.boxboxd.viewmodel.RacesViewModel

@Composable
fun ListScreen(
    racesViewModel: RacesViewModel,
    accountViewModel: AccountViewModel
) {
    ChooseListMenu(
        racesViewModel = racesViewModel,
        accountViewModel = accountViewModel,
        modifier = Modifier
            .fillMaxSize(),
        onDismiss = {
        }
    )
}