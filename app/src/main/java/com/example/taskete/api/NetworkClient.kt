package com.example.taskete.api

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object NetworkClient {
    private const val BASE_URL = "https://demo7748154.mockable.io/"
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    //APIs
    val usersApi = retrofit.create(UsersAPI::class.java)
}