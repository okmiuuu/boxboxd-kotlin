package com.example.boxboxd.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.room.util.copy
import com.example.boxboxd.LoginActivity
import com.example.boxboxd.MainActivity
import com.example.boxboxd.core.inner.CustomList
import com.example.boxboxd.core.inner.Entry
import com.example.boxboxd.core.inner.User
import com.example.boxboxd.core.inner.enums.StatTypes
import com.example.boxboxd.core.inner.objects.Collections
import com.example.boxboxd.core.inner.objects.Fields
import com.example.boxboxd.core.inner.objects.Routes
import com.example.boxboxd.core.jolpica.Circuit
import com.example.boxboxd.core.jolpica.Constructor
import com.example.boxboxd.core.jolpica.Driver
import com.example.boxboxd.core.jolpica.Race
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.firestore
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLEncoder


class AccountViewModel(private val navController: NavController) : ViewModel() {
    private val db: FirebaseFirestore = Firebase.firestore
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser

    val userId = auth.currentUser?.uid
    private val userDocRef = userId?.let { db.collection(Collections.USERS).document(it) }

    private val _userPhotoUrl = MutableStateFlow<Uri?>(null)
    val userPhotoUrl: StateFlow<Uri?> get() = _userPhotoUrl

    private val _userDisplayName = MutableStateFlow<String?>(null)
    val userDisplayName: StateFlow<String?> get() = _userDisplayName

    private val _userEmail = MutableStateFlow<String?>(null)
    val userEmail: StateFlow<String?> get() = _userEmail

    private val _userFavDriver = MutableStateFlow<Driver?>(null)
    val userFavDriver: StateFlow<Driver?> get() = _userFavDriver

    private val _userFavTeam = MutableStateFlow<Constructor?>(null)
    val userFavTeam: StateFlow<Constructor?> get() = _userFavTeam

    private val _userFavCircuit = MutableStateFlow<Circuit?>(null)
    val userFavCircuit: StateFlow<Circuit?> get() = _userFavCircuit

    private val _userEntries = MutableStateFlow<List<Entry>?>(null)
    val userEntries: StateFlow<List<Entry>?> get() = _userEntries

    private val _userLists = MutableStateFlow<List<CustomList>?>(null)
    val userLists: StateFlow<List<CustomList>?> get() = _userLists

    private val _userObject = MutableStateFlow(User())
    val userObject: StateFlow<User> = _userObject.asStateFlow()


    init {
        fetchUserData()
    }

    fun navigateToUserScreen(user : User) {
        val userJson = Gson().toJson(user)
        val encodedUserJson = URLEncoder.encode(userJson, "UTF-8")
        navController.navigate("${Routes.USER_SCREEN}/$encodedUserJson")
    }

    fun navigateToRaceScreen(race : Race) {
        val raceJson = Gson().toJson(race)
        val encodedRaceJson = URLEncoder.encode(raceJson, "UTF-8")
        navController.navigate("${Routes.RACE_SCREEN}/$encodedRaceJson")
    }

    fun navigateToListsScreen() {
        navController.navigate(Routes.LISTS_SCREEN)
    }

    fun navigateBack() {
        navController.popBackStack()
    }

    private fun fetchUserData() {
        fetchUser()

        userDocRef?.addSnapshotListener { documentSnapshot, e ->
            if (e != null) {
                Log.w("Firestore", "Listen failed.", e)
                return@addSnapshotListener
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                val user = documentSnapshot.toObject(User::class.java)

                if (user == null) {
                    Log.i("FETCH USER DATA", "user null")
                } else {
                    Log.i("FETCH USER DATA", user.id.toString())
                }

                getUserEntries(user ?: User())
                getAllUserLists(user ?: User())

                _userFavDriver.value = user?.favDriver
                _userFavTeam.value = user?.favTeam
                _userFavCircuit.value = user?.favCircuit

                _userPhotoUrl.value = auth.currentUser?.photoUrl
                _userDisplayName.value = auth.currentUser?.displayName
                _userEmail.value = auth.currentUser?.email

            } else {
                Log.w("Firestore", "User document does not exist")
            }
        }
    }

    fun createList(
        customList: CustomList
    ) {
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
        val db = FirebaseFirestore.getInstance()
        val listId = list.id ?: return
        val listRef = db.collection(Collections.CUSTOM_LISTS).document(listId)

        listRef.get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    Log.e("AccountViewModel", "List does not exist")
                    return@addOnSuccessListener
                }

                val currentListItems = snapshot.toObject(CustomList::class.java)?.listItems ?: emptyList()

                val isDuplicate = currentListItems.any { existingRace ->
                    existingRace.season == race.season && existingRace.round == race.round
                }

                if (isDuplicate) {
                    Log.i("AccountViewModel", "Duplicate race detected")
                    onDuplicate()
                    return@addOnSuccessListener
                }

