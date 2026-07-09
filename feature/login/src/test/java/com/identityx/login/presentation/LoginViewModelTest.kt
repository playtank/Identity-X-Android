package com.identityx.login.presentation

import app.cash.turbine.test
import com.identityx.login.bridge.LoginSessionPort
import com.identityx.login.domain.model.LoginUiState.LoginStatus
import com.identityx.login.domain.usecase.LoginAndFetchProfileUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val loginUseCase: LoginAndFetchProfileUseCase = mockk()
    private val sessionBridge: LoginSessionPort = mockk(relaxed = true)
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = LoginViewModel(loginUseCase, sessionBridge)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- Input field intents ---

    @Test
    fun `EmailChanged updates email and clears error`() = runTest {
        viewModel.onIntent(LoginUiIntent.EmailChanged("user@test.com"))
        assertEquals("user@test.com", viewModel.uiState.value.email)
        assertTrue(viewModel.uiState.value.status is LoginStatus.Idle)
    }

    @Test
    fun `PasswordChanged updates password and clears error`() = runTest {
        viewModel.onIntent(LoginUiIntent.PasswordChanged("secret"))
        assertEquals("secret", viewModel.uiState.value.password)
        assertTrue(viewModel.uiState.value.status is LoginStatus.Idle)
    }

    @Test
    fun `RememberUsernameChanged toggles rememberUsername`() = runTest {
        viewModel.onIntent(LoginUiIntent.RememberUsernameChanged(true))
        assertTrue(viewModel.uiState.value.rememberUsername)
        viewModel.onIntent(LoginUiIntent.RememberUsernameChanged(false))
        assertTrue(!viewModel.uiState.value.rememberUsername)
    }

    @Test
    fun `BiometricEnabledChanged toggles biometricEnabled`() = runTest {
        viewModel.onIntent(LoginUiIntent.BiometricEnabledChanged(true))
        assertTrue(viewModel.uiState.value.biometricEnabled)
    }

    // --- Validation ---

    @Test
    fun `Submit with blank email shows error`() = runTest {
        viewModel.onIntent(LoginUiIntent.PasswordChanged("password123"))
        viewModel.onIntent(LoginUiIntent.Submit)
        testDispatcher.scheduler.advanceUntilIdle()
        val status = viewModel.uiState.value.status
        assertTrue(status is LoginStatus.Error)
        assertEquals("Email and password are required", (status as LoginStatus.Error).message)
    }

    @Test
    fun `Submit with blank password shows error`() = runTest {
        viewModel.onIntent(LoginUiIntent.EmailChanged("user@test.com"))
        viewModel.onIntent(LoginUiIntent.Submit)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.status is LoginStatus.Error)
    }

    // --- Successful login flows ---

    @Test
    fun `Submit success without biometric emits ToDashboard`() = runTest {
        coEvery { loginUseCase(any(), any()) } returns Result.success(Unit)
        viewModel.onIntent(LoginUiIntent.EmailChanged("user@test.com"))
        viewModel.onIntent(LoginUiIntent.PasswordChanged("password123"))

        viewModel.navigationEvent.test {
            viewModel.onIntent(LoginUiIntent.Submit)
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals(LoginViewModel.NavigationEvent.ToDashboard, awaitItem())
        }
    }

    @Test
    fun `Submit success with both biometric checkboxes triggers BiometricPrompting`() = runTest {
        coEvery { loginUseCase(any(), any()) } returns Result.success(Unit)
        viewModel.onIntent(LoginUiIntent.EmailChanged("user@test.com"))
        viewModel.onIntent(LoginUiIntent.PasswordChanged("password123"))
        viewModel.onIntent(LoginUiIntent.RememberUsernameChanged(true))
        viewModel.onIntent(LoginUiIntent.BiometricEnabledChanged(true))
        viewModel.onIntent(LoginUiIntent.Submit)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.status is LoginStatus.BiometricPrompting)
    }

    @Test
    fun `BiometricSuccess after prompting emits ToDashboard`() = runTest {
        coEvery { loginUseCase(any(), any()) } returns Result.success(Unit)
        viewModel.onIntent(LoginUiIntent.EmailChanged("user@test.com"))
        viewModel.onIntent(LoginUiIntent.PasswordChanged("password123"))
        viewModel.onIntent(LoginUiIntent.RememberUsernameChanged(true))
        viewModel.onIntent(LoginUiIntent.BiometricEnabledChanged(true))
        viewModel.onIntent(LoginUiIntent.Submit)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.navigationEvent.test {
            viewModel.onIntent(LoginUiIntent.BiometricSuccess)
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals(LoginViewModel.NavigationEvent.ToDashboard, awaitItem())
        }
    }

    @Test
    fun `BiometricDismissed returns to Idle silently`() = runTest {
        viewModel.onIntent(LoginUiIntent.BiometricDismissed)
        assertTrue(viewModel.uiState.value.status is LoginStatus.Idle)
    }

    // --- Failed login ---

    @Test
    fun `Submit failure shows Error status with message`() = runTest {
        coEvery { loginUseCase(any(), any()) } returns Result.failure(Exception("Invalid credentials"))
        viewModel.onIntent(LoginUiIntent.EmailChanged("user@test.com"))
        viewModel.onIntent(LoginUiIntent.PasswordChanged("wrong"))
        viewModel.onIntent(LoginUiIntent.Submit)
        testDispatcher.scheduler.advanceUntilIdle()
        val status = viewModel.uiState.value.status
        assertTrue(status is LoginStatus.Error)
        assertEquals("Invalid credentials", (status as LoginStatus.Error).message)
    }

    @Test
    fun `Submit while Loading is ignored`() = runTest {
        // Put viewModel into Loading by submitting once
        coEvery { loginUseCase(any(), any()) } coAnswers {
            kotlinx.coroutines.delay(5_000)
            Result.success(Unit)
        }
        viewModel.onIntent(LoginUiIntent.EmailChanged("user@test.com"))
        viewModel.onIntent(LoginUiIntent.PasswordChanged("password"))
        viewModel.onIntent(LoginUiIntent.Submit)
        testDispatcher.scheduler.advanceTimeBy(100)
        assertTrue(viewModel.uiState.value.status is LoginStatus.Loading)

        // Second submit while loading — use case should only be called once
        viewModel.onIntent(LoginUiIntent.Submit)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify(exactly = 1) { loginUseCase(any(), any()) }
    }
}
