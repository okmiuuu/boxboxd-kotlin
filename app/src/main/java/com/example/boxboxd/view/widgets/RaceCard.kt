package com.example.boxboxd.view.widgets

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.example.boxboxd.core.jolpica.Race
import com.example.boxboxd.core.inner.objects.CircuitContinentMap
import com.example.boxboxd.core.inner.objects.RegionColorMap
import com.example.boxboxd.model.RaceWithPosition
import com.example.boxboxd.ui.theme.White
import com.example.boxboxd.viewmodel.RacesViewModel

@Composable
fun RaceCard(
    item: Race,
    modifier: Modifier = Modifier,
    racesViewModel: RacesViewModel,
    onClick: () -> Unit = {},
    onLongPress: (RaceWithPosition) -> Unit = {},
) {
    val context = LocalContext.current

    val drawableResId = racesViewModel.getDrawableResourceId(context, item.Circuit?.circuitId ?: "")

    val continent = CircuitContinentMap.circuitToContinent[item.Circuit?.circuitId] ?: "Unknown"
    val color = RegionColorMap.regionToColor[continent] ?: MaterialTheme.colorScheme.surface

    var cardCenterX by remember { mutableFloatStateOf(0f) }

    Card(
        modifier = modifier
            .width(120.dp)
            .height(180.dp)
            .padding(4.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        onClick()
                    },
                    onLongPress = {
                        onLongPress(RaceWithPosition(item, cardCenterX))
                    }
                )
            }
            .onGloballyPositioned { coordinates ->
                val position = coordinates.positionInRoot()

                cardCenterX = position.x //+ (coordinates.size.width / 2f)
//                cardSize = Pair(
//                    coordinates.size.width.toFloat(),
//                    coordinates.size.height.toFloat()
//                )
            },
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
                colorFilter = ColorFilter.tint(White)
            )
            Text(
                text = item.season.toString(),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(8.dp),
                textAlign = TextAlign.Center,
                color = White
            )
            Text(
                text = item.raceName,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(8.dp).height(40.dp),
                textAlign = TextAlign.Center,
                color = White
            )
        }
    }
}

