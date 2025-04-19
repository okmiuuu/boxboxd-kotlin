package com.example.boxboxd.view.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.boxboxd.R
import com.example.boxboxd.core.inner.User
import com.example.boxboxd.core.inner.enums.FavTypes
import com.example.boxboxd.core.jolpica.Circuit
import com.example.boxboxd.core.jolpica.Constructor
import com.example.boxboxd.core.jolpica.Driver
import com.example.boxboxd.viewmodel.RacesViewModel

@Composable
fun FavoritesTable(
    user: User,
    racesViewModel : RacesViewModel
) {
    val driver = user.favDriver
    val team = user.favTeam
    val circuit = user.favCircuit

    var driverCode = "-"
    var drawableResId = R.drawable.heart

    if (driver != null) {
        driverCode = driver.code
    }

    val context = LocalContext.current

    if (circuit != null) {
        drawableResId = racesViewModel.getDrawableResourceId(context, circuit.circuitId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        Row {
            // favorite driver row
            TextFavorites(
                text = stringResource(R.string.fav_driver)
            )

            Box(

            ) { //clickable box to change favorite
                Text(
                    text = driverCode,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.height(30.dp),
                )
            }
        }
        Row {
            //favorite team row
            TextFavorites(
                text = stringResource(R.string.fav_team)
            )
            Image(
                painter = painterResource(id = drawableResId),
                contentDescription = "Circuit image for fav track",
                modifier = Modifier
                    .height(40.dp)
                    .padding(5.dp),
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.tertiary)
            )
        }

        Row {
            //favorite track row
            TextFavorites(
                text = stringResource(R.string.fav_track)
            )

            Image(
                painter = painterResource(id = drawableResId),
                contentDescription = "Circuit image for fav track",
                modifier = Modifier
                    .height(40.dp)
                    .padding(5.dp),
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.tertiary)
            )
        }

    }
}

@Composable
fun TextFavorites(
    text : String
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier
            .height(40.dp)
            .fillMaxWidth(0.7f)
    )
}

