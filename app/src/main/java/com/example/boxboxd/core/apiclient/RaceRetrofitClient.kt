package com.example.boxboxd.core.apiclient

import com.example.boxboxd.core.api.RaceApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RaceRetrofitClient {
    private const val BASE_URL = "https://api.jolpi.ca/ergast/f1/"

    val instance: RaceApi by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(RaceApi::class.java)
    }
}