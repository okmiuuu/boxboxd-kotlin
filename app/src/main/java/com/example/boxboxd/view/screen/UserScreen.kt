package com.example.boxboxd.view.screen

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import co.yml.charts.common.extensions.isNotNull
import coil.compose.rememberAsyncImagePainter
import com.example.boxboxd.R
import com.example.boxboxd.core.inner.User
import com.example.boxboxd.core.jolpica.Driver
import com.example.boxboxd.view.widgets.BoxForOverlayMenu
import com.example.boxboxd.view.widgets.FavoritesTable
import com.example.boxboxd.view.widgets.FollowButton
import com.example.boxboxd.view.widgets.GradeStatsTable
import com.example.boxboxd.view.widgets.LogRaceMenu
import com.example.boxboxd.view.widgets.MenuButton
import com.example.boxboxd.view.widgets.SelectFavCircuitMenu
import com.example.boxboxd.view.widgets.SelectFavDriverMenu
import com.example.boxboxd.view.widgets.SelectFavTeamMenu
import com.example.boxboxd.view.widgets.UserImage
import com.example.boxboxd.view.widgets.UserStatsTable
import com.example.boxboxd.viewmodel.AccountViewModel
import com.example.boxboxd.viewmodel.RacesViewModel

@Composable
fun UserScreen (
    user : User,
    racesViewModel: RacesViewModel,
    accountViewModel: AccountViewModel
) {
    val favDriver = remember { mutableStateOf(user.favDriver) }
    val favTeam = remember { mutableStateOf(user.favTeam) }
    val favCircuit = remember { mutableStateOf(user.favCircuit) }

    val context = LocalContext.current

    val isThatActiveUserPage = accountViewModel.checkIfThatIsYourPage(user)

    val gradeToCountMapForUser = accountViewModel.getGradeStatsForUser()

    val isSelectDriverMenuOpened = remember { mutableStateOf(false) }
    val isSelectTrackMenuOpened = remember { mutableStateOf(false) }
    val isSelectTeamMenuOpened = remember { mutableStateOf(false) }

    val showConfirmDialog = remember { mutableStateOf(false) }
    val showConfirmExitDialog = remember { mutableStateOf(false) }

    Box {
        Column (
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column (
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row (
                    modifier = Modifier
                        .padding(horizontal = 10.dp, vertical = 20.dp)
                        .height(150.dp)
                ){
                    UserImage(user = user,
                        modifier = Modifier
                            .height(150.dp)
                            .width(150.dp)
                    )

                    Spacer(modifier = Modifier.width(20.dp))

                    FavoritesTable(
                        driver = favDriver.value,
                        team = favTeam.value,
                        circuit = favCircuit.value,
                        racesViewModel = racesViewModel,
                        areRowsClickable = isThatActiveUserPage,
                        onDriverSelect = {
                            isSelectDriverMenuOpened.value = true
                        },
                        onTeamSelect = {
                            isSelectTeamMenuOpened.value = true
                        },
                        onTrackSelect = {
                            isSelectTrackMenuOpened.value = true
                        }
                    )
                }

                if (!isThatActiveUserPage) {
                    FollowButton (
                        onClick = {
                            accountViewModel.followUser(user)
                        }
                    )
                }

                GradeStatsTable(gradeToCountMapForUser)

                UserStatsTable(
                    user = user,
                    areRowsClickable = isThatActiveUserPage,
                    accountViewModel = accountViewModel
                )
            }


            if (isThatActiveUserPage) {
                Column {

//                    MenuButton(
//                        buttonText = stringResource(R.string.settings),
//                        buttonImage = R.drawable.settings,
//                        onClick = { accountViewModel.goToSettings(user) }
//                    )

                    MenuButton(
                        buttonText = stringResource(R.string.leave_account),
                        buttonImage = R.drawable.logout,
                        onClick = { showConfirmExitDialog.value = true }
                    )

                }
            }
        }

        if (isSelectDriverMenuOpened.value) {
            BoxForOverlayMenu { showConfirmDialog.value = true }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable(
                        onClick = {  },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )
                    .padding(16.dp)
            ) {
                SelectFavDriverMenu(
                    accountViewModel = accountViewModel,
                    racesViewModel = racesViewModel,
                    onLogSubmitted = { newFavDriver ->
                        favDriver.value = newFavDriver
                        isSelectDriverMenuOpened.value = false
                    }
                )
            }
        }

        if (isSelectTeamMenuOpened.value) {
            BoxForOverlayMenu { showConfirmDialog.value = true }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable(
                        onClick = {  },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )
                    .padding(16.dp)
            ) {
                SelectFavTeamMenu (
                    accountViewModel = accountViewModel,
                    racesViewModel = racesViewModel,
                    onLogSubmitted = { newFavTeam ->
                        favTeam.value = newFavTeam
                        isSelectTeamMenuOpened.value = false
                    }
                )
            }
        }

        if (isSelectTrackMenuOpened.value) {
            BoxForOverlayMenu { showConfirmDialog.value = true }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable(
                        onClick = {  },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )
                    .padding(16.dp)
            ) {
                SelectFavCircuitMenu (
                    accountViewModel = accountViewModel,
                    racesViewModel = racesViewModel,
                    onLogSubmitted = { newFavCircuit ->
                        favCircuit.value = newFavCircuit
                        isSelectTrackMenuOpened.value = false
                    }
                )
            }
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
                            //close this dialog
                            showConfirmDialog.value = false

                            // close any opened menu
                            isSelectDriverMenuOpened.value = false
                            isSelectTeamMenuOpened.value = false
                            isSelectTrackMenuOpened.value = false
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


        if (showConfirmExitDialog.value) {
            AlertDialog(
                onDismissRequest = { showConfirmDialog.value = false },
                text = { Text(stringResource(R.string.leave_dialog)) },
                dismissButton = {
                    TextButton(
                        onClick = { showConfirmExitDialog.value = false }
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
                            //close this dialog
                            showConfirmExitDialog.value = false

                            //leave account
                            accountViewModel.logOut()
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
    }
}



