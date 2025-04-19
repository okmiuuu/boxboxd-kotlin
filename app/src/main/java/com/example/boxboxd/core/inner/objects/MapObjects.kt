package com.example.boxboxd.core.inner.objects

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.boxboxd.R
import com.example.boxboxd.core.inner.enums.StatTypes
import com.example.boxboxd.core.inner.enums.Teams
import com.example.boxboxd.core.inner.enums.TyresGrades

object MapObjects {
    val tyreToPicture = mapOf(
        TyresGrades.WET to R.drawable.tyre_wet,
        TyresGrades.INTER to R.drawable.tyre_inter,
        TyresGrades.HARD to R.drawable.tyre_hard,
        TyresGrades.MEDIUM to R.drawable.tyre_medium,
        TyresGrades.SOFT to R.drawable.tyre_soft,
    )

    val gradeToTyre = mapOf(
        1 to TyresGrades.WET,
        2 to TyresGrades.INTER,
        3 to TyresGrades.HARD,
        4 to TyresGrades.MEDIUM,
        5 to TyresGrades.SOFT
    )

    val tyreToGrade = mapOf(
        TyresGrades.WET to 1,
        TyresGrades.INTER to 2,
        TyresGrades.HARD to 3,
        TyresGrades.MEDIUM to 4,
        TyresGrades.SOFT to 5,
    )

    val typeStatToString = mapOf(
        StatTypes.ENTRIES to R.string.entries,
        StatTypes.LOGGED_RACES to R.string.logged_races,
        StatTypes.FOLLOWINGS to R.string.followings,
        StatTypes.FOLLOWERS to R.string.followers,
        StatTypes.LISTS to R.string.lists,

    )

    val teamToPicture = mapOf(
        Teams.FERRARI to R.drawable.ferrari,
        Teams.MCLAREN to R.drawable.mclaren,
        Teams.REDBULL to R.drawable.redbull,
    )
}