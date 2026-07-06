package com.identityx.login.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.identityx.android.core.navigation.LoginActions
import com.identityx.login.domain.delegate.BiometricDelegateImpl
import com.identityx.login.domain.model.LoginUiState
import com.identityx.login.domain.model.LoginUiState.LoginStatus

@Composable
fun LoginScreen(
    actions: LoginActions,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Navigate on success
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is LoginViewModel.NavigationEvent.ToDashboard -> actions.onLoginSuccess()
            }
        }
    }

    // Launch biometric prompt when status transitions to BiometricPrompting
    LaunchedEffect(uiState.status) {
        if (uiState.status is LoginStatus.BiometricPrompting) {
            val activity = context as? FragmentActivity ?: return@LaunchedEffect
            val delegate = BiometricDelegateImpl(activity)
            delegate.launchBiometricPrompt(
                onSuccess = { viewModel.onIntent(LoginUiIntent.BiometricSuccess) },
                onError   = { viewModel.onIntent(LoginUiIntent.BiometricDismissed) }
            )
        }
    }

    LoginScreenContent(
        uiState = uiState,
        onEmailChange = { viewModel.onIntent(LoginUiIntent.EmailChanged(it)) },
        onPasswordChange = { viewModel.onIntent(LoginUiIntent.PasswordChanged(it)) },
        onRememberUsernameChange = { viewModel.onIntent(LoginUiIntent.RememberUsernameChanged(it)) },
        onBiometricEnabledChange = { viewModel.onIntent(LoginUiIntent.BiometricEnabledChanged(it)) },
        onSubmit = { viewModel.onIntent(LoginUiIntent.Submit) },
        onForgotPassword = actions.onNavigateToForgotPassword,
        onRegister = actions.onNavigateToRegistration,
        onSupport = actions.onNavigateToSupport
    )
}

@Composable
private fun LoginScreenContent(
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRememberUsernameChange: (Boolean) -> Unit,
    onBiometricEnabledChange: (Boolean) -> Unit,
    onSubmit: () -> Unit,
    onForgotPassword: () -> Unit,
    onRegister: () -> Unit,
    onSupport: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    var passwordVisible by remember { mutableStateOf(false) }

    val isInteractionEnabled = uiState.status !is LoginStatus.Loading &&
            uiState.status !is LoginStatus.BiometricPrompting

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Sign in to Identity X",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.email,
                onValueChange = onEmailChange,
                label = { Text("Email") },
                singleLine = true,
                enabled = isInteractionEnabled,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            OutlinedTextField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                label = { Text("Password") },
                singleLine = true,
                enabled = isInteractionEnabled,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        onSubmit()
                    }
                ),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible)
                                Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (passwordVisible)
                                "Hide password" else "Show password"
                        )
                    }
                }
            )

            // Remember username checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = uiState.rememberUsername,
                    onCheckedChange = onRememberUsernameChange,
                    enabled = isInteractionEnabled
                )
                Text(
                    text = "Remember username",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
            }

            // Enable biometric checkbox — only shown when Remember username is checked
            if (uiState.rememberUsername) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = uiState.biometricEnabled,
                        onCheckedChange = onBiometricEnabledChange,
                        enabled = isInteractionEnabled
                    )
                    Text(
                        text = "Enable biometric login",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Forgot password link
            Box(modifier = Modifier.fillMaxWidth()) {
                TextButton(
                    onClick = onForgotPassword,
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Text(
                        text = "Forgot password?",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            // Status feedback — exhausted when
            when (val status = uiState.status) {
                is LoginStatus.Error -> Text(
                    text = status.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
                is LoginStatus.BiometricPrompting -> Text(
                    text = "Waiting for biometric verification...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                is LoginStatus.Idle, is LoginStatus.Loading -> Unit
            }

            Button(
                onClick = onSubmit,
                enabled = isInteractionEnabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.status is LoginStatus.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Sign In")
                }
            }

            TextButton(onClick = onRegister, modifier = Modifier.fillMaxWidth()) {
                Text("Don't have an account? Create one")
            }

            TextButton(onClick = onSupport, modifier = Modifier.fillMaxWidth()) {
                Text("Contact support")
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Demo: test@identityx.com / password123",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    LoginScreenContent(
        uiState = LoginUiState(),
        onEmailChange = {}, onPasswordChange = {}, onSubmit = {},
        onRememberUsernameChange = {}, onBiometricEnabledChange = {},
        onForgotPassword = {}, onRegister = {}, onSupport = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenBiometricPreview() {
    LoginScreenContent(
        uiState = LoginUiState(
            email = "test@identityx.com",
            rememberUsername = true,
            biometricEnabled = true,
            status = LoginStatus.BiometricPrompting
        ),
        onEmailChange = {}, onPasswordChange = {}, onSubmit = {},
        onRememberUsernameChange = {}, onBiometricEnabledChange = {},
        onForgotPassword = {}, onRegister = {}, onSupport = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenErrorPreview() {
    LoginScreenContent(
        uiState = LoginUiState(
            status = LoginStatus.Error("Invalid email or password")
        ),
        onEmailChange = {}, onPasswordChange = {}, onSubmit = {},
        onRememberUsernameChange = {}, onBiometricEnabledChange = {},
        onForgotPassword = {}, onRegister = {}, onSupport = {}
    )
}
