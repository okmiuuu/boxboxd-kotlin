package com.example.boxboxd.view.widgets

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.boxboxd.R
import com.example.boxboxd.core.jolpica.Race
import com.example.boxboxd.model.DropdownItem
import com.example.boxboxd.viewmodel.AccountViewModel
import com.example.boxboxd.viewmodel.RacesViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
fun SearchMenu(
    racesViewModel: RacesViewModel,
    onSearchSend : (List<Race>) -> Unit
) {

    val seasonValue = remember { mutableStateOf("") }

    val racesForSeason = remember { mutableStateOf<List<Race?>>(emptyList()) }
    val raceNames = remember { mutableStateOf<List<String>>(emptyList()) }
    val raceName = remember { mutableStateOf("") }
    val race = remember { mutableStateOf<Race?>(null) }

    val raceItems = remember { mutableStateListOf<DropdownItem>() }

//    val dropDownOptions = remember { mutableStateOf(listOf<String>()) }
//    val textFieldValue = remember { mutableStateOf(TextFieldValue()) }
//    val dropDownExpanded = remember { mutableStateOf(false) }

    val errorMessage = remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val fetchJob = remember { mutableStateOf<Job?>(null) }

//    fun onDropdownDismissRequest() {
//        dropDownExpanded.value = false
//    }
//
//    fun onValueChanged(value: TextFieldValue) {
//        dropDownExpanded.value = true
//        textFieldValue.value = value
//        dropDownOptions.value = raceNames.value
//            .filter { it.contains(value.text, ignoreCase = true) && it != value.text }
//            .take(3)
//        raceName.value = textFieldValue.value.text
//        race.value = racesForSeason.value.find {
//            it?.raceName == raceName.value
//        }
//    }

    Column (
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.search),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 5.dp)
        )

        TextField(
            value = seasonValue.value,
            onValueChange = { newValue ->
                if (newValue.all { it.isDigit() } && newValue.length <= 4) {
                    seasonValue.value = newValue
                    errorMessage.value = null
                } else {
                    errorMessage.value = "Enter a valid 4-digit year"
                }

                if (newValue.isEmpty()) {
                    racesForSeason.value = emptyList()
                    fetchJob.value?.cancel()
                }
            },
            label = { Text(stringResource(id = R.string.season), style = MaterialTheme.typography.bodyMedium) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )

        LaunchedEffect(seasonValue.value) {
            if (seasonValue.value.length == 4 && seasonValue.value.all { it.isDigit() }) {
                val seasonInt = seasonValue.value.toIntOrNull()
                if (seasonInt != null && seasonInt > 0) {
                    fetchJob.value?.cancel()
                    errorMessage.value = null

                    fetchJob.value = coroutineScope.launch {
                        try {
                            val races = racesViewModel.getRacesForSeason(seasonInt)
                            racesForSeason.value = races
                            raceNames.value = races.map { it?.raceName ?: "" }.filter { it.isNotBlank() }
                            Log.d("RaceSearchScreen", "Fetched ${races.size} races for season $seasonInt")
                            raceItems.clear()
                            raceNames.value.forEach {
                                raceItems.add(DropdownItem(it, null))
                            }
                        } catch (e: Exception) {
                            errorMessage.value = "Error fetching races: ${e.message}"
                            Log.e("RaceSearchScreen", "Error fetching races for $seasonInt", e)
                            racesForSeason.value = emptyList()
                            raceNames.value = emptyList()
                            raceItems.clear()
                        }
                    }
                }
            }
        }

        WidgetForAutocompleteField(
            valuesForSearch = raceItems,
            label = stringResource(R.string.race_name),
            onChangeValue = { chosenRace ->
                raceName.value = chosenRace.text
                race.value = racesForSeason.value.find {
                    it?.raceName == raceName.value
                }
            },
            valuesLimit = 5,
        )

//        TextFieldWithDropdown(
//            modifier = Modifier.fillMaxWidth(),
//            value = textFieldValue.value,
//            setValue = ::onValueChanged,
//            onDismissRequest = ::onDropdownDismissRequest,
//            dropDownExpanded = dropDownExpanded.value,
//            list = dropDownOptions.value,
//            label = stringResource(R.string.race_name)
//        )

        MainButton(
            buttonText = stringResource(R.string.search),
            onClick = {

                var resultList = mutableListOf<Race?>()

                val racesForSeasonValue = racesForSeason.value



                if (race.value == null) {
                    Log.i("SEARCH MENU size" , racesForSeasonValue.size.toString() )
                    resultList = racesForSeasonValue.toMutableList()
                } else {
                    resultList.add(race.value)
                }

                val resultListNoNulls = resultList.filterNotNull()

                onSearchSend(resultListNoNulls.toList())
            }
        )
    }

}