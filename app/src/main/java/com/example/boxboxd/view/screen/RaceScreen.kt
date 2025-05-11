package com.example.boxboxd.view.screen

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.boxboxd.R
import com.example.boxboxd.core.inner.collectAsStateDelegate
import com.example.boxboxd.core.jolpica.Driver
import com.example.boxboxd.core.jolpica.Race
import com.example.boxboxd.core.inner.objects.CircuitContinentMap
import com.example.boxboxd.core.inner.objects.RegionColorMap
import com.example.boxboxd.view.widgets.AddButton
import com.example.boxboxd.view.widgets.BoxForOverlayMenu
import com.example.boxboxd.view.widgets.ChooseListMenu
import com.example.boxboxd.view.widgets.LogRaceMenu
import com.example.boxboxd.view.widgets.MainButton
import com.example.boxboxd.view.widgets.RaceLeaderBoard
import com.example.boxboxd.view.widgets.ReviewColumn
import com.example.boxboxd.viewmodel.AccountViewModel
import com.example.boxboxd.viewmodel.RacesViewModel
import kotlinx.coroutines.launch

@Composable
fun RaceScreen (
    item : Race,
    racesViewModel: RacesViewModel,
    accountViewModel : AccountViewModel
) {
    val context = LocalContext.current
    val drawableResId = racesViewModel.getDrawableResourceId(context, item.Circuit?.circuitId ?: "")

    val continent = CircuitContinentMap.circuitToContinent[item.Circuit?.circuitId] ?: "Unknown"
    val color = RegionColorMap.regionToColor[continent] ?: MaterialTheme.colorScheme.surface

    val isRaceHasPassed = racesViewModel.checkIfTheRaceHasPassed(item)

    val lastWinner = remember { mutableStateOf<String?>(null) }

    val raceWithResult = remember { mutableStateOf<Race?>(null) }
    val driverResultsList = remember { mutableStateOf<Map<Int, Driver>>(emptyMap()) }

    val isLogRaceMenuOpened = remember { mutableStateOf(false) }
    val isListMenuOpened = remember { mutableStateOf(false) }
    val showConfirmDialog = remember { mutableStateOf(false) }
    val showLoginDialog = remember { mutableStateOf(false) }
    val isLogging = remember { mutableStateOf(false) }

    val currentUser = accountViewModel.userObject.collectAsState()

    val isRaceAlreadyLogged = remember { mutableStateOf(false) }

    BackHandler(enabled = showConfirmDialog.value || showLoginDialog.value) {
        showConfirmDialog.value = false
        showLoginDialog.value = false
    }

    BackHandler(enabled = isLogRaceMenuOpened.value || isListMenuOpened.value) {
        if (isListMenuOpened.value && !showConfirmDialog.value) showConfirmDialog.value = true
        if (isLogRaceMenuOpened.value && !showConfirmDialog.value) showConfirmDialog.value = true
    }

    LaunchedEffect(Unit) {
        Log.i("ENTRIES COUNT", accountViewModel.userEntries.value?.size.toString())

        isRaceAlreadyLogged.value = accountViewModel.getIfRaceAlreadyLoggedByUser(race = item)
    }

    LaunchedEffect(Unit) {
        launch {
            val winner = racesViewModel.getLastWinner(item)
            lastWinner.value = winner?.code

            val raceFromRepository = racesViewModel.getRaceWithResults(item)
            raceWithResult.value = raceFromRepository
            driverResultsList.value = racesViewModel.getFullResultTableForRace(raceWithResult.value)
        }
    }

    val localTime = racesViewModel.convertToLocalTime(item.date, item.time)

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .background(color)
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = drawableResId),
                    contentDescription = "Circuit image for ${item.Circuit?.circuitName}",
                    modifier = Modifier
                        .fillMaxWidth(0.3f),
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.tertiary)
                )
                Spacer(modifier = Modifier.width(20.dp))
                Column {
                    Text(
                        text = item.raceName,
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(8.dp),
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = item.date + " " + localTime,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(8.dp),
                    )
                }
            } // top part with circuit image

            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 10.dp),
            ) {

                if (isRaceHasPassed) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        verticalAlignment = Alignment.CenterVertically

                    ) {
                        MainButton(
                            buttonText = stringResource(R.string.log_as_watched),
                            onClick = {
                                if (currentUser.value.id != null) {
                                    isLogRaceMenuOpened.value = true
                                } else {
                                    showLoginDialog.value = true
                                }
                            },
                            enabled = !isRaceAlreadyLogged.value && !isLogging.value
                        )

                        AddButton (
                            modifier = Modifier
                                .height(40.dp)
                                .padding(horizontal = 10.dp)
                        ){
                            if (currentUser.value.id != null) {
                                isListMenuOpened.value = true
                            } else {
                                showLoginDialog.value = true
                            }

                        }
                    }

                }

                Text(
                    text = stringResource(R.string.circuit_name),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = item.Circuit?.circuitName ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (lastWinner.value != null) {
                    Row(
                        modifier = Modifier
                            .padding(vertical = 20.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.last_winner),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = lastWinner.value.toString(),
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }
                }

                if (isRaceHasPassed) {
                    if (raceWithResult.value != null) {
                        RaceLeaderBoard(driverResultsList = driverResultsList.value)
                    }
                }

                ReviewColumn(
                    race = item,
                    racesViewModel = racesViewModel,
                    accountViewModel = accountViewModel
                )
            }
        }

        if (isListMenuOpened.value) {

            BoxForOverlayMenu(
                onClick = { showConfirmDialog.value = true },
                content = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable(
                                onClick = {  },
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            )
                            .padding(16.dp)
                    ) {
                        ChooseListMenu(
                            racesViewModel = racesViewModel,
                            accountViewModel = accountViewModel,
                            isListScreen = false,
                            race = item,
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.6f),
                            onDismiss = {
                                isListMenuOpened.value = false
                            }
                        )
                    }
                }
            )
        }

        if (isLogRaceMenuOpened.value) {

            BoxForOverlayMenu (
                onClick =  { showConfirmDialog.value = true  },
                content = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable(
                                onClick = {  },
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            )
                            .padding(16.dp)
                    ) {
                        LogRaceMenu(
                            accountViewModel = accountViewModel,
                            racesViewModel = racesViewModel,
                            race = item,
                            isLogging = isLogging,
                            onLogSubmitted = {
                                isLogRaceMenuOpened.value = false
                                isRaceAlreadyLogged.value = true
                            }
                        )
                    }
                }
            )
        }

        if (showConfirmDialog.value) {
            AlertDialog(
                onDismissRequest = { showConfirmDialog.value = false },
                text = { Text(stringResource(R.string.confirm_dialog)) },
                dismissButton = {
                    TextButton(
                        onClick = { showConfirmDialog.value = false }
                    ) {
                        Text (
                            text = stringResource(R.string.no),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showConfirmDialog.value = false
                            isLogRaceMenuOpened.value = false
                            isListMenuOpened.value = false
                        }
                    ) {
                        Text (
                            text = stringResource(R.string.yes),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                },
            )
        }

        if (showLoginDialog.value) {
            AlertDialog(
                onDismissRequest = { showConfirmDialog.value = false },
                text = { Text(stringResource(R.string.confirm_dialog)) },
                dismissButton = {
                    TextButton(
                        onClick = { showLoginDialog.value = false }
                    ) {
                        Text (
                            text = stringResource(R.string.no),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showLoginDialog.value = false
                            accountViewModel.requestNavigateToLoginScreen()
                        }
                    ) {
                        Text (
                            text = stringResource(R.string.go_to_login),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                },
            )
        }
    }
}

