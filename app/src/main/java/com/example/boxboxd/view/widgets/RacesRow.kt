package com.example.boxboxd.view.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.boxboxd.R
import com.example.boxboxd.core.jolpica.Race
import com.example.boxboxd.model.RaceWithPosition
import com.example.boxboxd.viewmodel.AccountViewModel
import com.example.boxboxd.viewmodel.RacesViewModel

@Composable
fun RacesRow(
    raceItems: List<Race>,
    title: String?,
    racesViewModel: RacesViewModel,
    accountViewModel: AccountViewModel,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    forCardLongPress: (RaceWithPosition) -> Unit = {},
    onClickAdd : () -> Unit = {},
) {
//    val rowId = title ?: "default_row"
//    val scrollState = racesViewModel.getScrollState(rowId)
//    val lazyListState = rememberLazyListState(
//        initialFirstVisibleItemIndex = scrollState.firstVisibleItemIndex,
//        initialFirstVisibleItemScrollOffset = scrollState.firstVisibleItemScrollOffset
//    )

//    LaunchedEffect(lazyListState) {
//        snapshotFlow {
//            Pair(
//                lazyListState.firstVisibleItemIndex,
//                lazyListState.firstVisibleItemScrollOffset
//            )
//        }.collectLatest { (index, offset) ->
//            racesViewModel.saveScrollState(rowId, index, offset)
//        }
//    }

    Column(
        modifier = modifier.padding(vertical = 20.dp),
        horizontalAlignment = Alignment.Start
    ) {
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Loading...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(8.dp)
            )
        } else {

            if (raceItems.isEmpty()) {
                Column (
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.helmet),
                        contentDescription = "",
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.tertiary),
                    )
                    Text(
                        text = stringResource(R.string.server_error),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(items = raceItems) { raceItem ->
                    RaceCard(
                        item = raceItem,
                        racesViewModel = racesViewModel,
                        onClick = {
                            accountViewModel.requestNavigateToRaceScreen(raceItem)
                            onClickAdd()
                        },
                        onLongPress = { raceWithPosition ->
                            forCardLongPress(raceWithPosition)
                        }
                    )
                }
            }
        }
    }
}