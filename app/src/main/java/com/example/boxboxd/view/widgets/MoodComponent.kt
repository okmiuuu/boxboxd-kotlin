package com.example.boxboxd.view.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.boxboxd.core.inner.enums.Mood
import com.example.boxboxd.core.inner.objects.MapObjects

@Composable
fun MoodComponent(
    mood: Mood,
) {

    val moodPicture = MapObjects.moodToPicture[mood]

    Box(
        modifier = Modifier
            .height(60.dp)
            .width(60.dp),
        contentAlignment = Alignment.Center
    ) {
        moodPicture?.let {
            Image(
                painter = painterResource(id = it),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(1f)
            )
        }
    }
}