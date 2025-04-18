package com.example.boxboxd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.example.boxboxd.core.inner.RaceRepository
import com.example.boxboxd.ui.theme.BoxboxdTheme
import com.example.boxboxd.view.widgets.AppNavigation
import com.example.boxboxd.viewmodel.AccountViewModel
import com.example.boxboxd.viewmodel.AccountViewModelFactory
import com.example.boxboxd.viewmodel.RacesViewModel
import com.example.boxboxd.viewmodel.RacesViewModelFactory

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {

        val repository = RaceRepository()
        val racesViewModel = ViewModelProvider(this, RacesViewModelFactory(repository))
            .get(RacesViewModel::class.java)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BoxboxdTheme (
                dynamicColor = false
            ) {
                val navController = rememberNavController()

                val accountViewModel = ViewModelProvider(this, AccountViewModelFactory(navController))
                    .get(AccountViewModel::class.java)

                AppNavigation(
                    navController = navController,
                    racesViewModel = racesViewModel,
                    accountViewModel = accountViewModel
                )
            }
        }
    }
}