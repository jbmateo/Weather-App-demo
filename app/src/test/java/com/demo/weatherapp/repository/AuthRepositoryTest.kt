package com.demo.weatherapp.repository

import com.demo.weatherapp.data.local.dao.UserDao
import com.demo.weatherapp.data.local.entity.UserEntity
import com.demo.weatherapp.data.repository.AuthRepository
import com.demo.weatherapp.data.repository.AuthSessionStore
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AuthRepositoryTest {
    @Test
    fun signupCreatesLoggedInUser() = runTest {
        val repository = AuthRepository(FakeUserDao(), FakeAuthSessionStore())

        val result = repository.signUp("Demo User", "demo@example.com", "password")

        assertThat(result.isSuccess).isTrue()
        assertThat(repository.isLoggedIn()).isTrue()
        assertThat(repository.currentUserName()).isEqualTo("Demo User")
    }

    @Test
    fun signupRejectsEmail() = runTest {
        val repository = AuthRepository(FakeUserDao(), FakeAuthSessionStore())

        val result = repository.signUp("Demo User", "fail_email.com", "password")

        assertThat(result.isFailure).isTrue()
        assertThat(repository.isLoggedIn()).isFalse()
    }

    @Test
    fun loginRejectsWrongPassword() = runTest {
        val repository = AuthRepository(FakeUserDao(), FakeAuthSessionStore())
        repository.signUp("Demo User", "demo@example.com", "password")
        repository.logout()

        val result = repository.login("demo@example.com", "bad-password")

        assertThat(result.isFailure).isTrue()
        assertThat(repository.isLoggedIn()).isFalse()
    }

    @Test
    fun savedSessionRestoresLoggedInUser() {
        val sessionStore = FakeAuthSessionStore().apply {
            save("demo@example.com", "Demo User")
        }
        val repository = AuthRepository(FakeUserDao(), sessionStore)

        assertThat(repository.isLoggedIn()).isTrue()
        assertThat(repository.currentUserName()).isEqualTo("Demo User")
    }
}

private class FakeUserDao : UserDao {
    private val users = mutableMapOf<String, UserEntity>()

    override suspend fun insert(user: UserEntity) {
        if (users.containsKey(user.email)) error("duplicate")
        users[user.email] = user
    }

    override suspend fun findByEmail(email: String): UserEntity? = users[email]
}

private class FakeAuthSessionStore : AuthSessionStore {
    private var email: String? = null
    private var displayName: String? = null

    override fun savedEmail(): String? = email

    override fun savedDisplayName(): String? = displayName

    override fun save(email: String, displayName: String) {
        this.email = email
        this.displayName = displayName
    }

    override fun clear() {
        email = null
        displayName = null
    }
}
