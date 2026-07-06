package com.identityx.login.data

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MockLoginDataSourceTest {

    private val dataSource = MockLoginDataSource()

    @Test
    fun `login succeeds with correct mock credentials`() = runTest {
        val result = dataSource.login(MockLoginDataSource.MOCK_EMAIL, MockLoginDataSource.MOCK_PASSWORD)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `login fails with wrong email`() = runTest {
        val result = dataSource.login("wrong@email.com", MockLoginDataSource.MOCK_PASSWORD)
        assertTrue(result.isFailure)
        assertEquals("Invalid email or password", result.exceptionOrNull()?.message)
    }

    @Test
    fun `login fails with wrong password`() = runTest {
        val result = dataSource.login(MockLoginDataSource.MOCK_EMAIL, "wrongpassword")
        assertTrue(result.isFailure)
    }

    @Test
    fun `login fails with empty credentials`() = runTest {
        val result = dataSource.login("", "")
        assertTrue(result.isFailure)
    }
}
