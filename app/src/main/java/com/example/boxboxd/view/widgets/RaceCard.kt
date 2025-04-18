package com.example.boxboxd.view.widgets

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.boxboxd.core.jolpica.Race
import com.example.boxboxd.core.inner.objects.CircuitContinentMap
import com.example.boxboxd.core.inner.objects.RegionColorMap
import com.example.boxboxd.viewmodel.RacesViewModel

@Composable
fun RaceCard(
    item: Race,
    modifier: Modifier = Modifier,
    racesViewModel: RacesViewModel,
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current

    val drawableResId = racesViewModel.getDrawableResourceId(context, item.Circuit?.circuitId ?: "")

    val continent = CircuitContinentMap.circuitToContinent[item.Circuit?.circuitId] ?: "Unknown"
    val color = RegionColorMap.regionToColor[continent] ?: MaterialTheme.colorScheme.surface

    Card(
        modifier = modifier
            .width(120.dp)
            .height(180.dp)
            .padding(4.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = color
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = drawableResId),
                contentDescription = "Circuit image for ${item.Circuit?.circuitName}",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.tertiary)
            )
            Text(
                text = item.raceName,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(8.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