                db.runTransaction { transaction ->
                    val freshSnapshot = transaction.get(listRef)
                    if (!freshSnapshot.exists()) {
                        throw FirebaseFirestoreException(
                            "List does not exist",
                            FirebaseFirestoreException.Code.NOT_FOUND
                        )
                    }

                    val freshListItems = freshSnapshot.toObject(CustomList::class.java)?.listItems ?: emptyList()
                    val updatedListItems = freshListItems + race
                    transaction.update(listRef, "listItems", updatedListItems)
                }
                    .addOnSuccessListener {
                        Log.i("AccountViewModel", "Race added to list successfully")
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        Log.e("AccountViewModel", "Failed to add race to list: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                Log.e("AccountViewModel", "Failed to read list: ${e.message}")
            }
    }

    fun deleteRaceFromList() {

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

    fun editListPicture(customList: CustomList) {

    }

    fun editListName(customList: CustomList) {

    }

    fun editListDescription(customList: CustomList) {

    }

    fun getUserStat(statType: StatTypes, user : User) : Int {
        getUserEntries(user)
        getAllUserLists(user)

        val typeToStat = mapOf(
            StatTypes.ENTRIES to _userEntries.value?.size,
            StatTypes.LISTS to _userLists.value?.size,
        )

        return typeToStat[statType] ?: 0
    }

    private fun getAllUserLists(user : User)  {
        val query = db.collection(Collections.CUSTOM_LISTS)
            .whereEqualTo("user.id", user.id)

        query.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e("AccountViewModel", "Error fetching lists", e)
                _userLists.value = emptyList()
                return@addSnapshotListener
            }

            if (snapshot != null) {
                Log.i("AccountViewModel", "Snapshot documents: ${snapshot.documents.size}")
                snapshot.documents.forEach { doc ->
                    Log.d("AccountViewModel", "Raw document: ${doc.data}")
                }

                val lists = try {
                    snapshot.toObjects(CustomList::class.java)
                } catch (ex: Exception) {
                    Log.e("AccountViewModel", "Serialization error", ex)
                    emptyList()
                }
                Log.i("AccountViewModel", "Fetched ${lists.size} lists")
                _userLists.value = lists
            } else {
                Log.w("AccountViewModel", "Snapshot is null")
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
            e.printStackTrace()
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
                Log.e("AccountViewModel", "Error logging race", e)
            }
    }

//    fun logRace(entry: Entry,
//                onComplete: (String) -> Unit =
//                    { documentId ->
//                        Log.i("Test", documentId)
//                    }
//    ) {
//
//        val userId = this.userId
//
//        if (userId != null) {
//
//            val userDocRef = db.collection(Collections.USERS).document(userId)
//            userDocRef.get().addOnSuccessListener { userDocument ->
//                if (userDocument.exists()) {
//
//                    db.collection(Collections.ENTRIES)
//                        .document(userId)
//                        .collection(Fields.ENTRY)
//                        .add(entry)
//                        .addOnSuccessListener { document ->
//                            val updatedEntry = entry.copy(id = document.id)
//                            db.collection(Collections.ENTRIES)
//                                .document(userId)
//                                .collection(Fields.ENTRY)
//                                .document(document.id)
//                                .set(updatedEntry)
//                                .addOnSuccessListener {
//                                    onComplete(document.id)
//                                }
//                                .addOnFailureListener { e ->
//                                    Log.w("Firestore", "Error updating transaction", e)
//                                }
//                        }
//                        .addOnFailureListener { e ->
//                            Log.w("Firestore", "Error adding transaction", e)
//                        }
//
//                }
//            }.addOnFailureListener { e ->
//                Log.w("Firestore", "Error getting user document", e)
//            }
//
//        }
//
//    }

    fun followUser(user : User) {

    }

    fun goToSettings(user : User) {

    }

    fun logOut(context: Context) {
        auth.signOut()
        (context as MainActivity).finish()
        val i = Intent(context, LoginActivity::class.java)
        context.startActivity(i)
    }

    fun checkIfThatIsYourPage(user : User) : Boolean {
        return user.id == this.userId
    }

    fun fetchUser() {
        viewModelScope.launch {
            val user = withContext(Dispatchers.IO) {
                getCurrentUser()
            }
            _userObject.value = user
            Log.i("User", user.toString())
        }
    }

    fun getCurrentUser(): User {

        if (currentUser == null) {
            return User()
        }

        val userId = currentUser.uid

        Log.i("current User", userId)

        val userDocRef = db.collection(Collections.USERS).document(userId)

        return try {
            val documentSnapshot = Tasks.await(userDocRef.get())
            if (documentSnapshot.exists()) {
                Log.i("TRY", "yes document")
                documentSnapshot.toObject(User::class.java) ?: User()
            } else {
                Log.i("TRY", "no document")
                User()
            }
        } catch (e: Exception) {
            Log.i("CATCH", e.message.toString())
            User()
        }
    }

