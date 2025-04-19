package com.example.boxboxd.view.widgets

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.boxboxd.core.jolpica.Driver
import com.example.boxboxd.model.WikiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

@Composable
fun DriverCard(
    driver : Driver
) {
    val picture = remember { mutableStateOf<String?>("") }

    LaunchedEffect(Unit) {
        picture.value = getFirstImageFromWiki(wikiUrl = driver.url)
    }

    AsyncImage(
        model = picture,
        contentDescription = "${driver.givenName} ${driver.familyName}",
        modifier = Modifier,
        contentScale = ContentScale.Fit
    )
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