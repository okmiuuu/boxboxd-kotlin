package com.example.boxboxd.view.widgets

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.boxboxd.R
import com.example.boxboxd.core.inner.enums.TyresGrades
import com.example.boxboxd.core.inner.objects.MapObjects

@Composable
fun GradeStatsTable(gradeToCountMap : Map<Int, Int>) {

    Card(
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .padding(vertical = 5.dp),
        shape = RoundedCornerShape(5.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            var fullEntryCount = 0

            for (i in 1..5) {
                fullEntryCount += gradeToCountMap[i] ?: 0
            }

            for (i in 1..5) { // grades from 1 to 5
                StatRectangle(
                    grade = i,
                    countForGrade = gradeToCountMap[i] ?: 0,
                    fullEntryCount = fullEntryCount
                )
            }
        }
    }
}

@Composable
fun StatRectangle (grade : Int, countForGrade : Int, fullEntryCount : Int) {


    val height : Float = 100 - (countForGrade.toFloat() / fullEntryCount.toFloat() ) * 100

    Log.i("HEIGHT FOR ${grade}", height.toString())

    val tyreType = MapObjects.gradeToTyre[grade] ?: TyresGrades.WET

    Column(
        modifier = Modifier
            .padding(3.dp),
    ) {
        Column(
            modifier = Modifier
                .height(100.dp)
                .width(60.dp)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .height(height.dp)
                    .width(60.dp)
                    .background(MaterialTheme.colorScheme.surface)
            ) {

            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        TyreComponent(
            typeType = tyreType
        )
    }
}