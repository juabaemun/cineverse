package com.example.cineversemobile.util

import android.content.Context

class AppConfig(context: Context) {
    private val prefs = context.getSharedPreferences("cineverse_prefs", Context.MODE_PRIVATE)

    fun saveApiUrl(url: String) {
        prefs.edit().putString("api_url", url).apply()
    }

    fun getApiUrl(): String {
        return prefs.getString("api_url", "") ?: ""
    }
}