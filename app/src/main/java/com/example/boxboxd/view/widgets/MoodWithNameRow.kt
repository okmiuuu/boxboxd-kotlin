package com.example.boxboxd.view.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.boxboxd.R
import com.example.boxboxd.core.inner.enums.Mood
import com.example.boxboxd.core.inner.enums.TyresGrades
import com.example.boxboxd.core.inner.objects.MapObjects

@Composable
fun MoodWithNameRow(mood : Mood) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        MoodComponent (
            mood = mood
        )
        Text (
            text = stringResource(MapObjects.moodToString[mood] ?: R.string.no),
            style = MaterialTheme.typography.bodyMedium
        )
    }

}