package com.example.boxboxd.core.api

import com.example.boxboxd.core.jolpica.RaceResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RaceApi {
    @GET("races.json")
    suspend fun getRaces(): RaceResponse

    @GET("{season}/races.json")
    suspend fun fetchRacesForSeason(
        @Path("season") season: Int
    ): RaceResponse

    @GET("{season}/circuits/{circuitId}/results.json")
    suspend fun fetchRaceForSeasonAndCircuit(
        @Path("season") season: Int,
        @Path("circuitId") circuitId: String
    ): RaceResponse

    @GET("{season}/drivers.json")
    suspend fun fetchDriversForSeason(
        @Path("season") season: Int,
    ): RaceResponse

    @GET("circuits.json")
    suspend fun fetchAllCircuits(@Query("offset") offset: Int = 0): RaceResponse
}