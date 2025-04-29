package com.example.boxboxd.core.jolpica

import com.google.firebase.firestore.PropertyName
import kotlinx.serialization.Serializable

@Serializable
data class RaceResponse(
    val MRData: MRData
)

@Serializable
data class MRData(
    val xmlns: String,
    val series: String,
    val url: String,
    val limit: String,
    val offset: String,
    val total: String,
    val RaceTable: RaceTable?, // Required field
    val StatusTable: StatusTable?, // Required field
    val DriverTable: DriverTable?, // Optional or not present
    val CircuitTable: CircuitTable? // Optional or not present
)

@Serializable
data class CircuitTable(
    val Circuits: List<Circuit>
)


@Serializable
data class DriverTable(
    val Drivers: List<Driver>
)

@Serializable
data class StatusTable (
    val Status : List<Status>,
)

@Serializable
data class Status (
    val statusId : Int,
    val count : Int,
    val status : String,
)

@Serializable
data class RaceTable(
    val season : Int,
    val Races: List<Race>
)

@Serializable
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

@Serializable
data class Event (
    val date: String,
    val time : String,
)

@Serializable
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

@Serializable
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

@Serializable
data class QualifyingResult (
    val number : Int,
    val position : Int,
    val Driver : Driver,
    val Constructor : Constructor,
    val Q1 : String,
    val Q2 : String,
    val Q3 : String,
)

@Serializable
data class Driver (
    val driverId : String? = null,
    val permanentNumber : Int? = null,
    val code : String? = null,
    val url : String? = null,
    val givenName : String? = null,
    val familyName : String? = null,
    val dateOfBirth : String? = null,
    val nationality : String? = null,
)

@Serializable
data class Constructor (
    val constructorId : String,
    val url : String,
    val name : String,
    val nationality : String,
)

@Serializable
data class Time (
    val millis : Int,
    val time : String,
)

@Serializable
data class Circuit(
    val circuitId : String = "",
    val url : String  = "",
    val circuitName: String  = "",
    val Location: Location  = Location(),
)

@Serializable
data class Location(
    val lat : Double = 0.0,
    val long : Double = 0.0,
    val locality: String = "",
    val country: String = "",
)

@Serializable
data class FastestLap (
    val rank : Int,
    val lap : Int,
    val Time : Time,
    val AverageSpeed : AverageSpeed,
)

@Serializable
data class AverageSpeed (
    val units : String,
    val speed : Double,
)