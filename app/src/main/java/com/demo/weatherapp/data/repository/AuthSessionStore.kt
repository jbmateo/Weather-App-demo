package com.demo.weatherapp.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

interface AuthSessionStore {
    fun savedEmail(): String?
    fun savedDisplayName(): String?
    fun save(email: String, displayName: String)
    fun clear()
}

@Singleton
class SharedPreferencesAuthSessionStore @Inject constructor(
    @ApplicationContext context: Context
) : AuthSessionStore {
    private val preferences = context.getSharedPreferences("auth_session", Context.MODE_PRIVATE)

    override fun savedEmail(): String? = preferences.getString(KEY_EMAIL, null)

    override fun savedDisplayName(): String? = preferences.getString(KEY_DISPLAY_NAME, null)

    // basic logged in state
    override fun save(email: String, displayName: String) {
        preferences.edit {
            putString(KEY_EMAIL, email)
                .putString(KEY_DISPLAY_NAME, displayName)
        }
    }

    override fun clear() {
        preferences.edit { clear() }
    }

    private companion object {
        const val KEY_EMAIL = "email"
        const val KEY_DISPLAY_NAME = "display_name"
    }
}