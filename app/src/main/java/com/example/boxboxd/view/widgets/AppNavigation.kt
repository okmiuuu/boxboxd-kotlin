package com.example.boxboxd.view.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.boxboxd.core.inner.User
import com.example.boxboxd.core.inner.objects.Routes
import com.example.boxboxd.core.jolpica.Race
import com.example.boxboxd.view.screen.ListScreen
import com.example.boxboxd.view.screen.MainScreen
import com.example.boxboxd.view.screen.RaceScreen
import com.example.boxboxd.view.screen.UserEntriesScreen
import com.example.boxboxd.view.screen.UserScreen
import com.example.boxboxd.viewmodel.AccountViewModel
import com.example.boxboxd.viewmodel.RacesViewModel
import com.google.gson.Gson
import java.net.URLDecoder

@Composable
fun AppNavigation(
    navController: NavHostController,
    racesViewModel: RacesViewModel,
    accountViewModel: AccountViewModel
) {
    NavHost(navController = navController, startDestination = Routes.MAIN_SCREEN) {
        composable(Routes.MAIN_SCREEN) {
            Scaffold(
                topBar = {
                    TopBar(
                        isMainPage = true,
                        accountViewModel = accountViewModel
                    )
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    MainScreen(
                        navController = navController,
                        racesViewModel = racesViewModel,
                        accountViewModel = accountViewModel
                    )
                }
            }
        }

        composable(Routes.ENTRIES_SCREEN) {
            Scaffold(
                topBar = {
                    TopBar(
                        accountViewModel = accountViewModel
                    )
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    UserEntriesScreen(
                        racesViewModel = racesViewModel,
                        accountViewModel = accountViewModel
                    )
                }
            }
        }

        composable(
            route = "${Routes.USER_SCREEN}/{userJson}",
            arguments = listOf(
                navArgument("userJson") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val raceJson = backStackEntry.arguments?.getString("userJson")
            val decodedRaceJson = URLDecoder.decode(raceJson, "UTF-8")
            val user = Gson().fromJson(decodedRaceJson, User::class.java)
            Scaffold(
                topBar = {
                    TopBar(
                        isMainPage = false,
                        isUserPage = true,
                        accountViewModel = accountViewModel
                    )
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    UserScreen(
                        user = user,
                        racesViewModel = racesViewModel,
                        accountViewModel = accountViewModel
                    )
                }
            }
        }

        composable(
            route = "${Routes.RACE_SCREEN}/{raceJson}",
            arguments = listOf(
                navArgument("raceJson") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val raceJson = backStackEntry.arguments?.getString("raceJson")
            val decodedRaceJson = URLDecoder.decode(raceJson, "UTF-8")
            val race = Gson().fromJson(decodedRaceJson, Race::class.java)
            Scaffold(
                topBar = {
                    TopBar(
                        isMainPage = false,
                        accountViewModel = accountViewModel
                    )
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    RaceScreen(
                        item = race,
                        racesViewModel = racesViewModel,
                        accountViewModel = accountViewModel
                    )
                }
            }
        }

        composable(Routes.LISTS_SCREEN) {
            Scaffold(
                topBar = {
                    TopBar(
                        accountViewModel = accountViewModel
                    )
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    ListScreen(
                        racesViewModel = racesViewModel,
                        accountViewModel = accountViewModel
                    )
                }
            }
        }
    }
}