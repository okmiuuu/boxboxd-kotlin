package com.example.boxboxd.view.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.boxboxd.R
import com.example.boxboxd.core.inner.enums.TyresGrades
import com.example.boxboxd.core.inner.objects.MapObjects

@Composable
fun TyreWithReactionRow(tyreType : TyresGrades) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TyreComponent(
            typeType = tyreType
        )
        Text(
            text = stringResource(MapObjects.tyreToReaction[tyreType] ?: R.string.no),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth(0.8f)
        )
    }

}