package com.example.boxboxd.core.jolpica

import com.google.firebase.firestore.PropertyName

data class RaceResponse(
    val MRData: MRData
)

data class MRData(
    val RaceTable : RaceTable,
    val StatusTable : StatusTable
)

data class StatusTable (
    val Status : List<Status>,
)

data class Status (
    val statusId : Int,
    val count : Int,
    val status : String,
)

data class RaceTable(
    val season : Int,
    val Races: List<Race>
)

data class Race (
    @PropertyName("season") val season: Int = 0,
    @PropertyName("round") val round: Int = 0,
    @PropertyName("raceName") val raceName: String = "",
    @PropertyName("url") val url: String = "",
    @PropertyName("circuit") val Circuit: Circuit? = null,
    @PropertyName("date") val date: String = "",
    @PropertyName("time") val time: String = "",
    @PropertyName("Results") val Results: List<Result>? = null,
    @PropertyName("SprintResults") val sprintResults: List<SprintResult>? = null,
    @PropertyName("QualifyingResults") val qualifyingResults: List<QualifyingResult>? = null,
    @PropertyName("FirstPractice") val firstPractice: Event? = null,
    @PropertyName("SecondPractice") val secondPractice: Event? = null,
    @PropertyName("ThirdPractice") val thirdPractice: Event? = null,
    @PropertyName("Qualifying") val qualifying: Event? = null,
)


data class Event (
    val date: String,
    val time : String,
)

data class Result (
    val number : Int,
    val position : Int,
    val positionText : String,
    val points : Int,
    val Driver : Driver,
    val Constructor : Constructor,
    val grid : Int,
    val laps : Int,
    val status : String,
    val Time : Time,
    val FastestLap : FastestLap,
)

data class SprintResult (
    val number : Int,
    val position : Int,
    val positionText : String,
    val points : Int,
    val Driver : Driver,
    val Constructor : Constructor,
    val grid : Int,
    val laps : Int,
    val status : String,
    val Time : Time,
    val FastestLap : FastestLap,
)

data class QualifyingResult (
    val number : Int,
    val position : Int,
    val Driver : Driver,
    val Constructor : Constructor,
    val Q1 : String,
    val Q2 : String,
    val Q3 : String,
)


data class Driver (
    val driverId : String,
    val permanentNumber : Int,
    val code : String,
    val url : String,
    val givenName : String,
    val familyName : String,
    val dateOfBirth : String,
    val nationality : String,
)

data class Constructor (
    val constructorId : String,
    val url : String,
    val name : String,
    val nationality : String,
)

data class Time (
    val millis : Int,
    val time : String,
)

data class Circuit(
    val circuitId : String = "",
    val url : String  = "",
    val circuitName: String  = "",
    val Location: Location  = Location(),
)

data class Location(
    val lat : Double = 0.0,
    val long : Double = 0.0,
    val locality: String = "",
    val country: String = "",
)

data class FastestLap (
    val rank : Int,
    val lap : Int,
    val Time : Time,
    val AverageSpeed : AverageSpeed,
)

data class AverageSpeed (
    val units : String,
    val speed : Double,
)