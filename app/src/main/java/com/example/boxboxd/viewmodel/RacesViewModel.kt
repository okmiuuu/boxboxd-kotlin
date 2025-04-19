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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
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

    private val _racesLastSeason = MutableStateFlow<List<Race>>(emptyList())
    val racesLastSeason: StateFlow<List<Race>> = _racesLastSeason.asStateFlow()

    private val _isLoadingLastSeason = MutableStateFlow(false)
    val isLoadingLastSeason: StateFlow<Boolean> = _isLoadingLastSeason.asStateFlow()

    fun fetchRacesForLastSeason(seasonYear: Int) {
        viewModelScope.launch {
            _isLoadingLastSeason.value = true
            try {
                val racesForSeason = repository.getRacesForSeason(seasonYear - 1)
                _racesLastSeason.value = racesForSeason
            } catch (e: Exception) {
                _racesLastSeason.value = emptyList()
            } finally {
                _isLoadingLastSeason.value = false
            }
        }
    }

    fun fetchRacesForThisSeason(seasonYear: Int) {
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

    fun loadAndUploadDrivers() {
        viewModelScope.launch {
            println("Starting driver upload process")
            val allDrivers = mutableListOf<Driver>()

            // Collect drivers for each season concurrently
            val deferredDrivers = (1950..2025).map { seasonYear ->
                async(Dispatchers.IO) {
                    try {
                        val driversForSeason = repository.getDriversForSeason(seasonYear)
                        println("Loaded ${driversForSeason.size} drivers for season $seasonYear")
                        driversForSeason
                    } catch (e: Exception) {
                        println("Error loading drivers for season $seasonYear: ${e.message}")
                        emptyList<Driver>() // Return empty list for failed season
                    }
                }
            }

            val seasonDrivers = deferredDrivers.awaitAll()
            allDrivers.addAll(seasonDrivers.flatten())
            println("Total drivers loaded: ${allDrivers.size}")

            val uniqueDrivers = deduplicateDrivers(allDrivers)
            println("Found ${uniqueDrivers.size} unique drivers")

            uploadDriversToFirestore(db, uniqueDrivers).fold(
                onSuccess = { println("Upload completed successfully") },
                onFailure = { e -> println("Upload failed: ${e.message}") }
            )
        }
    }

    private fun deduplicateDrivers(drivers: List<Driver>): List<Driver> {
        val uniqueDrivers = drivers.distinctBy { it.driverId }
        println("Deduplicated ${drivers.size} drivers to ${uniqueDrivers.size} unique drivers")
        return uniqueDrivers
    }



    suspend fun uploadDriversToFirestore(db: FirebaseFirestore, drivers: List<Driver>): Result<Unit> {
        return try {
            println("Attempting to upload ${drivers.size} drivers")
            val collectionRef = db.collection("drivers")

            val batch: WriteBatch = db.batch()
            drivers.forEach { driver ->
                val docRef = collectionRef.document(driver.driverId)
                println("Writing driver ${driver.driverId}: $driver")
                batch.set(docRef, driver)
            }

            batch.commit().await()
            println("Successfully uploaded ${drivers.size} drivers to Firestore")
            Result.success(Unit)
        } catch (e: Exception) {
            println("Error uploading drivers to Firestore: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getListOfDriverNames(): List<String> = withContext(Dispatchers.IO) {
        try {
            val query = db.collection("drivers")
            val snapshot = query.get().await()

            if (snapshot.isEmpty) {
                println("Drivers collection is empty")
                return@withContext emptyList()
            }

            val driverNames = snapshot.documents.mapNotNull { document ->
                try {
                    val givenName = document.getString("givenName")
                    val familyName = document.getString("familyName")
                    if (givenName != null && familyName != null) {
                        "$givenName $familyName"
                    } else {
                        println("Missing givenName or familyName for document ${document.id}")
                        null
                    }
                } catch (e: Exception) {
                    println("Error processing document ${document.id}: ${e.message}")
                    null
                }
            }

            println("Retrieved ${driverNames.size} driver names: $driverNames")
            driverNames
        } catch (e: Exception) {
            println("Error querying drivers collection: ${e.message}")
            emptyList()
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
        Log.i("GET LAST WINNER", "season: ${race.season} + round${race.round}")

        Log.i("GET LAST WINNER CHECK", checkIfTheRaceHasPassed(race).toString())

        var lastRace = if (checkIfTheRaceHasPassed(race)) {
            Log.i("aaa", "aaa")
            repository.getRaceForSeasonAndCircuit(LocalDate.now().year, race.Circuit ?: Circuit())
        } else {
            Log.i("bbb", "bbb")
            repository.getRaceForSeasonAndCircuit(LocalDate.now().year - 1, race.Circuit ?: Circuit())
        }

        Log.i("GET LAST WINNER", "season: ${lastRace?.season} + round${lastRace?.round}")

        if (lastRace != null) {
            return getDriverAtPositionForRace(1, lastRace)
        }

        lastRace = repository.getRaceForSeasonAndCircuit(LocalDate.now().year - 1, race.Circuit ?: Circuit())

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