package com.printcare.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.printcare.app.ui.components.FormField
import com.printcare.app.ui.components.PrimaryAction
import com.printcare.app.ui.components.ScreenCard
import com.printcare.app.ui.components.SecondaryAction
import com.printcare.app.utils.ValidationUtils

@Composable
fun AuthScreen(
    isSignUp: Boolean,
    firebaseConfigured: Boolean,
    working: Boolean,
    onSignIn: (String, String) -> Unit,
    onCreateAccount: (String, String, String) -> Unit,
    onForgotPassword: (String) -> Unit,
    onNavigateToSignUp: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var showResetDialog by rememberSaveable { mutableStateOf(false) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Print,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 12.dp)
            )
            Column {
                Text(
                    "PrintShop",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Customer Manager",
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        Spacer(Modifier.height(24.dp))

        if (!firebaseConfigured) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    "Firebase setup is required to sign in. Register the Android app, add app/google-services.json, enable Email/Password sign-in, and create Firestore. See README.md.",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        ScreenCard {
            Text(
                if (isSignUp) "Create staff account" else "Welcome back",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                if (isSignUp) "Register to manage your shop's customer directory."
                else "Sign in to manage your printing shop customers.",
                color = MaterialTheme.colorScheme.secondary
            )

            if (isSignUp) {
                FormField("Full name", name, { name = it }, error = nameError, enabled = !working)
            }

            FormField(
                label = "Email address",
                value = email,
                onValueChange = { email = it },
                error = emailError,
                keyboardType = KeyboardType.Email,
                enabled = !working
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Password") },
                isError = passwordError != null,
                supportingText = passwordError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showPassword) "Hide password" else "Show password"
                        )
                    }
                },
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                enabled = !working
            )

            if (isSignUp) {
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Confirm password") },
                    isError = confirmError != null,
                    supportingText = confirmError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    enabled = !working
                )
            }

            PrimaryAction(
                text = if (isSignUp) "Create account" else "Sign in",
                enabled = firebaseConfigured,
                working = working,
                onClick = {
                    nameError = if (isSignUp) ValidationUtils.validateName(name) else null
                    emailError = ValidationUtils.validateEmail(email)
                    passwordError = ValidationUtils.validatePassword(password)
                    confirmError = if (isSignUp && confirmPassword != password) {
                        "Passwords do not match."
                    } else if (isSignUp && confirmPassword.isEmpty()) {
                        "Please confirm your password."
                    } else {
                        null
                    }
                    if (nameError == null && emailError == null && passwordError == null && confirmError == null) {
                        if (isSignUp) onCreateAccount(name, email, password)
                        else onSignIn(email, password)
                    }
                }
            )

            if (!isSignUp) {
                SecondaryAction(
                    text = "Forgot password?",
                    onClick = { showResetDialog = true }
                )
            }
        }

        Spacer(Modifier.height(10.dp))
        if (isSignUp) {
            SecondaryAction("Already have an account? Sign in", onNavigateToLogin)
        } else {
            SecondaryAction("New to PrintShop? Create an account", onNavigateToSignUp)
        }
    }

    if (showResetDialog) {
        PasswordResetDialog(
            initialEmail = email,
            onDismiss = { showResetDialog = false },
            onSend = { address ->
                val error = ValidationUtils.validateEmail(address)
                if (error == null) {
                    email = address
                    showResetDialog = false
                    onForgotPassword(address)
                } else {
                    emailError = error
                    showResetDialog = false
                }
            }
        )
    }
}

@Composable
private fun PasswordResetDialog(
    initialEmail: String,
    onDismiss: () -> Unit,
    onSend: (String) -> Unit
) {
    var email by rememberSaveable { mutableStateOf(initialEmail) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reset password") },
        text = {
            FormField(
                label = "Account email",
                value = email,
                onValueChange = { email = it },
                keyboardType = KeyboardType.Email
            )
        },
        confirmButton = {
            Button(onClick = { onSend(email) }) {
                Text("Send reset email")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
