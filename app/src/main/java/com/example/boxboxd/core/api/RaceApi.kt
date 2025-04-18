package com.example.boxboxd.core.api

import com.example.boxboxd.core.jolpica.RaceResponse
import retrofit2.http.GET
import retrofit2.http.Path

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
}