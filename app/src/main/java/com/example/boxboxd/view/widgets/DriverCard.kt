package com.example.boxboxd.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import com.example.boxboxd.core.jolpica.Driver
import com.example.boxboxd.model.WikiResponse
import com.example.boxboxd.viewmodel.RacesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

@Composable
fun DriverCard(
    driver: Driver,
    modifier: Modifier = Modifier,
) {
    var picture by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(driver.driverId) {
        isLoading = true
        errorMessage = null
        picture = getFirstImageFromWiki(driver.url)
        isLoading = false
        if (picture == null) {
            errorMessage = "No image found for ${driver.givenName} ${driver.familyName}"
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            }
            errorMessage != null -> {
                Text(
                    text = errorMessage ?: "Unknown error",
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp)
                )
            }
            picture != null -> {
                AsyncImage(
                    model = picture,
                    contentDescription = "${driver.givenName} ${driver.familyName}",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .width(150.dp)
                        .height(200.dp) ,
                    onState = { state ->
                        when (state) {
                            is AsyncImagePainter.State.Loading -> println("Loading image for ${driver.driverId}")
                            is AsyncImagePainter.State.Success -> println("Image loaded for ${driver.driverId}")
                            is AsyncImagePainter.State.Error -> {
                                println("Error loading image for ${driver.driverId}: ${state.result.throwable.message}")
                                errorMessage = "Failed to load image"
                            }
                            is AsyncImagePainter.State.Empty -> println("Empty image state for ${driver.driverId}")
                        }
                    }
                )
            }
            else -> {
                Text(
                    text = "No image available",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        Column (
            modifier = Modifier
                .padding(20.dp)
        ) {
            Text(
                text = "${driver.givenName} ${driver.familyName}",
                modifier = Modifier.padding(bottom = 8.dp),
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "code - ${driver.code}",
                modifier = Modifier.padding(bottom = 8.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}


/**
 * Fetches the first image URL from a Wikipedia page using the MediaWiki API.
 * @param wikiUrl The Wikipedia page URL (e.g., http://en.wikipedia.org/wiki/Lewis_Hamilton)
 * @return The image URL or null if no image is found
 */
suspend fun getFirstImageFromWiki(wikiUrl: String?): String? = withContext(Dispatchers.IO) {
    val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }
    val client = OkHttpClient()

    if (wikiUrl.isNullOrBlank()) {
        println("Invalid or empty Wikipedia URL")
        return@withContext null
    }

    try {
        // Extract page title from URL (e.g., "Lewis_Hamilton")
        val pageTitle = wikiUrl.substringAfterLast("/wiki/").replace(" ", "_")
        if (pageTitle.isBlank()) {
            println("Invalid page title extracted from URL: $wikiUrl")
            return@withContext null
        }

        // Build API URL for pageimages
        val apiUrl = "https://en.wikipedia.org/w/api.php?action=query" +
                "&format=json" +
                "&prop=pageimages" +
                "&piprop=thumbnail" +
                "&pithumbsize=500" + // Request a larger thumbnail
                "&titles=$pageTitle"

        val request = Request.Builder().url(apiUrl).build()
        val response = client.newCall(request).execute()

        if (!response.isSuccessful) {
            println("API request failed with code ${response.code}: ${response.message}")
            return@withContext null
        }

        val jsonString = response.body?.string() ?: run {
            println("Empty response body from API")
            return@withContext null
        }

        println("API response for $pageTitle: ${jsonString.take(200)}...")

        val wikiResponse = json.decodeFromString<WikiResponse>(jsonString)
        val page = wikiResponse.query.pages.values.firstOrNull()

        if (page == null || page.thumbnail == null) {
            println("No image found for page $pageTitle")
            return@withContext null
        }

        val imageUrl = page.thumbnail.source
        println("Found image URL for $pageTitle: $imageUrl")
        imageUrl
    } catch (e: IOException) {
        println("Network error fetching image for $wikiUrl: ${e.message}")
        null
    } catch (e: Exception) {
        println("Error parsing image for $wikiUrl: ${e.message}")
        null
    }
}