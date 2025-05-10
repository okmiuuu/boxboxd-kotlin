package com.example.boxboxd.core.inner.objects

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.boxboxd.R
import com.example.boxboxd.core.inner.enums.Mood
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

    val tyreToReaction = mapOf(
        TyresGrades.WET to R.string.kinda_awful,
        TyresGrades.INTER to R.string.ngl_bad,
        TyresGrades.HARD to R.string.okay_ig,
        TyresGrades.MEDIUM to R.string.pretty_good,
        TyresGrades.SOFT to R.string.awesome_thing,
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

    val moodToString = mapOf(
        Mood.EXCITING to R.string.exciting,
        Mood.BORING to R.string.boring,
        Mood.CHAOTIC to R.string.chaotic,
        Mood.FRUSTRATING to R.string.frustrating,
        Mood.SATISFYING to R.string.satisfying,
        Mood.EMOTIONAL to R.string.emotional,
        Mood.CONTROVERSIAL to R.string.controversial,
        Mood.PREDICTABLE to R.string.predictable,
    )

    val stringToMood: Map<Int, Mood> = moodToString.entries.associate { (mood, string) ->
        string to mood
    }

    val moodToPicture = mapOf(
        Mood.EXCITING to R.drawable.exciting,
        Mood.BORING to R.drawable.boring,
        Mood.CHAOTIC to R.drawable.chaotic,
        Mood.FRUSTRATING to R.drawable.frustrating,
        Mood.SATISFYING to R.drawable.satisfying,
        Mood.EMOTIONAL to R.drawable.emotional,
        Mood.CONTROVERSIAL to R.drawable.controversial,
        Mood.PREDICTABLE to R.drawable.predictive,
    )

    val teamToPicture = mapOf(
        Teams.FERRARI to R.drawable.ferrari,
        Teams.MCLAREN to R.drawable.mclaren,
        Teams.REDBULL to R.drawable.redbull,
        Teams.ASTON_MARTIN to R.drawable.astonmartin,
        Teams.ALPINE to R.drawable.alpine,
        Teams.WILLIAMS to R.drawable.williams,
        Teams.HAAS to R.drawable.haas,
        Teams.SAUBER to R.drawable.kick_sauber,
        Teams.MERCEDES to R.drawable.mercedes,
        Teams.VCARB to R.drawable.vcarb,
    )

    val stringNameToTeam = mapOf(
        "scuderia ferrari hp" to Teams.FERRARI,
        "mclaren formula 1 team" to Teams.MCLAREN,
        "oracle red bull racing" to Teams.REDBULL,
        "mercedes amg petronas f1 team" to Teams.MERCEDES,
        "aston martin aramco f1 team" to Teams.ASTON_MARTIN,
        "bwt alpine f1 team" to Teams.ALPINE,
        "atlassian williams racing" to Teams.WILLIAMS,
        "moneygram haas f1 team" to Teams.HAAS,
        "stake f1 team kick sauber" to Teams.SAUBER,
        "visa cash app racing bulls f1 team" to Teams.VCARB
    )
}