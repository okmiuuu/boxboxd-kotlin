package com.example.boxboxd.view.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.boxboxd.R
import com.example.boxboxd.core.inner.Entry
import com.example.boxboxd.core.inner.User
import com.example.boxboxd.core.inner.enums.Mood
import com.example.boxboxd.core.inner.enums.TyresGrades
import com.example.boxboxd.core.inner.enums.Visibility
import com.example.boxboxd.core.inner.objects.MapObjects
import com.example.boxboxd.core.jolpica.Race
import com.example.boxboxd.viewmodel.AccountViewModel
import com.google.firebase.Timestamp

@Composable
fun LogRaceMenu(
    accountViewModel: AccountViewModel,
    race: Race,
    onLogSubmitted: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxHeight(0.5f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        val comment = remember { mutableStateOf("") }

        val selectedTyre = remember { mutableStateOf<TyresGrades?>(null) }

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            text = stringResource(R.string.how_was_the_race),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        GradeSelector(selectedTyre = selectedTyre)

        TextField(
            value = comment.value,
            onValueChange = {
                comment.value = it
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
                .height(100.dp),
            label = {
                Text (
                    text = stringResource(R.string.any_thoughts),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            textStyle = MaterialTheme.typography.bodyMedium,
            singleLine = false,
            maxLines = 4
        )

        MainButton(
            buttonText = stringResource(R.string.add_log),
            onClick = {
                val rating = MapObjects.tyreToGrade[selectedTyre.value]

                val entry = Entry(
                    race = race,
                    userId = accountViewModel.userId,
                    mood = null,
                    rating = rating,
                    text = comment.value,
                    createdAt = Timestamp.now(),
                    updatedAt = Timestamp.now(),
                    visibility = Visibility.PUBLIC,
                    user = accountViewModel.userObject.value
                )

                accountViewModel.logRace(entry)
                onLogSubmitted()
            },
        )

    }
}

@Composable
fun GradeSelector(selectedTyre: MutableState<TyresGrades?>) {

    val tyreToReaction = mapOf(
        TyresGrades.WET to stringResource(R.string.kinda_awful),
        TyresGrades.INTER to stringResource(R.string.ngl_bad),
        TyresGrades.HARD to stringResource(R.string.okay_ig),
        TyresGrades.MEDIUM to stringResource(R.string.pretty_good),
        TyresGrades.SOFT to stringResource(R.string.awesome_thing),
    )

    val reaction = remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TyresGrades.entries.forEach { tyreGrade ->
                TyreComponentSelector(
                    typeType = tyreGrade,
                    isSelected = selectedTyre.value == tyreGrade,
                    onClick = {
                        reaction.value = tyreToReaction[tyreGrade]
                        selectedTyre.value = tyreGrade
                    }
                )
            }
        }
        if (reaction.value != null) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                text = reaction.value.toString(),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun TyreComponentSelector(
    typeType: TyresGrades,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val tyreSize = if (isSelected) 1f else 0.8f

    val tyreImage = MapObjects.tyreToPicture[typeType]

    Box(
        modifier = Modifier
            .height(60.dp)
            .width(60.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        tyreImage?.let {
            Image(
                painter = painterResource(id = it),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(tyreSize)
            )
        }
    }
}