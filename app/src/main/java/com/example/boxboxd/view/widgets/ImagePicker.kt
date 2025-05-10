package com.example.boxboxd.view.widgets

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.boxboxd.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ImagePicker(
    firstImageToShow : Uri? = null,
    onImageSelected: (Uri?) -> Unit
) {
    var imageUri by remember { mutableStateOf(firstImageToShow) }

    Log.i("IMAGE PICKER", imageUri.toString())

    val permission =
        android.Manifest.permission.READ_MEDIA_IMAGES
    val permissionState = rememberPermissionState(permission)

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
        onImageSelected(uri)
    }

    Column(
        modifier = Modifier
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (imageUri != null) {
            Image(
                painter = rememberAsyncImagePainter(imageUri),
                contentDescription = "Selected Image",
                modifier = Modifier
                    .size(200.dp)
                    .padding(8.dp)
            )
        } else {
            Text (
                text = stringResource(R.string.no_image_selected),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }

        Button(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .padding(vertical = 10.dp),
            shape = RoundedCornerShape(10.dp),
            onClick = {
                if (permissionState.status.isGranted) {
                    galleryLauncher.launch("image/*")
                } else {
                    permissionState.launchPermissionRequest()
                }
            }
        ) {
            Text (
                text = stringResource(R.string.choose_picture),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }

        if (!permissionState.status.isGranted && permissionState.status.shouldShowRationale) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Permission denied. Please grant access to the gallery in settings.")
        }
    }
}