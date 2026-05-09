package com.efrei.nanoorbit.data.api

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8080/api/"

    // Adaptateur Gson pour LocalDateTime
    private val gson = GsonBuilder()
        .registerTypeAdapter(
            LocalDateTime::class.java,
            JsonDeserializer { json, _, _ ->
                LocalDateTime.parse(json.asString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            }
        )
        .create()

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
        )
        .build()

    val api: NanoOrbitApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(NanoOrbitApi::class.java)
    }
}
