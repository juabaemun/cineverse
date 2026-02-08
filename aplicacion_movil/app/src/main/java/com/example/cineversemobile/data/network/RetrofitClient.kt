package com.example.cineversemobile.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private var retrofit: Retrofit? = null
    private var lastUrl: String = ""

    fun getService(baseUrl: String): CineVerseApi {
        // Normalizamos la URL para que termine en /
        val formattedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"

        if (retrofit == null || lastUrl != formattedUrl) {
            lastUrl = formattedUrl
            retrofit = Retrofit.Builder()
                .baseUrl(formattedUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!.create(CineVerseApi::class.java)
    }
}