package com.ecommerce.asdtechcreationadmin.session

import android.content.Context

class SessionManager(context: Context) {

    private val pref =
        context.getSharedPreferences("ASD_ADMIN", Context.MODE_PRIVATE)

    companion object {

        private const val TOKEN = "TOKEN"
        private const val NAME = "NAME"
        private const val EMAIL = "EMAIL"

    }

    fun saveSession(
        token: String,
        name: String,
        email: String
    ) {

        pref.edit()
            .putString(TOKEN, token)
            .putString(NAME, name)
            .putString(EMAIL, email)
            .apply()

    }

    fun getToken(): String? {

        return pref.getString(TOKEN, null)

    }

    fun getName(): String {

        return pref.getString(NAME, "") ?: ""

    }

    fun getEmail(): String {

        return pref.getString(EMAIL, "") ?: ""

    }

    fun isLoggedIn(): Boolean {

        return getToken() != null

    }

    fun logout() {

        pref.edit().clear().apply()

    }

}