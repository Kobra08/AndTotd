package com.example.taskete.api

import com.example.taskete.data.User
import com.example.taskete.data.UserResponse
import io.reactivex.rxjava3.core.Single
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface UsersAPI {
    @GET("users/trial")
    fun getTrialUser(): Call<UserResponse>
}