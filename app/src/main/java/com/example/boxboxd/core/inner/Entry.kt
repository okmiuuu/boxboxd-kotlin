package com.example.boxboxd.core.inner

import com.example.boxboxd.core.inner.enums.Mood
import com.example.boxboxd.core.inner.enums.TyresGrades
import com.example.boxboxd.core.inner.enums.Visibility
import com.example.boxboxd.core.jolpica.Race
import com.google.firebase.Timestamp

data class Entry (
    val id : String? = null,
    val race : Race? = null,
    val mood : Mood? = null,
    val userId: String? = null,
    val rating : Int? = null,
    val user: User? = null,
    val text : String? = null,
    val visibility : Visibility? = null,
    val likesFrom : List<User>? = null,
    val createdAt : Timestamp? = Timestamp.now(),
    val updatedAt : Timestamp? = Timestamp.now(),
)