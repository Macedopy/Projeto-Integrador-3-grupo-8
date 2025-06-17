package com.example.geowarning.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    val retrofit: ApiService = Retrofit.Builder()
        .baseUrl("http://172.16.227.156:3000/api/") // Novo IP do computador
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)
}