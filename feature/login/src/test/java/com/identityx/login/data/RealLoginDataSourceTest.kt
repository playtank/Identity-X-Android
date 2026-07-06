package com.identityx.login.data

import com.identityx.android.core.network.model.AuthRequest
import com.identityx.android.core.network.model.AuthResponse
import com.identityx.android.core.network.restful.IdentityXApiClient
import com.identityx.local.domain.TokenProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RealLoginDataSourceTest {

    private val apiClient: IdentityXApiClient = mockk()
    private val tokenProvider: TokenProvider = mockk(relaxed = true)
    private val dataSource = RealLoginDataSource(apiClient, tokenProvider)

    @Test
    fun `login success saves tokens and returns success`() = runTest {
        val fakeResponse = AuthResponse(
            accessToken  = "access_abc",
            refreshToken = "refresh_xyz"
        )
        coEvery { apiClient.login(any()) } returns fakeResponse

        val result = dataSource.login("user@test.com", "password123")

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { tokenProvider.saveTokens("access_abc", "refresh_xyz") }
    }

    @Test
    fun `login sends correct email and password to API`() = runTest {
        coEvery { apiClient.login(any()) } returns AuthResponse("a", "r")

        dataSource.login("test@identityx.com", "mypassword")

        coVerify {
            apiClient.login(AuthRequest(email = "test@identityx.com", password = "mypassword"))
        }
    }

    @Test
    fun `login API failure returns failure and does not save tokens`() = runTest {
        coEvery { apiClient.login(any()) } throws Exception("Network error")

        val result = dataSource.login("user@test.com", "password")

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { tokenProvider.saveTokens(any(), any()) }
    }
}
