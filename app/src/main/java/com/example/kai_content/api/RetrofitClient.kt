package com.example.kai_content.api

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://192.168.100.133:8000/" // Ganti dengan URL API Anda
//    private const val BASE_URL = "http://192.168.0.163:8000/"
//    private const val BASE_URL = "http://192.168.1.8:8000/"
//    private const val BASE_URL = "http://192.168.18.66:8000/"
//    private const val BASE_URL = "http://192.168.10.195:8000/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val response = chain.proceed(chain.request())
            Log.d("Interceptor", "Response Code: ${response.code}")
            response
        }
        .build()

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

}
