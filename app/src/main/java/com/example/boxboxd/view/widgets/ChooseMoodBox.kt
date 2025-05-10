package com.example.boxboxd.view.widgets

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import com.example.boxboxd.R
import com.example.boxboxd.core.inner.enums.Mood
import com.example.boxboxd.core.inner.objects.MapObjects
import com.example.boxboxd.model.DropdownItem
import com.example.boxboxd.viewmodel.RacesViewModel

@Composable
fun ChooseMoodBox(
    racesViewModel: RacesViewModel,
    onMoodChange : (Mood?) -> Unit
) {
    val moodName = remember { mutableStateOf<String?>(null) }
    val moodResourceId = remember { mutableStateOf<Int?>(null) }
    val mood = remember { mutableStateOf<Mood?>(null) }

    val moodItems = remember { mutableStateOf<List<DropdownItem>>(emptyList()) }

    val resultList: MutableList<DropdownItem> = mutableListOf()

    MapObjects.moodToString.forEach {
        val moodString = stringResource(it.value)
        val picture = MapObjects.moodToPicture[it.key]
        resultList.add(DropdownItem(moodString,picture))
    }

    moodItems.value = resultList

    val dropDownOptions = remember { mutableStateOf(listOf<DropdownItem>()) }
    val textFieldValue = remember { mutableStateOf(TextFieldValue()) }
    val dropDownExpanded = remember { mutableStateOf(false) }

    fun onDropdownDismissRequest() {
        dropDownExpanded.value = false
    }

    val context = LocalContext.current

    fun onValueChanged(value: TextFieldValue) {
        dropDownExpanded.value = true
        textFieldValue.value = value
        dropDownOptions.value = moodItems.value
            .filter { it.text.contains(value.text, ignoreCase = true) && it.text != value.text }

        moodName.value = textFieldValue.value.text
        moodResourceId.value = racesViewModel.getResourceId(
                context = context,
                name = moodName.value ?: "",
                resourceType = "string"
        )
        mood.value = MapObjects.stringToMood[moodResourceId.value]
        onMoodChange(mood.value)
    }

    TextFieldWithDropdownAndPicture(
        modifier = Modifier.fillMaxWidth(),
        value = textFieldValue.value,
        setValue = ::onValueChanged,
        onDismissRequest = ::onDropdownDismissRequest,
        dropDownExpanded = dropDownExpanded.value,
        list = dropDownOptions.value,
        label = stringResource(R.string.mood)
    )

}