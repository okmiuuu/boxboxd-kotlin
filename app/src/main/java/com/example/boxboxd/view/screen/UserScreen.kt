package com.example.boxboxd.view.screen

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
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
import com.example.boxboxd.view.widgets.FavoritesTable
import com.example.boxboxd.view.widgets.FollowButton
import com.example.boxboxd.view.widgets.GradeStatsTable
import com.example.boxboxd.view.widgets.MenuButton
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
    val context = LocalContext.current

    val isThatActiveUserPage = accountViewModel.checkIfThatIsYourPage(user)

    val gradeToCountMapForUser = accountViewModel.getGradeStatsForUser()

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

                FavoritesTable(user = user, racesViewModel = racesViewModel)
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
                MenuButton(
                    buttonText = stringResource(R.string.settings),
                    buttonImage = R.drawable.settings,
                    onClick = { accountViewModel.goToSettings(user) }
                )

                MenuButton(
                    buttonText = stringResource(R.string.leave_account),
                    buttonImage = R.drawable.logout,
                    onClick = { accountViewModel.logOut(context) }
                )
            }

        }
    }

}



