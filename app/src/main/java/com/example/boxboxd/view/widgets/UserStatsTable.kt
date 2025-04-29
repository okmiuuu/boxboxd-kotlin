package com.example.boxboxd.view.widgets

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.boxboxd.core.inner.User
import com.example.boxboxd.core.inner.enums.StatTypes
import com.example.boxboxd.core.inner.enums.Teams
import com.example.boxboxd.core.inner.objects.MapObjects
import com.example.boxboxd.core.jolpica.Circuit
import com.example.boxboxd.core.jolpica.Driver
import com.example.boxboxd.viewmodel.AccountViewModel

@Composable
fun UserStatsTable(
    user : User,
    areRowsClickable: Boolean,
    accountViewModel: AccountViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .padding(vertical = 5.dp),
        shape = RoundedCornerShape(5.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column (
            modifier = Modifier
                .padding(horizontal = 5.dp),
        ){
            UserStatRow(
                isClickable = areRowsClickable,
                type = StatTypes.ENTRIES,
                count = accountViewModel.getUserStat(StatTypes.ENTRIES, user),
                onClick = {
                    Log.i("userRow1", "click")
                    accountViewModel.navigateToEntriesScreen()
                },
            )

            UserStatRow(
                isClickable = areRowsClickable,
                type = StatTypes.LISTS,
                count = accountViewModel.getUserStat(StatTypes.LISTS, user),
                onClick = {
                    Log.i("userRow1", "click")
                    accountViewModel.navigateToListsScreen()
                },
            )
        }
    }

}

@Composable
fun UserStatRow(
    isClickable : Boolean = true,
    type : StatTypes,
    count : Int,
    onClick: () -> Unit = {},
) {
    val textStat = stringResource(MapObjects.typeStatToString[type] ?: 0)

    Row (
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clickable(
                onClick = {
                    if (isClickable) {
                        onClick()
                    }
                }
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = textStat,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth(0.3f)
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontSize = 16.sp,
        )
        Box (
            modifier = Modifier
                .height(30.dp)
                .width(30.dp)
        ) {
            if (isClickable) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = textStat,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}