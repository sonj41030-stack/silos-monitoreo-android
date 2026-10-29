package com.sonj.silosmonitoreo.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object SilosApiClient {

    private const val BASE_URL = "https://api.silosmonitoreo.com/"

    val service: SilosApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SilosApiService::class.java)
    }
}
