package com.demo.weatherapp.data.repository

import android.util.Patterns
import com.demo.weatherapp.data.local.dao.UserDao
import com.demo.weatherapp.data.local.entity.UserEntity
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val userDao: UserDao,
    private val sessionStore: AuthSessionStore
) {
    private var currentUserEmail: String? = sessionStore.savedEmail()
    private var currentUserDisplayName: String? = sessionStore.savedDisplayName()

    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")

    fun isLoggedIn(): Boolean = currentUserEmail != null

    fun currentUserName(): String? = currentUserDisplayName

    suspend fun signUp(displayName: String, email: String, password: String): Result<Unit> {
        val normalizedEmail = email.trim().lowercase()
        val trimmedName = displayName.trim()

        if (trimmedName.isBlank()) {
            return Result.failure(
                IllegalArgumentException("Display name is required.")
            )
        }

        if (!emailRegex.matches(normalizedEmail)) {
            return Result.failure(
                IllegalArgumentException("Please enter a valid email address.")
            )
        }

        if (password.length < 6) {
            return Result.failure(
                IllegalArgumentException("Password must be at least 6 characters.")
            )
        }

        if (userDao.findByEmail(normalizedEmail) != null) {
            return Result.failure(
                IllegalArgumentException("This email is already registered.")
            )
        }

        if (userDao.findByEmail(normalizedEmail) != null) {
            return Result.failure(IllegalArgumentException("This email is already registered."))
        }

        // Insert into local db
        userDao.insert(
            UserEntity(
                email = normalizedEmail,
                displayName = trimmedName,
                passwordHash = password.sha256(),
                createdAtMillis = System.currentTimeMillis()
            )
        )

        currentUserEmail = normalizedEmail
        currentUserDisplayName = trimmedName

        sessionStore.save(normalizedEmail, trimmedName)

        return Result.success(Unit)
    }

    suspend fun login(email: String, password: String): Result<Unit> {
        val normalizedEmail = email.trim().lowercase()
        val user = userDao.findByEmail(normalizedEmail)
        return if (user != null && user.passwordHash == password.sha256()) {
            currentUserEmail = normalizedEmail
            currentUserDisplayName = user.displayName

            // save to prefs
            sessionStore.save(normalizedEmail, user.displayName)

            Result.success(Unit)
        } else {
            Result.failure(IllegalArgumentException("Email or password is incorrect."))
        }
    }

    fun logout() {
        currentUserEmail = null
        currentUserDisplayName = null

        // remove from prefs
        sessionStore.clear()
    }
}

private fun String.sha256(): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(toByteArray())
    return bytes.joinToString(separator = "") { "%02x".format(it) }
}