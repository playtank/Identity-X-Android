package com.identityx.login.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.identityx.android.core.navigation.LoginActions
import com.identityx.login.domain.model.LoginUiState
import com.identityx.login.domain.model.LoginUiState.LoginStatus
import com.identityx.login.presentation.LoginUiIntent
import com.identityx.login.presentation.LoginViewModel

@Composable
fun LoginScreen(
    actions: LoginActions,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is LoginViewModel.NavigationEvent.ToDashboard -> actions.onLoginSuccess()
            }
        }
    }

    LoginScreenContent(
        uiState = uiState,
        onEmailChange = { viewModel.onIntent(LoginUiIntent.EmailChanged(it)) },
        onPasswordChange = { viewModel.onIntent(LoginUiIntent.PasswordChanged(it)) },
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
    onSubmit: () -> Unit,
    onForgotPassword: () -> Unit,
    onRegister: () -> Unit,
    onSupport: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    var passwordVisible by remember { mutableStateOf(false) }
    val isLoading = uiState.status is LoginStatus.Loading

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
                enabled = !isLoading,
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
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
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
                                Icons.Filled.VisibilityOff
                            else
                                Icons.Filled.Visibility,
                            contentDescription = if (passwordVisible)
                                "Hide password"
                            else
                                "Show password"
                        )
                    }
                }
            )

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

            // Error message — only shown in Error status
            when (val status = uiState.status) {
                is LoginStatus.Error -> Text(
                    text = status.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
                is LoginStatus.Idle, is LoginStatus.Loading -> Unit
            }

            Button(
                onClick = onSubmit,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Sign In")
                }
            }

            // Register link
            TextButton(
                onClick = onRegister,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Don't have an account? Create one")
            }

            // Support link
            TextButton(
                onClick = onSupport,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Contact support")
            }

            // Demo hint
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
        onEmailChange = {},
        onPasswordChange = {},
        onSubmit = {},
        onForgotPassword = {},
        onRegister = {},
        onSupport = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenLoadingPreview() {
    LoginScreenContent(
        uiState = LoginUiState(
            email = "test@identityx.com",
            password = "password123",
            status = LoginStatus.Loading
        ),
        onEmailChange = {},
        onPasswordChange = {},
        onSubmit = {},
        onForgotPassword = {},
        onRegister = {},
        onSupport = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenErrorPreview() {
    LoginScreenContent(
        uiState = LoginUiState(
            email = "wrong@example.com",
            password = "wrong",
            status = LoginStatus.Error("Invalid email or password")
        ),
        onEmailChange = {},
        onPasswordChange = {},
        onSubmit = {},
        onForgotPassword = {},
        onRegister = {},
        onSupport = {}
    )
}
