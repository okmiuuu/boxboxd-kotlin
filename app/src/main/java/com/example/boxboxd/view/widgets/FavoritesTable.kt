package com.example.boxboxd.view.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.boxboxd.R
import com.example.boxboxd.core.inner.User
import com.example.boxboxd.core.inner.enums.Teams
import com.example.boxboxd.core.inner.objects.MapObjects
import com.example.boxboxd.core.jolpica.Circuit
import com.example.boxboxd.core.jolpica.Driver
import com.example.boxboxd.viewmodel.RacesViewModel

@Composable
fun FavoritesTable(
    driver : Driver?,
    circuit : Circuit?,
    team : Teams?,
    racesViewModel : RacesViewModel,
    areRowsClickable : Boolean,
    onDriverSelect : () -> Unit,
    onTeamSelect : () -> Unit,
    onTrackSelect : () -> Unit
) {

    val context = LocalContext.current

    var driverCode : String? = null
    var drawableTeamId : Int? = null
    var drawableTrackId : Int? = null

    if (driver != null) {
        driverCode = driver.code
    }

    if (team != null) {
        drawableTeamId = MapObjects.teamToPicture[team] ?: R.drawable.hungaroring
    }

    if (circuit != null) {
        drawableTrackId = racesViewModel.getDrawableResourceId(context, circuit.circuitId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        Row (
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.height(40.dp),
        ) {
            // favorite driver row
            TextFavorites(
                text = stringResource(R.string.fav_driver)
            )

            Box(
                modifier = Modifier
                    .clickable {
                        if (areRowsClickable) {
                            onDriverSelect()
                        }
                    }
            ) { //clickable box to change favorite
                if (driverCode != null) {
                    Text(
                        text = driverCode,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                } else {
                    PlaceholderImage()
                }

            }
        }
        Row (
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.height(40.dp),
        ) {
            //favorite team row
            TextFavorites(
                text = stringResource(R.string.fav_team)
            )
            Box(
                modifier = Modifier
                    .clickable {
                        if (areRowsClickable) {
                            onTeamSelect()
                        }
                    }
            ) { //clickable box to change favorite
                if (drawableTeamId != null) {
                    Image(
                        painter = painterResource(id = drawableTeamId),
                        contentDescription = "Logo for fav constructor",
                        modifier = Modifier
                            .height(40.dp)
                            .padding(5.dp),
                        contentScale = ContentScale.Fit,
                    )
                } else {
                    PlaceholderImage()
                }


            }

        }

        Row (
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.height(40.dp),
        ) {
            //favorite track row
            TextFavorites(
                text = stringResource(R.string.fav_track)
            )

            Box(
                modifier = Modifier
                    .clickable {
                        if (areRowsClickable) {
                            onTrackSelect()
                        }
                    }
            ) { //clickable box to change favorite
                if (drawableTrackId != null) {
                    Image(
                        painter = painterResource(id = drawableTrackId),
                        contentDescription = "Circuit image for fav track",
                        modifier = Modifier
                            .height(40.dp)
                            .padding(5.dp),
                        contentScale = ContentScale.Fit,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.tertiary)
                    )
                } else {
                    PlaceholderImage()
                }

            }
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
            .fillMaxWidth(0.7f)
    )
}

