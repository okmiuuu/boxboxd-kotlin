package com.example.boxboxd.view.widgets

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.boxboxd.R
import com.example.boxboxd.core.inner.enums.Teams
import com.example.boxboxd.core.inner.objects.MapObjects
import com.example.boxboxd.core.jolpica.Circuit
import com.example.boxboxd.model.DropdownItem
import com.example.boxboxd.viewmodel.AccountViewModel
import com.example.boxboxd.viewmodel.RacesViewModel
import kotlinx.coroutines.launch

@Composable
fun SelectFavCircuitMenu(
    accountViewModel: AccountViewModel,
    racesViewModel: RacesViewModel,
    onLogSubmitted: (Circuit) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        val trackName = remember { mutableStateOf("") }
        val track = remember { mutableStateOf<Circuit?>(null) }
        val isSaving = remember { mutableStateOf(false) }
        val saveError = remember { mutableStateOf<String?>(null) }
        val coroutineScope = rememberCoroutineScope()

        val trackItems = remember { mutableStateOf<List<DropdownItem>>(emptyList()) }
        val allCircuits  = remember { mutableStateOf<List<Circuit?>>(emptyList()) }

        val context = LocalContext.current

        LaunchedEffect(Unit) {
            trackItems.value = racesViewModel.getCircuitsWithPicturesList(context)
            Log.i("track items count", trackItems.value.size.toString())
            allCircuits.value = racesViewModel.getCircuitsFromRepository()
            Log.i("all Circuits count", allCircuits.value.size.toString())
        }

        val dropDownOptions = remember { mutableStateOf(listOf<DropdownItem>()) }
        val textFieldValue = remember { mutableStateOf(TextFieldValue()) }
        val dropDownExpanded = remember { mutableStateOf(false) }

        fun onDropdownDismissRequest() {
            dropDownExpanded.value = false
        }

        fun onValueChanged(value: TextFieldValue) {
            dropDownExpanded.value = true
            textFieldValue.value = value
            dropDownOptions.value = trackItems.value
                .filter { it.text.contains(value.text, ignoreCase = true) && it.text != value.text }
                .take(3)

            trackName.value = textFieldValue.value.text
            track.value = allCircuits.value.find {
                it?.circuitName == trackName.value
            }
        }

        Text(
            text = stringResource(R.string.select_fav_track),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 5.dp)
        )

        TextFieldWithDropdownAndPicture(
            modifier = Modifier.fillMaxWidth(),
            value = textFieldValue.value,
            setValue = ::onValueChanged,
            onDismissRequest = ::onDropdownDismissRequest,
            dropDownExpanded = dropDownExpanded.value,
            list = dropDownOptions.value,
            label = stringResource(R.string.circuit_name)
        )

        MainButton(
            buttonText = stringResource(R.string.select),
            onClick = {
                track.value?.let { selectedTrack ->
                    isSaving.value = true
                    saveError.value = null
                    coroutineScope.launch {
                        try {
                            val success = accountViewModel.setFavoriteCircuit(selectedTrack)
                            isSaving.value = false
                            if (success) {
                                onLogSubmitted(selectedTrack)
                            } else {
                                saveError.value = "Failed to save favorite team"
                            }
                        } catch (e: Exception) {
                            isSaving.value = false
                            saveError.value = "Error: ${e.message}"
                            Log.e("SelectFavTeamMenu", "Error saving team", e)
                        }
                    }
                }
            },
            enabled = track.value != null && !isSaving.value
        )

        if (isSaving.value) {
            CircularProgressIndicator(modifier = Modifier.padding(8.dp))
        }

        saveError.value?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(8.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}