    fun getUserEntries(user: User) {
        val query = db.collection(Collections.ENTRIES)
            .whereEqualTo("user.id", user.id)

        query.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e("AccountViewModel", "Error fetching entries", e)
                _userEntries.value = emptyList()
                return@addSnapshotListener
            }

            if (snapshot != null) {
                Log.i("AccountViewModel", "Snapshot documents: ${snapshot.documents.size}")
                val entries = try {
                    snapshot.toObjects(Entry::class.java)
                } catch (ex: Exception) {
                    Log.e("AccountViewModel", "Serialization error", ex)
                    emptyList()
                }
                Log.i("AccountViewModel", "Fetched ${entries.size} entries")
                _userEntries.value = entries
            } else {
                Log.w("AccountViewModel", "Snapshot is null")
                _userEntries.value = emptyList()
            }
        }
    }

    fun getGradeStatsForUser() : Map<Int, Int> {

        val userEntries = _userEntries.value ?: emptyList()

        Log.i("USER ENTRIES COUNT", userEntries.size.toString())

        val gradeToCountMap = mutableMapOf(
            0 to 0,
            1 to 0,
            2 to 0,
            3 to 0,
            4 to 0,
            5 to 0,
        ) // grade in (1..5) and count of those grades

        userEntries.forEach { entry ->
            val currentCount = gradeToCountMap[entry.rating] ?: 0
            gradeToCountMap[entry.rating?: 0] = currentCount + 1
        } // going through all of the entries and counting quantity for each grade

        return gradeToCountMap;
    }

    fun addLikeToEntry(entry : Entry) {
        val db = FirebaseFirestore.getInstance()
        val entryId = entry.id ?: return
        val listRef = db.collection(Collections.ENTRIES).document(entryId)

        Log.i("add like to entry", entryId)

        listRef.get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    Log.e("AccountViewModel", "Entry does not exist")
                    return@addOnSuccessListener
                }

                val currentLikes = snapshot.toObject(Entry::class.java)?.likesFrom ?: emptyList()

                val isDuplicate = currentLikes.any { user ->
                    user.id == userId
                }

                if (isDuplicate) {
                    Log.i("AccountViewModel", "Duplicate like detected")
                    return@addOnSuccessListener
                }

                db.runTransaction { transaction ->
                    val freshSnapshot = transaction.get(listRef)
                    if (!freshSnapshot.exists()) {
                        throw FirebaseFirestoreException(
                            "Entry does not exist",
                            FirebaseFirestoreException.Code.NOT_FOUND
                        )
                    }

                    val freshLikes = freshSnapshot.toObject(Entry::class.java)?.likesFrom ?: emptyList()
                    val updatedLikes = freshLikes + userObject.value
                    transaction.update(listRef, "likesFrom", updatedLikes)
                }
                    .addOnSuccessListener {
                        Log.i("AccountViewModel", "Like added to entry successfully")
                    }
                    .addOnFailureListener { e ->
                        Log.e("AccountViewModel", "Failed to add like to entry: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                Log.e("AccountViewModel", "Failed to read entry: ${e.message}")
            }
    }

    fun removeLikeFromEntry(entry: Entry) {
        val db = FirebaseFirestore.getInstance()
        val entryId = entry.id ?: return
        val listRef = db.collection(Collections.ENTRIES).document(entryId)

        listRef.get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    Log.e("AccountViewModel", "Entry does not exist")
                    return@addOnSuccessListener
                }

                val currentLikes = snapshot.toObject(Entry::class.java)?.likesFrom ?: emptyList()

                val userHasLiked = currentLikes.any { user ->
                    user.id == userId
                }

                if (!userHasLiked) {
                    Log.i("AccountViewModel", "User has not liked this entry")
                    return@addOnSuccessListener
                }

                db.runTransaction { transaction ->
                    val freshSnapshot = transaction.get(listRef)
                    if (!freshSnapshot.exists()) {
                        throw FirebaseFirestoreException(
                            "Entry does not exist",
                            FirebaseFirestoreException.Code.NOT_FOUND
                        )
                    }

                    val freshLikes = freshSnapshot.toObject(Entry::class.java)?.likesFrom ?: emptyList()
                    val updatedLikes = freshLikes.filterNot { user ->
                        user.id == userId
                    }
                    transaction.update(listRef, "likesFrom", updatedLikes)
                }
                    .addOnSuccessListener {
                        Log.i("AccountViewModel", "Like removed from entry successfully")
                    }
                    .addOnFailureListener { e ->
                        Log.e("AccountViewModel", "Failed to remove like from entry: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                Log.e("AccountViewModel", "Failed to read entry: ${e.message}")
            }
    }
}