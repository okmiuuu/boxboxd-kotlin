package com.example.boxboxd.view.widgets

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.boxboxd.R
import com.example.boxboxd.core.inner.enums.Teams
import com.example.boxboxd.core.inner.objects.MapObjects
import com.example.boxboxd.model.DropdownItem
import com.example.boxboxd.viewmodel.AccountViewModel
import com.example.boxboxd.viewmodel.RacesViewModel
import kotlinx.coroutines.launch

@Composable
fun SelectFavTeamMenu(
    accountViewModel: AccountViewModel,
    racesViewModel: RacesViewModel,
    onLogSubmitted: (Teams) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        val teamName = remember { mutableStateOf("") }
        val team = remember { mutableStateOf<Teams?>(null) }
        val isSaving = remember { mutableStateOf(false) }
        val saveError = remember { mutableStateOf<String?>(null) }
        val coroutineScope = rememberCoroutineScope()

        // Team items with image URLs
        val teamItems = remember { mutableStateOf<List<DropdownItem>>(emptyList()) }

        LaunchedEffect(Unit) {
            teamItems.value = racesViewModel.getListOfTeamItems()
        }
        Text(
            text = stringResource(R.string.select_fav_team),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 5.dp)
        )

        WidgetForAutocompleteField(
            valuesForSearch = teamItems.value,
            label = stringResource(R.string.team_name),
            onChangeValue = { chosenTeam ->
                teamName.value = chosenTeam.text
                team.value = MapObjects.stringNameToTeam[teamName.value.lowercase()]
            },
            valuesLimit = 2,
        )

        MainButton(
            buttonText = stringResource(R.string.select),
            onClick = {
                team.value?.let { selectedTeam ->
                    isSaving.value = true
                    saveError.value = null
                    coroutineScope.launch {
                        try {
                            val success = accountViewModel.setFavoriteTeam(selectedTeam)
                            isSaving.value = false
                            if (success) {
                                onLogSubmitted(selectedTeam)
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
            enabled = team.value != null
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