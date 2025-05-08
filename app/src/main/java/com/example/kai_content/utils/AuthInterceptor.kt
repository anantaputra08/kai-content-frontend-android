package com.example.kai_content.utils

import android.content.Context
import android.content.SharedPreferences
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = SessionManager.getToken()

        val request = if (token != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Accept", "application/json")
                .build()
        } else {
            chain.request().newBuilder()
                .addHeader("Accept", "application/json")
                .build()
        }

        return chain.proceed(request)
    }
}

object SessionManager {
    private const val PREFS_NAME = "KaiContentPrefs"
    private const val TOKEN_KEY = "auth_token"

    private var sharedPreferences: SharedPreferences? = null

    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveToken(token: String) {
        sharedPreferences?.edit()?.putString(TOKEN_KEY, token)?.apply()
    }

    fun getToken(): String? {
        return sharedPreferences?.getString(TOKEN_KEY, null)
    }

    fun clearToken() {
        sharedPreferences?.edit()?.remove(TOKEN_KEY)?.apply()
    }
}