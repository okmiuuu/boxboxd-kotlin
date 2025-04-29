package com.example.boxboxd.core.inner

import android.util.Log
import com.example.boxboxd.core.api.RaceApi
import com.example.boxboxd.core.apiclient.RaceRetrofitClient
import com.example.boxboxd.core.jolpica.Circuit
import com.example.boxboxd.core.jolpica.Driver
import com.example.boxboxd.core.jolpica.Race
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class RaceRepository @Inject constructor(
) {
    private val raceApi = RaceRetrofitClient.instance

    fun fetchRaces(onResult: (List<Race>?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = raceApi.getRaces()
                withContext(Dispatchers.Main) {
                    onResult(response.MRData.RaceTable?.Races)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onResult(null)
                }
            }
        }
    }

    suspend fun fetchRacesSuspend(): List<Race>? {
        return suspendCancellableCoroutine { continuation ->
            fetchRaces { races ->
                if (races != null) {
                    continuation.resume(races)
                } else {
                    continuation.resumeWithException(Exception("Failed to fetch races."))
                }
            }
        }
    }

    suspend fun getRacesForSeason(seasonYear: Int): List<Race> {
        return try {
            val response = raceApi.fetchRacesForSeason(seasonYear)
            val races = response.MRData.RaceTable?.Races ?: emptyList()
            races
        } catch (e: Exception) {
            println("Error fetching races: ${e.message}")
            emptyList()
        }
    }

    suspend fun getDriversForSeason(seasonYear: Int): List<Driver> {
        return try {
            val response = raceApi.fetchDriversForSeason(seasonYear)
            val drivers = response.MRData.DriverTable?.Drivers ?: emptyList()
            drivers
        } catch (e: Exception) {
            println("Error fetching drivers: ${e.message}")
            emptyList()
        }
    }

    suspend fun getAllCircuits(): List<Circuit> = withContext(Dispatchers.IO) {
        val allCircuits = mutableListOf<Circuit>()
        var offset = 0
        val limit = 30 // Default API limit (from JSON: "limit": "30")

        try {
            do {
                val response = raceApi.fetchAllCircuits(offset)
                Log.d("RacesViewModel", "API Response (offset=$offset): $response")

                val circuitTable = response.MRData.CircuitTable
                Log.d("RacesViewModel", "CircuitTable (offset=$offset): $circuitTable")

                val circuits = circuitTable?.Circuits ?: emptyList()
                Log.d("RacesViewModel", "Circuits (offset=$offset): $circuits (Size: ${circuits.size})")

                allCircuits.addAll(circuits)

                // Parse total and offset from response
                val total = response.MRData.total.toIntOrNull() ?: 0
                Log.d("RacesViewModel", "Total Circuits: $total, Current Offset: $offset, Fetched: ${allCircuits.size}")

                // Increment offset for next page
                offset += limit

                // Continue if more circuits remain
            } while (offset < total && circuits.isNotEmpty())

            if (allCircuits.isEmpty()) {
                Log.w("RacesViewModel", "No circuits returned from API")
            } else {
                Log.d("RacesViewModel", "Total Circuits Fetched: ${allCircuits.size}")
            }

            allCircuits
        } catch (e: Exception) {
            Log.e("RacesViewModel", "Error fetching circuits: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getRaceForSeasonAndCircuit(season : Int, circuit: Circuit) : Race? {

        val circuitId = circuit.circuitId

        Log.i("getRaceForSeasonAndCircuit", circuitId)

        return try {
            val response = raceApi.fetchRaceForSeasonAndCircuit(season, circuitId)
            val races = response.MRData.RaceTable?.Races ?: emptyList()
            Log.i("getRaceForSeasonAndCircuit", "season: ${races[0].season} + round${races[0].round}")
            races[0]
        } catch (e: Exception) {
            Log.e("Error", "fetching race: ${e.message}")
            null
        }
    }


}