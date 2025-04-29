package com.example.boxboxd.core.inner

import com.example.boxboxd.core.inner.enums.StatTypes
import com.example.boxboxd.core.inner.enums.Teams
import com.example.boxboxd.core.inner.enums.TyresGrades
import com.example.boxboxd.core.jolpica.Circuit
import com.example.boxboxd.core.jolpica.Constructor
import com.example.boxboxd.core.jolpica.Driver
import com.example.boxboxd.core.jolpica.Race
import com.google.firebase.Timestamp


class User(
    val firstLogin : Boolean? = null,
    val id : String? = null,
    val username : String? = null,
    val picture : String? = null,
    val email : String? = null,
    val createdAt : Timestamp? = Timestamp.now(),
    val updatedAt : Timestamp? = Timestamp.now(),
    val entries : List<Entry>? = mutableListOf(),
    val followings : List<User>? = mutableListOf(),
    val followers : List<User>? = mutableListOf(),
    val favDriver : Driver? = null,
    val favCircuit : Circuit? = null,
    val favTeam : Teams? = null,
)