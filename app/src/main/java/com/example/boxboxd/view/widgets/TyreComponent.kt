package com.example.boxboxd.view.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.boxboxd.core.inner.enums.TyresGrades
import com.example.boxboxd.core.inner.objects.MapObjects

@Composable
fun TyreComponent(
    typeType: TyresGrades,
) {

    val tyreImage = MapObjects.tyreToPicture[typeType]

    Box(
        modifier = Modifier
            .height(60.dp)
            .width(60.dp),
        contentAlignment = Alignment.Center,
    ) {
        tyreImage?.let {
            Image(
                painter = painterResource(id = it),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(1f)
            )
        }
    }
}