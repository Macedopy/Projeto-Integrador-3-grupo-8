package com.example.geowarning.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private const val BASE_URL = "http://172.16.227.156:3000/api/" // Novo IP do computador

    // Tempos limite em segundos
    private const val CONNECT_TIMEOUT: Long = 30 // Tempo limite para estabelecer uma conexão
    private const val READ_TIMEOUT: Long = 30    // Tempo limite para ler dados do servidor
    private const val WRITE_TIMEOUT: Long = 30   // Tempo limite para enviar dados para o servidor

    val retrofit: ApiService by lazy {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // Adiciona o OkHttpClient configurado
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}