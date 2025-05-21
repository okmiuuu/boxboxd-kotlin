package com.example.boxboxd.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.boxboxd.core.inner.CustomList
import com.example.boxboxd.core.inner.Entry
import com.example.boxboxd.core.inner.User
import com.example.boxboxd.core.inner.enums.StatTypes
import com.example.boxboxd.core.inner.enums.Teams
import com.example.boxboxd.core.inner.objects.Collections
import com.example.boxboxd.core.inner.objects.Fields
import com.example.boxboxd.core.jolpica.Circuit
import com.example.boxboxd.core.jolpica.Driver
import com.example.boxboxd.core.jolpica.Race
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AccountViewModel(private val navController: NavController) : ViewModel() {
    private val db: FirebaseFirestore = Firebase.firestore
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val userId get() = auth.currentUser?.uid
    private val userDocRef get() = userId?.let { db.collection(Collections.USERS).document(it) }

    private val _userPhotoUrl = MutableStateFlow<Uri?>(null)
    val userPhotoUrl: StateFlow<Uri?> = _userPhotoUrl.asStateFlow()

    private val _userDisplayName = MutableStateFlow<String?>(null)
    val userDisplayName: StateFlow<String?> = _userDisplayName.asStateFlow()

    private val _userEmail = MutableStateFlow<String?>(null)
    val userEmail: StateFlow<String?> = _userEmail.asStateFlow()

    private val _isAdmin = MutableStateFlow<Boolean?>(false)
    val isAdmin: StateFlow<Boolean?> = _isAdmin.asStateFlow()

    private val _userFavDriver = MutableStateFlow<Driver?>(null)
    val userFavDriver: StateFlow<Driver?> = _userFavDriver.asStateFlow()

    private val _userFavTeam = MutableStateFlow<Teams?>(null)
    val userFavTeam: StateFlow<Teams?> = _userFavTeam.asStateFlow()

    private val _userFavCircuit = MutableStateFlow<Circuit?>(null)
    val userFavCircuit: StateFlow<Circuit?> = _userFavCircuit.asStateFlow()

    private val _userEntries = MutableStateFlow<List<Entry>?>(null)
    val userEntries: StateFlow<List<Entry>?> = _userEntries.asStateFlow()

    private val _userLists = MutableStateFlow<List<CustomList>?>(null)
    val userLists: StateFlow<List<CustomList>?> = _userLists.asStateFlow()

    private val _userObject = MutableStateFlow<User>(User())
    val userObject: StateFlow<User> = _userObject.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _expandedStates = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val expandedStates: StateFlow<Map<String, Boolean>> = _expandedStates.asStateFlow()

    private val _navigationEvents = MutableSharedFlow<NavigationEvent>()
    val navigationEvents: SharedFlow<NavigationEvent> = _navigationEvents.asSharedFlow()

    init {
        Log.d("AccountViewModel", "Initializing, userId: $userId")
        if (userId != null) {
            fetchUserData()
        } else {
            _isLoading.value = false
        }
    }

    sealed class NavigationEvent {
        data class NavigateToUserScreen(val user: User) : NavigationEvent()
        data class NavigateToRaceScreen(val race: Race) : NavigationEvent()
        object NavigateToEntriesScreen : NavigationEvent()
        object NavigateToMainScreen : NavigationEvent()
        object NavigateToLoginScreen : NavigationEvent()
        object NavigateToRegistrationScreen : NavigationEvent()
        object NavigateToListsScreen : NavigationEvent()
        data class NavigateToRacesSearchScreen(val races: List<Race?>) : NavigationEvent()
        object NavigateBack : NavigationEvent()
    }

    fun requestNavigateToUserScreen(user: User) {
        viewModelScope.launch {
            _navigationEvents.emit(NavigationEvent.NavigateToUserScreen(user))
        }
    }

    fun requestNavigateToMainScreen() {
        viewModelScope.launch {
            _navigationEvents.emit(NavigationEvent.NavigateToMainScreen)
        }
    }

    fun requestNavigateToLoginScreen() {
        viewModelScope.launch {
            _navigationEvents.emit(NavigationEvent.NavigateToLoginScreen)
        }
    }

    fun requestNavigateToRegistrationScreen() {
        viewModelScope.launch {
            _navigationEvents.emit(NavigationEvent.NavigateToRegistrationScreen)
        }
    }

    fun requestNavigateToRaceScreen(race: Race) {
        viewModelScope.launch {
            _navigationEvents.emit(NavigationEvent.NavigateToRaceScreen(race))
        }
    }

    fun requestNavigateToEntriesScreen() {
        viewModelScope.launch {
            _navigationEvents.emit(NavigationEvent.NavigateToEntriesScreen)
        }
    }

    fun requestNavigateToListsScreen() {
        viewModelScope.launch {
            _navigationEvents.emit(NavigationEvent.NavigateToListsScreen)
        }
    }

    fun requestNavigateToRacesSearchScreen(racesList: List<Race?>) {
        viewModelScope.launch {
            _navigationEvents.emit(NavigationEvent.NavigateToRacesSearchScreen(racesList))
        }
    }

    fun requestNavigateBack() {
        viewModelScope.launch {
            _navigationEvents.emit(NavigationEvent.NavigateBack)
        }
    }

    fun getIfRaceAlreadyLoggedByUser(race: Race): Boolean {
        val entriesList = userEntries.value
        if (!entriesList.isNullOrEmpty()) {
            val loggedRace = entriesList.find {
                it.race?.season == race.season && it.race.round == race.round
            }
            loggedRace?.let { return true }
        }
        return false
    }

    fun fetchUserData() {
        Log.d("AccountViewModel", "fetchUserData called, userId: ${auth.currentUser?.uid}")
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.w("AccountViewModel", "No authenticated user, waiting for auth state")
            // Don't reset userObject; wait for auth state change
            _isLoading.value = false
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = currentUser.uid
                val userDocRef = db.collection(Collections.USERS).document(userId)
                val documentSnapshot = userDocRef.get().await()

                if (documentSnapshot.exists()) {
                    Log.d("AccountViewModel", "Raw document data: ${documentSnapshot.data}")
                    val user = documentSnapshot.toObject(User::class.java)?.copy(id = userId)
                        ?: User(id = userId)
                    Log.d("AccountViewModel", "Fetched user data: id=${user.id}, email=${user.email}, username=${user.username}, picture=${user.picture}")
                    _userObject.value = user
                    updateUserFields(user)
                } else {
                    Log.w("AccountViewModel", "No user document found for userId=$userId, using Firebase Auth data")
                    val fallbackUser = User(
                        id = userId,
                        email = currentUser.email,
                        username = currentUser.email?.substringBefore("@"),
                        picture = currentUser.photoUrl?.toString()
                    )
                    _userObject.value = fallbackUser
                    updateUserFields(fallbackUser)
                    userDocRef.set(fallbackUser).await()
                    Log.d("AccountViewModel", "Created new user document for userId=$userId")
                }

                userDocRef.addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.e("AccountViewModel", "Snapshot listener failed: ${e.message}", e)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        Log.d("AccountViewModel", "Snapshot received: id=${snapshot.id}, data=${snapshot.data}")
                        val user = snapshot.toObject(User::class.java)?.copy(id = userId)
                            ?: User(id = userId)
                        _userObject.value = user
                        updateUserFields(user)
                    } else {
                        Log.w("AccountViewModel", "Snapshot empty or document missing for userId=$userId")
                        val fallbackUser = User(
                            id = userId,
                            email = auth.currentUser?.email,
                            username = auth.currentUser?.email?.substringBefore("@"),
                            picture = auth.currentUser?.photoUrl?.toString()
                        )
                        _userObject.value = fallbackUser
                        updateUserFields(fallbackUser)
                    }
                }

                // Trigger navigation to MainScreen after successful fetch
                //requestNavigateToMainScreen()
            } catch (e: Exception) {
                Log.e("AccountViewModel", "Error fetching user data: ${e.message}", e)
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    val fallbackUser = User(
                        id = userId,
                        email = auth.currentUser?.email,
                        username = auth.currentUser?.email?.substringBefore("@"),
                        picture = auth.currentUser?.photoUrl?.toString()
                    )
                    _userObject.value = fallbackUser
                    updateUserFields(fallbackUser)
                    requestNavigateToMainScreen()
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun updateUserFields(user: User) {
        Log.d("AccountViewModel", "Updating user fields: id=${user.id}, username=${user.username}")
        getUserEntries(user)
        getAllUserLists(user)
        _userFavDriver.value = user.favDriver
        _userFavTeam.value = user.favTeam
        _userFavCircuit.value = user.favCircuit
        _isAdmin.value = user.admin
        _userPhotoUrl.value = auth.currentUser?.photoUrl
        _userDisplayName.value = auth.currentUser?.displayName ?: user.username
        _userEmail.value = auth.currentUser?.email ?: user.email
    }

    fun clearUserObject() {
        Log.d("AccountViewModel", "Clearing userObject and related states")
        _userObject.value = User()
        _userPhotoUrl.value = null
        _userDisplayName.value = null
        _userEmail.value = null
        _isAdmin.value = false
        _userFavDriver.value = null
        _userFavTeam.value = null
        _userFavCircuit.value = null
        _userEntries.value = null
        _userLists.value = null
        _isLoading.value = false
    }

    fun createList(customList: CustomList) {
        db.collection(Collections.CUSTOM_LISTS)
            .add(customList)
            .addOnSuccessListener { documentRef ->
                documentRef.update("id", documentRef.id)
                Log.i("AccountViewModel", "List added with ID: ${documentRef.id}")
            }
            .addOnFailureListener { e ->
                Log.e("AccountViewModel", "Error creating list", e)
            }
    }

    fun addRaceToTheList(
        race: Race,
        list: CustomList,
        onSuccess: () -> Unit = {},
        onDuplicate: () -> Unit = {}
    ) {
        val listId = list.id ?: return
        val listRef = db.collection(Collections.CUSTOM_LISTS).document(listId)

        viewModelScope.launch {
            try {
                val snapshot = listRef.get().await()
                if (!snapshot.exists()) {
                    Log.e("AccountViewModel", "List does not exist")
                    return@launch
                }

                val currentListItems = snapshot.toObject(CustomList::class.java)?.listItems
                    ?: emptyList()
                val isDuplicate = currentListItems.any { existingRace ->
                    existingRace.season == race.season && existingRace.round == race.round
                }

                if (isDuplicate) {
                    Log.i("AccountViewModel", "Duplicate race detected")
                    onDuplicate()
                    return@launch
                }

                db.runTransaction { transaction ->
                    val freshSnapshot = transaction.get(listRef)
                    if (!freshSnapshot.exists()) {
                        throw FirebaseFirestoreException(
                            "List does not exist",
                            FirebaseFirestoreException.Code.NOT_FOUND
                        )
                    }

                    val freshListItems = freshSnapshot.toObject(CustomList::class.java)?.listItems
                        ?: emptyList()
                    val updatedListItems = freshListItems + race
                    transaction.update(listRef, "listItems", updatedListItems)
                }.addOnSuccessListener {
                    Log.i("AccountViewModel", "Race added to list successfully")
                    onSuccess()
                }.addOnFailureListener { e ->
                    Log.e("AccountViewModel", "Failed to add race to list: ${e.message}")
                }
            } catch (e: Exception) {
                Log.e("AccountViewModel", "Failed to read list: ${e.message}")
            }
        }
    }

    fun deleteRaceFromList(race: Race, customList: CustomList): Boolean {
        if (customList.id == null) {
            Log.e("AccountViewModel", "Invalid input: listId=${customList.id}, race.season=${race.season}, race.round=${race.round}")
            return false
        }

        try {
            val raceToDelete = customList.listItems?.find { item ->
                item.season == race.season && item.round == race.round
            }

            if (raceToDelete == null) {
                Log.d("AccountViewModel", "Race not found in list ${customList.id}: season=${race.season}, round=${race.round}")
                return false
            }

            if (customList.listItems.size == 1) {
                db.collection(Collections.CUSTOM_LISTS)
                    .document(customList.id)
                    .update(Fields.LIST_ITEMS, listOf<Race>())
            } else {
                db.collection(Collections.CUSTOM_LISTS)
                    .document(customList.id)
                    .update(Fields.LIST_ITEMS, FieldValue.arrayRemove(raceToDelete))
            }

            Log.d("AccountViewModel", "Deleted race: ${race.raceName} (season=${race.season}, round=${race.round}) from list ${customList.id}")
            return true
        } catch (e: Exception) {
            Log.e("AccountViewModel", "Error deleting race from list ${customList.id}: ${e.message}", e)
            return false
        }
    }

    fun deleteList(customList: CustomList) {
        customList.id?.let { listId ->
            db.collection(Collections.CUSTOM_LISTS)
                .document(listId)
                .delete()
                .addOnSuccessListener {
                    Log.d("AccountViewModel", "List deleted: $listId")
                    _userLists.update { currentLists ->
                        currentLists?.filterNot { it.id == listId }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("AccountViewModel", "Error deleting list: $listId", e)
                }
        } ?: run {
            Log.e("AccountViewModel", "Cannot delete list: ID is null")
        }
    }

    fun getUserStat(statType: StatTypes, user: User): Int {
        getUserEntries(user)
        getAllUserLists(user)
        val typeToStat = mapOf(
            StatTypes.ENTRIES to _userEntries.value?.size,
            StatTypes.LISTS to _userLists.value?.size,
        )
        return typeToStat[statType] ?: 0
    }

    private fun getAllUserLists(user: User) {
        val query = db.collection(Collections.CUSTOM_LISTS)
            .whereEqualTo("user.id", user.id)

        query.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e("AccountViewModel", "Error fetching lists: ${e.message}", e)
                _userLists.value = emptyList()
                return@addSnapshotListener
            }

            if (snapshot != null) {
                Log.d("AccountViewModel", "Lists snapshot: ${snapshot.documents.size} documents")
                val lists = try {
                    snapshot.toObjects(CustomList::class.java)
                } catch (ex: Exception) {
                    Log.e("AccountViewModel", "Serialization error: ${ex.message}", ex)
                    emptyList()
                }
                Log.d("AccountViewModel", "Fetched ${lists.size} lists")
                _userLists.value = lists
            } else {
                Log.w("AccountViewModel", "Lists snapshot is null")
                _userLists.value = emptyList()
            }
        }
    }

    fun persistUriPermission(context: Context, uri: Uri) {
        try {
            val contentResolver = context.contentResolver
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            contentResolver.takePersistableUriPermission(uri, takeFlags)
        } catch (e: SecurityException) {
            Log.e("AccountViewModel", "SecurityException in persistUriPermission: ${e.message}", e)
        }
    }

    fun logRace(entry: Entry) {
        db.collection(Collections.ENTRIES)
            .add(entry)
            .addOnSuccessListener { documentRef ->
                documentRef.update("id", documentRef.id)
                Log.i("AccountViewModel", "Entry added with ID: ${documentRef.id}")
            }
            .addOnFailureListener { e ->
                Log.e("AccountViewModel", "Error logging race: ${e.message}", e)
            }
    }

    fun followUser(user: User) {
        // TODO: Implement follow user logic
    }

    fun goToSettings(user: User) {
        // TODO: Implement go to settings logic
    }

    fun logOut() {
        Log.d("AccountViewModel", "Logging out, currentUser: ${auth.currentUser?.uid}")
        auth.signOut()
        _isLoading.value = false
        clearUserObject()
        requestNavigateToLoginScreen()
        Log.d("AccountViewModel", "Logout completed, navigated to LoginScreen")
    }

    fun checkIfThatIsYourPage(user: User): Boolean {
        return user.id == this.userId
    }

    fun fetchUser() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = getCurrentUser()
                _userObject.value = user
                updateUserFields(user)
                Log.d("AccountViewModel", "Fetched user: id=${user.id}, email=${user.email}, picture=${user.picture}")
            } catch (e: Exception) {
                Log.e("AccountViewModel", "Error fetching user: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun getCurrentUser(): User = withContext(Dispatchers.IO) {
        val userId = auth.currentUser?.uid
        Log.d("AccountViewModel", "getCurrentUser, userId: $userId")

        if (userId == null) {
            Log.w("AccountViewModel", "No authenticated user found")
            return@withContext User()
        }

        val userDocRef = db.collection(Collections.USERS).document(userId)
        try {
            val documentSnapshot = userDocRef.get().await()
            if (documentSnapshot.exists()) {
                Log.d("AccountViewModel", "User document exists for userId: $userId, data=${documentSnapshot.data}")
                val user = documentSnapshot.toObject(User::class.java)?.copy(id = userId)
                    ?: User(id = userId)
                return@withContext user
            } else {
                Log.w("AccountViewModel", "No user document for userId: $userId")
                return@withContext User(
                    id = userId,
                    email = auth.currentUser?.email,
                    username = auth.currentUser?.email?.substringBefore("@"),
                    picture = auth.currentUser?.photoUrl?.toString()
                )
            }
        } catch (e: Exception) {
            Log.e("AccountViewModel", "Error fetching user: ${e.message}", e)
            return@withContext User(
                id = userId,
                email = auth.currentUser?.email,
                username = auth.currentUser?.email?.substringBefore("@"),
                picture = auth.currentUser?.photoUrl?.toString()
            )
        }
    }

    fun getUserEntries(user: User) {
        val query = db.collection(Collections.ENTRIES)
            .whereEqualTo("user.id", user.id)

        query.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e("AccountViewModel", "Error fetching entries: ${e.message}", e)
                _userEntries.value = emptyList()
                return@addSnapshotListener
            }

            if (snapshot != null) {
                Log.d("AccountViewModel", "Entries snapshot: ${snapshot.documents.size} documents")
                val entries = try {
                    snapshot.toObjects(Entry::class.java)
                } catch (ex: Exception) {
                    Log.e("AccountViewModel", "Serialization error: ${ex.message}", ex)
                    emptyList()
                }
                Log.d("AccountViewModel", "Fetched ${entries.size} entries")
                _userEntries.value = entries
            } else {
                Log.w("AccountViewModel", "Entries snapshot is null")
                _userEntries.value = emptyList()
            }
        }
    }

    fun getGradeStatsForUser(): Map<Int, Int> {
        val userEntries = _userEntries.value ?: emptyList()
        Log.d("AccountViewModel", "USER ENTRIES COUNT: ${userEntries.size}")

        val gradeToCountMap = mutableMapOf(0 to 0, 1 to 0, 2 to 0, 3 to 0, 4 to 0, 5 to 0)
        userEntries.forEach { entry ->
            val currentCount = gradeToCountMap[entry.rating] ?: 0
            gradeToCountMap[entry.rating ?: 0] = currentCount + 1
        }
        return gradeToCountMap
    }

    fun addLikeToEntry(entry: Entry) {
        val entryId = entry.id ?: return
        val listRef = db.collection(Collections.ENTRIES).document(entryId)

        viewModelScope.launch {
            try {
                val snapshot = listRef.get().await()
                if (!snapshot.exists()) {
                    Log.e("AccountViewModel", "Entry does not exist")
                    return@launch
                }

                val currentLikes = snapshot.toObject(Entry::class.java)?.likesFrom ?: emptyList()
                val isDuplicate = currentLikes.any { user -> user.id == userId }

                if (isDuplicate) {
                    Log.i("AccountViewModel", "Duplicate like detected")
                    return@launch
                }

                db.runTransaction { transaction ->
                    val freshSnapshot = transaction.get(listRef)
                    if (!freshSnapshot.exists()) {
                        throw FirebaseFirestoreException(
                            "Entry does not exist",
                            FirebaseFirestoreException.Code.NOT_FOUND
                        )
                    }

                    val freshLikes = freshSnapshot.toObject(Entry::class.java)?.likesFrom
                        ?: emptyList()
                    val updatedLikes = freshLikes + userObject.value
                    transaction.update(listRef, "likesFrom", updatedLikes)
                }.addOnSuccessListener {
                    Log.i("AccountViewModel", "Like added to entry successfully")
                }.addOnFailureListener { e ->
                    Log.e("AccountViewModel", "Failed to add like to entry: ${e.message}")
                }
            } catch (e: Exception) {
                Log.e("AccountViewModel", "Failed to read entry: ${e.message}")
            }
        }
    }

    fun removeLikeFromEntry(entry: Entry) {
        val entryId = entry.id ?: return
        val listRef = db.collection(Collections.ENTRIES).document(entryId)

        viewModelScope.launch {
            try {
                val snapshot = listRef.get().await()
                if (!snapshot.exists()) {
                    Log.e("AccountViewModel", "Entry does not exist")
                    return@launch
                }

                val currentLikes = snapshot.toObject(Entry::class.java)?.likesFrom ?: emptyList()
                val userHasLiked = currentLikes.any { user -> user.id == userId }

                if (!userHasLiked) {
                    Log.i("AccountViewModel", "User has not liked this entry")
                    return@launch
                }

                db.runTransaction { transaction ->
                    val freshSnapshot = transaction.get(listRef)
                    if (!freshSnapshot.exists()) {
                        throw FirebaseFirestoreException(
                            "Entry does not exist",
                            FirebaseFirestoreException.Code.NOT_FOUND
                        )
                    }

                    val freshLikes = freshSnapshot.toObject(Entry::class.java)?.likesFrom
                        ?: emptyList()
                    val updatedLikes = freshLikes.filterNot { user -> user.id == userId }
                    transaction.update(listRef, "likesFrom", updatedLikes)
                }.addOnSuccessListener {
                    Log.i("AccountViewModel", "Like removed from entry successfully")
                }.addOnFailureListener { e ->
                    Log.e("AccountViewModel", "Failed to remove like from entry: ${e.message}")
                }
            } catch (e: Exception) {
                Log.e("AccountViewModel", "Failed to read entry: ${e.message}")
            }
        }
    }

    suspend fun setFavoriteDriver(driver: Driver): Boolean = withContext(Dispatchers.IO) {
        try {
            val user = auth.currentUser ?: return@withContext false
            val userDocRef = db.collection(Collections.USERS).document(user.uid)
            val driverData = mapOf(
                Fields.CODE to driver.code,
                Fields.DATE_OF_BIRTH to driver.dateOfBirth,
                Fields.DRIVER_ID to driver.driverId,
                Fields.FAMILY_NAME to driver.familyName,
                Fields.GIVEN_NAME to driver.givenName,
                Fields.NATIONALITY to driver.nationality,
                Fields.PERMANENT_NUMBER to driver.permanentNumber,
                Fields.URL to driver.url
            )
            userDocRef.update(Fields.FAV_DRIVER, driverData).await()
            Log.i("AccountViewModel", "Set favorite driver for user ${user.uid}: $driver")
            true
        } catch (e: Exception) {
            Log.e("AccountViewModel", "Error setting favorite driver: ${e.message}", e)
            false
        }
    }

    suspend fun setFavoriteTeam(team: Teams): Boolean = withContext(Dispatchers.IO) {
        try {
            val user = auth.currentUser ?: return@withContext false
            val userDocRef = db.collection(Collections.USERS).document(user.uid)
            userDocRef.update(Fields.FAV_TEAM, team).await()
            Log.i("AccountViewModel", "Set favorite team for user ${user.uid}: $team")
            true
        } catch (e: Exception) {
            Log.e("AccountViewModel", "Error setting favorite team: ${e.message}", e)
            false
        }
    }

    suspend fun setFavoriteCircuit(circuit: Circuit): Boolean = withContext(Dispatchers.IO) {
        try {
            val user = auth.currentUser ?: return@withContext false
            val userDocRef = db.collection(Collections.USERS).document(user.uid)
            val circuitData = mapOf(
                Fields.CIRCUIT_ID to circuit.circuitId,
                Fields.CIRCUIT_NAME to circuit.circuitName,
                Fields.URL to circuit.url
            )
            userDocRef.update(Fields.FAV_TRACK, circuitData).await()
            Log.i("AccountViewModel", "Set favorite circuit for user ${user.uid}: $circuit")
            true
        } catch (e: Exception) {
            Log.e("AccountViewModel", "Error setting favorite circuit: ${e.message}", e)
            false
        }
    }

    fun deleteEntry(entry: Entry) {
        entry.id?.let { entryId ->
            db.collection(Collections.ENTRIES)
                .document(entryId)
                .delete()
                .addOnSuccessListener {
                    Log.d("AccountViewModel", "Entry deleted: $entryId")
                    _userEntries.update { currentEntries ->
                        currentEntries?.filterNot { it.id == entryId }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("AccountViewModel", "Error deleting entry: $entryId", e)
                }
        } ?: run {
            Log.e("AccountViewModel", "Cannot delete entry: ID is null")
        }
    }

    fun changeListToNew(id: String?, newList: CustomList): Boolean {
        if (id == null) {
            Log.e("AccountViewModel", "Invalid input: listId=$id")
            return false
        }

        try {
            db.collection(Collections.CUSTOM_LISTS)
                .document(id)
                .update(
                    mapOf(
                        Fields.NAME to newList.name,
                        Fields.DESCRIPTION to newList.description,
                        Fields.PICTURE to newList.picture
                    )
                )
            Log.d("AccountViewModel", "Updated list $id")
            return true
        } catch (e: Exception) {
            Log.e("AccountViewModel", "Error updating list $id: ${e.message}", e)
            return false
        }
    }

    fun setListExpandedState(listId: String, isExpanded: Boolean) {
        _expandedStates.update { current ->
            current.toMutableMap().apply { this[listId] = isExpanded }
        }
        Log.d("AccountViewModel", "Set isExpanded=$isExpanded for list $listId")
    }

    fun getListExpandedState(listId: String): Boolean {
        return _expandedStates.value[listId] ?: false
    }
}