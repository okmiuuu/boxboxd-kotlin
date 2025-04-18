package com.example.boxboxd.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.boxboxd.R
import com.example.boxboxd.core.inner.Entry
import com.example.boxboxd.core.inner.RaceRepository
import com.example.boxboxd.core.inner.User
import com.example.boxboxd.core.inner.objects.Collections
import com.example.boxboxd.core.inner.objects.Fields
import com.example.boxboxd.core.jolpica.Circuit
import com.example.boxboxd.core.jolpica.Driver
import com.example.boxboxd.core.jolpica.Race
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class RacesViewModel(private val repository: RaceRepository) : ViewModel() {
    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private val _raceEntries = MutableStateFlow<List<Entry>>(emptyList())
    val raceEntries: StateFlow<List<Entry>> = _raceEntries.asStateFlow()

    private val _racesThisSeason = MutableStateFlow<List<Race>>(emptyList())
    val racesThisSeason: StateFlow<List<Race>> = _racesThisSeason.asStateFlow()

    private val _isLoadingThisSeason = MutableStateFlow(false)
    val isLoadingThisSeason: StateFlow<Boolean> = _isLoadingThisSeason.asStateFlow()

    fun fetchRacesForSeason(seasonYear: Int) {
        viewModelScope.launch {
            _isLoadingThisSeason.value = true
            try {
                val racesForSeason = repository.getRacesForSeason(seasonYear)
                _racesThisSeason.value = racesForSeason
            } catch (e: Exception) {
                _racesThisSeason.value = emptyList()
            } finally {
                _isLoadingThisSeason.value = false
            }
        }
    }

    fun isEntryLikedByCurrentUser(entry: Entry, currentUser: User): Boolean {
        val userId = currentUser.id ?: return false // No user ID, can't be liked
        val likesFrom = entry.likesFrom ?: emptyList() // Safe access to likesFrom

        Log.i("entryId", entry.id ?: "")
        Log.i("userId", userId)

        val isLiked = likesFrom.any { user ->
            user.id == userId
        }

        Log.i("isLiked", isLiked.toString())

        return isLiked
    }

    fun getDrawableResourceId(context: Context, drawableName: String): Int {
        val resource = context.resources.getIdentifier(drawableName.lowercase(), "drawable", context.packageName)
        return if (resource == 0) {
            R.drawable.americas
        } else resource
    }

    fun checkIfTheRaceHasPassed(race: Race): Boolean {
        val raceTimestamp = getRaceTimestamp(race)
        Log.i("timestamp", raceTimestamp.toString())
        return raceTimestamp < getCurrentTimestamp()
    }

    fun getRaceTimestamp(race: Race): Long {
        val raceDateString = race.date
        val raceTimeString = race.time
        val raceTimestampString = "$raceDateString $raceTimeString"
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssX")
        val localDateTime = LocalDateTime.parse(raceTimestampString, formatter)
        val zonedDateTime = localDateTime.atZone(ZoneId.systemDefault())
        val timestamp = zonedDateTime.toInstant().toEpochMilli()
        return timestamp
    }

    fun getCurrentTimestamp(): Long {
        val currentInstant = Instant.now()
        val adjustedInstant = currentInstant.plus(Duration.ofHours(2))
        return adjustedInstant.toEpochMilli()
    }

    fun convertToLocalTime(raceDateString: String, raceTimeString: String): String {
        val raceTimestampString = "$raceDateString $raceTimeString"
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssX")
        val utcDateTime = ZonedDateTime.parse(raceTimestampString, formatter)
        val localDateTime = utcDateTime.withZoneSameInstant(ZoneId.systemDefault())
        val outputFormatter = DateTimeFormatter.ofPattern("HH:mm")
        return localDateTime.format(outputFormatter)
    }

    suspend fun getRaceWithResults (race: Race) : Race? {
        return repository.getRaceForSeasonAndCircuit(race.season, race.Circuit ?: Circuit())
    }

    suspend fun getLastWinner(race: Race) : Driver? {
        val lastRace : Race?
        if (checkIfTheRaceHasPassed(race)) {
            lastRace = repository.getRaceForSeasonAndCircuit(LocalDate.now().year, race.Circuit ?: Circuit())
        } else {
            lastRace = repository.getRaceForSeasonAndCircuit(LocalDate.now().year - 1, race.Circuit ?: Circuit())
        }

        if (lastRace != null) {
            return getDriverAtPositionForRace(1, lastRace)
        }
        return null
    }

    fun getDriverAtPositionForRace(position : Int, race : Race) : Driver? {
        return race.Results?.find { it.position == position }?.Driver
    }

    fun getRaceEntries(race: Race, userId: String? = null) {
        Log.i("RacesViewModel", "Querying for race: ${race.raceName}, season: ${race.season}, userId: $userId")

        val query = db.collection(Collections.ENTRIES)
            .whereEqualTo("race.round", race.round)
            .whereEqualTo("race.season", race.season)


//        if (userId != null) {
//            query = query.whereEqualTo("userId", userId)
//        }

        query.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e("RacesViewModel", "Error fetching entries", e)
                _raceEntries.value = emptyList()
                return@addSnapshotListener
            }

            if (snapshot != null) {
                Log.i("RacesViewModel", "Snapshot documents: ${snapshot.documents.size}")
                val entries = try {
                    snapshot.toObjects(Entry::class.java)
                } catch (ex: Exception) {
                    Log.e("RacesViewModel", "Serialization error", ex)
                    emptyList()
                }
                Log.i("RacesViewModel", "Fetched ${entries.size} entries")
                _raceEntries.value = entries
            } else {
                Log.w("RacesViewModel", "Snapshot is null")
                _raceEntries.value = emptyList()
            }
        }
    }

    fun getFullResultTableForRace(race : Race?) : Map<Int,Driver> {

        val resultTable = mutableMapOf<Int, Driver>()

        if (race?.Results != null) {
            race.Results.forEach{ result ->
                resultTable[result.position] = result.Driver
            }
        }

        return resultTable
    }

}