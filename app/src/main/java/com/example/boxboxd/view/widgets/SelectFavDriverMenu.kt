package com.example.boxboxd.view.widgets

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.boxboxd.R
import com.example.boxboxd.core.inner.objects.MapObjects
import com.example.boxboxd.core.jolpica.Driver
import com.example.boxboxd.model.DropdownItem
import com.example.boxboxd.ui.DriverCard
import com.example.boxboxd.viewmodel.AccountViewModel
import com.example.boxboxd.viewmodel.RacesViewModel
import kotlinx.coroutines.launch

@Composable
fun SelectFavDriverMenu(
    accountViewModel: AccountViewModel,
    racesViewModel: RacesViewModel,
    onLogSubmitted: (Driver?) -> Unit,
) {
    val driverNames = remember { mutableStateOf<List<String>>(emptyList()) }
    val driverName = remember { mutableStateOf("") }
    val driver = remember { mutableStateOf<Driver?>(null) }
    val isLoading = remember { mutableStateOf(false) }
    val isSaving = remember { mutableStateOf(false) }
    val saveError = remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val driverItems = remember { mutableStateListOf<DropdownItem>() }

    LaunchedEffect(Unit) {
        driverNames.value = racesViewModel.getListOfDriverNames()
        Log.i("MENU driverNames", driverNames.value.size.toString())

        // Clear and repopulate driverItems
        driverItems.clear()
        driverNames.value.forEach {
            driverItems.add(DropdownItem(it, null))
        }
        Log.i("MENU driverItems", driverItems.size.toString())
    }

    LaunchedEffect(driverName.value) {
        if (driverName.value.isNotBlank()) {
            isLoading.value = true
            driver.value = racesViewModel.getDriverObjectFromName(driverName.value)
            isLoading.value = false
        } else {
            driver.value = null
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.select_fav_driver),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 5.dp)
        )

        WidgetForAutocompleteField(
            valuesForSearch = driverItems,
            label = stringResource(R.string.driver_name),
            onChangeValue = { chosenDriver ->
                driverName.value = chosenDriver.text
            },
            valuesLimit = 2,
        )

        if (isLoading.value) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        }

        driver.value?.let { currentDriver ->
            DriverCard(
                driver = currentDriver,
            )
        }

        MainButton(
            buttonText = stringResource(R.string.select),
            onClick = {
                driver.value?.let { selectedDriver ->
                    isSaving.value = true
                    saveError.value = null
                    coroutineScope.launch {
                        try {
                            val success = accountViewModel.setFavoriteDriver(selectedDriver)
                            isSaving.value = false
                            if (success) {
                                onLogSubmitted(driver.value)
                            } else {
                                saveError.value = "Failed to save favorite driver"
                            }
                        } catch (e: Exception) {
                            isSaving.value = false
                            saveError.value = "Error: ${e.message}"
                            Log.e("SelectFavDriverMenu", "Error saving driver", e)
                        }
                    }
                }
            },
            enabled = driver.value != null && !isSaving.value
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