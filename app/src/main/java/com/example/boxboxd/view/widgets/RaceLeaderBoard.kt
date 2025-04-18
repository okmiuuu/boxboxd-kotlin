package com.example.boxboxd.view.widgets

import android.util.Log
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.boxboxd.core.jolpica.Driver

@Composable
fun RaceLeaderBoard(
    driverResultsList : Map<Int, Driver>
) {

    var isResultsExpanded = remember { mutableStateOf(false) }

    val podiumCount = 3 // const to get only 3 first places for the race

    val podiumList = mutableMapOf<Int, Driver?>()

    for (i in 1 until podiumCount + 1 ) {
        podiumList[i] = driverResultsList[i]
    }

    val missingDrivers = driverResultsList.filterKeys { it !in podiumList.keys }
    val firstHalf = (missingDrivers.size + 1) / 2 // get how many drivers are going to be in the first column

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isResultsExpanded.value = !isResultsExpanded.value },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "results:",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = if (isResultsExpanded.value) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand/Collapse",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isResultsExpanded.value) {
                Spacer(modifier = Modifier.height(8.dp))
                if (driverResultsList.isEmpty()) {
                    Text(
                        text = "No results available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    val sortedResults = missingDrivers.entries.sortedBy { it.key }
                    val chunkedResults = sortedResults.chunked(firstHalf)
                    OnlyPodium(podiumList)
                    Spacer(modifier = Modifier.height(10.dp))

                    Divider(
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.fillMaxWidth(),
                        thickness = 5.dp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        chunkedResults.forEach { column ->
                            Column {
                                column.forEach { (position, driver) ->
                                    Text(
                                        text = "$position ${driver.code}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                OnlyPodium(podiumList)
            }
        }
    }
}

@Composable
fun OnlyPodium(podiumList :  MutableMap<Int, Driver?>) {
    Row {
        Text(
            text = "1 ${podiumList[1]?.code}", // the first position
            modifier = Modifier
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row {
        Text(
            text = "2 ${podiumList[2]?.code}", // the second position
            modifier = Modifier
                .fillMaxWidth(0.7f),
            style = MaterialTheme.typography.titleMedium,
        )

        Text(
            text = "3 ${podiumList[3]?.code}", // the third position
            modifier = Modifier
                .fillMaxWidth(),
            style = MaterialTheme.typography.titleMedium,
        )
    }
}
