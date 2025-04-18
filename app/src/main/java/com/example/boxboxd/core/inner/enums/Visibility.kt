package com.example.boxboxd.core.inner.enums

enum class Visibility {
    PRIVATE,
    PUBLIC,
    FOR_FRIENDS;

    val isPrivate : Boolean
        get() = this == PRIVATE

    val isPublic : Boolean
        get() = this == PUBLIC

    val isForFriends : Boolean
        get() = this == FOR_FRIENDS
}
