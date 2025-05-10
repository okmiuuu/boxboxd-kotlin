package com.example.boxboxd.view.widgets

import java.net.URLDecoder
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
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
import com.example.boxboxd.view.screen.RacesScreen
import com.example.boxboxd.view.screen.UserEntriesScreen
import com.example.boxboxd.view.screen.UserScreen
import com.example.boxboxd.viewmodel.AccountViewModel
import com.example.boxboxd.viewmodel.RacesViewModel
import com.google.common.reflect.TypeToken
import com.google.gson.GsonBuilder
import kotlinx.coroutines.flow.collectLatest
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun AppNavigation(
    navController: NavHostController,
    racesViewModel: RacesViewModel,
    accountViewModel: AccountViewModel
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val gson = GsonBuilder().setLenient().create()

    // Observe navigation events
    LaunchedEffect(Unit) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            accountViewModel.navigationEvents.collectLatest { event ->
                when (event) {
                    is AccountViewModel.NavigationEvent.NavigateToUserScreen -> {
                        val userJson = URLEncoder.encode(gson.toJson(event.user), "UTF-8")
                        navController.navigate("${Routes.USER_SCREEN}/$userJson")
                    }
                    is AccountViewModel.NavigationEvent.NavigateToRaceScreen -> {
                        val raceJson = URLEncoder.encode(gson.toJson(event.race), "UTF-8")
                        navController.navigate("${Routes.RACE_SCREEN}/$raceJson")
                    }
                    is AccountViewModel.NavigationEvent.NavigateToEntriesScreen -> {
                        navController.navigate(Routes.ENTRIES_SCREEN)
                    }
                    is AccountViewModel.NavigationEvent.NavigateToListsScreen -> {
                        navController.navigate(Routes.LISTS_SCREEN)
                    }
                    is AccountViewModel.NavigationEvent.NavigateToRacesSearchScreen -> {
                        val racesJson = URLEncoder.encode(gson.toJson(event.races), "UTF-8")
                        navController.navigate("${Routes.RACES_SEARCH_SCREEN}/$racesJson")
                    }
                    is AccountViewModel.NavigationEvent.NavigateBack -> {
                        navController.popBackStack()
                    }
                }
            }
        }
    }

    NavHost(navController = navController, startDestination = Routes.MAIN_SCREEN) {
        composable(Routes.MAIN_SCREEN) {
            val isSearchActivated = remember { mutableStateOf(false) }

            Scaffold(
                topBar = {
                    TopBar(
                        isMainPage = true,
                        accountViewModel = accountViewModel,
                        onSearchClick = {
                            isSearchActivated.value = !isSearchActivated.value
                        }
                    )
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    MainScreen(
                        racesViewModel = racesViewModel,
                        accountViewModel = accountViewModel
                    )

                    if (isSearchActivated.value) {
                        BoxForOverlayMenu {
                            isSearchActivated.value = false
                        }

                        SearchMenu(
                            racesViewModel = racesViewModel
                        ) { racesList ->
                            isSearchActivated.value = false
                            if (racesList.size == 1) {
                                accountViewModel.requestNavigateToRaceScreen(racesList[0])
                            } else {
                                accountViewModel.requestNavigateToRacesSearchScreen(racesList)
                            }
                        }
                    }
                }
            }
        }

        composable(Routes.ENTRIES_SCREEN) {
            Scaffold(
                topBar = {
                    TopBar(accountViewModel = accountViewModel)
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
            arguments = listOf(navArgument("userJson") { type = NavType.StringType })
        ) { backStackEntry ->
            val userJson = backStackEntry.arguments?.getString("userJson")
            val decodedUserJson = userJson?.let { URLDecoder.decode(it, "UTF-8") }
            val user = decodedUserJson?.let { gson.fromJson(it, User::class.java) }
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
                    user?.let {
                        UserScreen(
                            user = it,
                            racesViewModel = racesViewModel,
                            accountViewModel = accountViewModel
                        )
                    }
                }
            }
        }

        composable(
            route = "${Routes.RACE_SCREEN}/{raceJson}",
            arguments = listOf(navArgument("raceJson") { type = NavType.StringType })
        ) { backStackEntry ->
            val raceJson = backStackEntry.arguments?.getString("raceJson")
            val decodedRaceJson = raceJson?.let { URLDecoder.decode(it, "UTF-8") }
            val race = decodedRaceJson?.let { gson.fromJson(it, Race::class.java) }
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
                    race?.let {
                        RaceScreen(
                            item = it,
                            racesViewModel = racesViewModel,
                            accountViewModel = accountViewModel
                        )
                    }
                }
            }
        }

        composable(Routes.LISTS_SCREEN) {
            Scaffold(
                topBar = {
                    TopBar(accountViewModel = accountViewModel)
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

        composable(
            route = "${Routes.RACES_SEARCH_SCREEN}/{racesJson}",
            arguments = listOf(navArgument("racesJson") { type = NavType.StringType })
        ) { backStackEntry ->
            val racesJson = backStackEntry.arguments?.getString("racesJson")
            val decodedRacesJson = racesJson?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.name()) }
            val type = object : TypeToken<List<Race>>() {}.type
            val parsedRaces = decodedRacesJson?.let { gson.fromJson<List<Race>>(it, type) } ?: emptyList()
            Scaffold(
                topBar = {
                    TopBar(accountViewModel = accountViewModel)
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    RacesScreen(
                        racesList = parsedRaces,
                        racesViewModel = racesViewModel,
                        accountViewModel = accountViewModel
                    )
                }
            }
        }
    }
}