package com.example.boxboxd.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.yml.charts.common.extensions.isNotNull
import com.example.boxboxd.R
import com.example.boxboxd.core.inner.Entry
import com.example.boxboxd.core.inner.RaceRepository
import com.example.boxboxd.core.inner.User
import com.example.boxboxd.core.inner.objects.Collections
import com.example.boxboxd.core.inner.objects.Fields
import com.example.boxboxd.core.inner.objects.MapObjects
import com.example.boxboxd.core.jolpica.Circuit
import com.example.boxboxd.core.jolpica.Driver
import com.example.boxboxd.core.jolpica.Race
import com.example.boxboxd.model.DropdownItem
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
            val collectionRef = db.collection(Collections.DRIVERS)

            val batch: WriteBatch = db.batch()
            drivers.forEach { driver ->
                val docRef = collectionRef.document(driver.driverId ?: "")
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

    suspend fun getCircuitsFromRepository() : List<Circuit?> {
        return repository.getAllCircuits()
    }

    fun addPicturesToCircuitsList(context: Context, circuitsList : List<Circuit?>) : List<DropdownItem> {

        val resultList: MutableList<DropdownItem> = mutableListOf()

        circuitsList.forEach { circuit ->
            val drawableId = getDrawableResourceId(
                context,
                circuit?.circuitId
            )

            resultList.add(DropdownItem(circuit?.circuitName ?: "", drawableId))
        }

        return resultList

    }

    suspend fun getCircuitsWithPicturesList(context: Context): List<DropdownItem> = withContext(Dispatchers.IO) {
        val circuitsList = getCircuitsFromRepository()
        addPicturesToCircuitsList(context, circuitsList)
    }


    fun getListOfTeamItems(): List<DropdownItem> {

        val resultList: MutableList<DropdownItem> = mutableListOf()

        val teams : List<String> = listOf(
            "Oracle Red Bull Racing",
            "McLaren Formula 1 Team",
            "Scuderia Ferrari HP",
            "Mercedes AMG Petronas F1 Team",
            "Aston Martin Aramco F1 Team",
            "BWT Alpine F1 Team",
            "Atlassian Williams Racing",
            "MoneyGram Haas F1 Team",
            "Stake F1 Team Kick Sauber",
            "Visa Cash App Racing Bulls F1 Team")

        teams.forEach { teamName ->
            val team = MapObjects.stringNameToTeam[teamName.lowercase()]
            val picture = MapObjects.teamToPicture[team]

            resultList.add(DropdownItem(teamName, picture))
        }
        return resultList
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

    fun getDrawableResourceId(context: Context, drawableName: String?): Int {
        val resource = context.resources.getIdentifier(drawableName?.lowercase(), "drawable", context.packageName)
        return if (resource == 0) {
            R.drawable.placeholder
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
        var lastRace = if (checkIfTheRaceHasPassed(race)) {
            repository.getRaceForSeasonAndCircuit(LocalDate.now().year, race.Circuit ?: Circuit())
        } else {
            repository.getRaceForSeasonAndCircuit(LocalDate.now().year - 1, race.Circuit ?: Circuit())
        }

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


    suspend fun getListOfDrivers(): List<Driver> = withContext(Dispatchers.IO) {
        try {
            val query = db.collection("drivers")
            val snapshot = query.get().await()

            if (snapshot.isEmpty) {
                println("Drivers collection is empty")
                return@withContext emptyList()
            }

            val driverNames = snapshot.documents.mapNotNull { document ->
                try {
                    val code = document.getString(Fields.CODE)
                    val dateOfBirth = document.getString(Fields.DATE_OF_BIRTH)
                    val driverId = document.getString(Fields.DRIVER_ID)
                    val familyName = document.getString(Fields.FAMILY_NAME)
                    val givenName = document.getString(Fields.GIVEN_NAME)
                    val nationality = document.getString(Fields.NATIONALITY)
                    val permanentNumber = document.getLong(Fields.PERMANENT_NUMBER)?.toInt()
                    val url = document.getString(Fields.URL)

                    if (!givenName.isNullOrEmpty() &&
                        !familyName.isNullOrEmpty() &&
                        !driverId.isNullOrEmpty() &&
                        !nationality.isNullOrEmpty() &&
                        permanentNumber != null &&
                        !url.isNullOrEmpty() &&
                        !code.isNullOrEmpty() &&
                        !dateOfBirth.isNullOrEmpty()) {

                        Driver(
                            code = code,
                            dateOfBirth = dateOfBirth,
                            driverId = driverId,
                            familyName = familyName,
                            givenName = givenName,
                            nationality = nationality,
                            permanentNumber = permanentNumber,
                            url = url
                        )
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


    suspend fun getDriverObjectFromName(fullName: String): Driver? = withContext(Dispatchers.IO) {
        // Return null if fullName is empty or doesn't contain a space
        if (fullName.isBlank() || !fullName.contains(" ")) {
            println("Invalid fullName: $fullName")
            return@withContext null
        }

        try {
            // Split fullName into givenName and familyName
            val indexOfSpace = fullName.indexOf(" ")
            val givenName = fullName.substring(0, indexOfSpace).trim()
            val familyName = fullName.substring(indexOfSpace + 1).trim()

            // Query Firestore for a driver with matching givenName and familyName
            val query = db.collection("drivers")
                .whereEqualTo(Fields.GIVEN_NAME, givenName)
                .whereEqualTo(Fields.FAMILY_NAME, familyName)
                .limit(1) // We only need one match

            val snapshot = query.get().await()

            if (snapshot.isEmpty) {
                println("No driver found for givenName: $givenName, familyName: $familyName")
                return@withContext null
            }

            // Get the first matching document
            val document = snapshot.documents.firstOrNull()
            if (document == null) {
                println("No driver document found for givenName: $givenName, familyName: $familyName")
                return@withContext null
            }

            // Extract fields and create Driver object
            val code = document.getString(Fields.CODE)
            val dateOfBirth = document.getString(Fields.DATE_OF_BIRTH)
            val driverId = document.getString(Fields.DRIVER_ID)
            val nationality = document.getString(Fields.NATIONALITY)
            val permanentNumber = document.getLong(Fields.PERMANENT_NUMBER)?.toInt()
            val url = document.getString(Fields.URL)

            if (!givenName.isNullOrEmpty() &&
                !familyName.isNullOrEmpty() &&
                !driverId.isNullOrEmpty() &&
                !nationality.isNullOrEmpty() &&
                permanentNumber != null &&
                !url.isNullOrEmpty() &&
                !code.isNullOrEmpty() &&
                !dateOfBirth.isNullOrEmpty()
            ) {
                val driver = Driver(
                    code = code,
                    dateOfBirth = dateOfBirth,
                    driverId = driverId,
                    familyName = familyName,
                    givenName = givenName,
                    nationality = nationality,
                    permanentNumber = permanentNumber,
                    url = url
                )
                println("Found driver: $driver")
                driver
            } else {
                println("Missing or invalid data for document ${document.id}")
                null
            }
        } catch (e: Exception) {
            println("Error in getDriverObjectFromName: ${e.message}")
            null
        }
    }

}