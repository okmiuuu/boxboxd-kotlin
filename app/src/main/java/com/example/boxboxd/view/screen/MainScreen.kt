package com.example.boxboxd.view.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.boxboxd.R
import com.example.boxboxd.core.inner.collectAsStateDelegate
import com.example.boxboxd.core.jolpica.Driver
import com.example.boxboxd.ui.DriverCard
import com.example.boxboxd.view.widgets.RacesRow
import com.example.boxboxd.viewmodel.AccountViewModel
import com.example.boxboxd.viewmodel.AuthViewModel
import com.example.boxboxd.viewmodel.RacesViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

@Composable
fun MainScreen(
    racesViewModel: RacesViewModel,
    accountViewModel: AccountViewModel,
    authViewModel: AuthViewModel
) {
    val racesThisSeason by racesViewModel.racesThisSeason.collectAsStateDelegate()
    val isLoadingThisSeason by racesViewModel.isLoadingThisSeason.collectAsStateDelegate()

    val racesLastSeason by racesViewModel.racesLastSeason.collectAsStateDelegate()
    val isLoadingLastSeason by racesViewModel.isLoadingLastSeason.collectAsStateDelegate()

    val userLists by accountViewModel.userLists.collectAsStateDelegate()

    // Reactive FirebaseAuth state
    val currentUserState = rememberFirebaseAuthState()
    val userObject = accountViewModel.userObject.collectAsState().value

    LaunchedEffect(Unit) {
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

        if (currentUserState.value != null) {
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
}

@Composable
fun rememberFirebaseAuthState(): State<FirebaseUser?> {
    val auth = FirebaseAuth.getInstance()
    val userState = remember { MutableStateFlow(auth.currentUser) }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            Log.d("MainScreen", "Auth state changed: ${firebaseAuth.currentUser?.email}")
            userState.value = firebaseAuth.currentUser
        }
        auth.addAuthStateListener(listener)
        onDispose {
            Log.d("MainScreen", "Removing AuthStateListener")
            auth.removeAuthStateListener(listener)
        }
    }

    return userState.collectAsState()
}