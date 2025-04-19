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