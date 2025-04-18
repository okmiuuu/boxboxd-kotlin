package com.example.boxboxd.core.inner

import com.example.boxboxd.core.inner.enums.Visibility
import com.example.boxboxd.core.jolpica.Race

class CustomList (
    val id : String? = null,
    val listItems : List<Race>? = null,
    val user : User? = null,
    val name : String? = null,
    val description : String? = null,
    val visibility : Visibility? = null,
    val picture : String? = null
)