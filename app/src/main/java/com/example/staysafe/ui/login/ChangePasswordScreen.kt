package com.example.staysafe.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalAutofill
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ChangePasswordScreen(
    initialPhone: String = "",
    initialRole: UserRole = UserRole.Guest,
    errorMessage: String? = null,
    onInputChange: () -> Unit = {},
    onChangePasswordClick: (phone: String, role: UserRole, existingPassword: String, newPassword: String) -> String? = { _, _, _, _ -> null },
    onSuccess: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    var selectedRole by remember { mutableStateOf(initialRole) }
    var phone by remember { mutableStateOf(initialPhone) }
    var existingPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }

    var existingPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var localError by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val passwordsMismatch = confirmNewPassword.isNotEmpty() && confirmNewPassword != newPassword
    val canSubmit = isValidPhone(phone) &&
            existingPassword.isNotBlank() &&
            newPassword.isNotBlank() &&
            confirmNewPassword == newPassword

    val displayError = localError ?: errorMessage

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = StayBlueDark
                    )
                }
                Text(
                    text = "Change Password",
                    color = StayBlueDark,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Provide your existing or temporary password, then enter and confirm your new password.",
                color = TextGray,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            RoleToggle(
                selectedRole = selectedRole,
                onRoleSelected = {
                    selectedRole = it
                    localError = null
                    onInputChange()
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            CompositionLocalProvider(LocalAutofill provides null) {
                InputField(
                    value = phone,
                    onValueChange = {
                        phone = it
                        localError = null
                        onInputChange()
                    },
                    placeholder = "Phone Number",
                    modifier = Modifier.testTag("change_phone"),
                    leadingIcon = Icons.Outlined.Phone,
                    keyboardType = KeyboardType.Phone
                )

                if (phone.isNotEmpty() && !isValidPhone(phone)) {
                    Text(
                        text = PHONE_ERROR,
                        color = Color(0xFFD62839),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                InputField(
                    value = existingPassword,
                    onValueChange = {
                        existingPassword = it
                        localError = null
                        onInputChange()
                    },
                    placeholder = "Existing / Temporary Password",
                    modifier = Modifier.testTag("change_existing_password"),
                    leadingIcon = Icons.Outlined.Lock,
                    keyboardType = KeyboardType.Password,
                    visualTransformation = if (existingPasswordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    trailingContent = {
                        IconButton(onClick = { existingPasswordVisible = !existingPasswordVisible }) {
                            Icon(
                                imageVector = if (existingPasswordVisible) Icons.Outlined.VisibilityOff
                                else Icons.Outlined.Visibility,
                                contentDescription = if (existingPasswordVisible) "Hide password" else "Show password",
                                tint = TextGray
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                InputField(
                    value = newPassword,
                    onValueChange = {
                        newPassword = it
                        localError = null
                        onInputChange()
                    },
                    placeholder = "New Password",
                    modifier = Modifier.testTag("change_new_password"),
                    leadingIcon = Icons.Outlined.Lock,
                    keyboardType = KeyboardType.Password,
                    visualTransformation = if (newPasswordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    trailingContent = {
                        IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                            Icon(
                                imageVector = if (newPasswordVisible) Icons.Outlined.VisibilityOff
                                else Icons.Outlined.Visibility,
                                contentDescription = if (newPasswordVisible) "Hide password" else "Show password",
                                tint = TextGray
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                InputField(
                    value = confirmNewPassword,
                    onValueChange = {
                        confirmNewPassword = it
                        localError = null
                        onInputChange()
                    },
                    placeholder = "Confirm New Password",
                    modifier = Modifier.testTag("change_confirm_password"),
                    leadingIcon = Icons.Outlined.Lock,
                    keyboardType = KeyboardType.Password,
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    trailingContent = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Outlined.VisibilityOff
                                else Icons.Outlined.Visibility,
                                contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password",
                                tint = TextGray
                            )
                        }
                    }
                )
            }

            if (passwordsMismatch) {
                Text(
                    text = "New passwords do not match",
                    color = Color(0xFFD62839),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            if (displayError != null) {
                Text(
                    text = displayError,
                    color = Color(0xFFD62839),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val err = onChangePasswordClick(phone.trim(), selectedRole, existingPassword, newPassword)
                    if (err != null) {
                        localError = err
                    } else {
                        showSuccessDialog = true
                    }
                },
                enabled = canSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("change_password_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = StayBlue,
                    disabledContainerColor = Color(0xFFA9C4EE),
                    disabledContentColor = Color.White
                )
            ) {
                Text(text = "Change Password", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                onSuccess()
            },
            title = {
                Text(
                    text = "Password Changed Successfully",
                    color = StayBlueDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "Your password has been updated. You can now log in with your new password.",
                    color = TextGray,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onSuccess()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StayBlue)
                ) {
                    Text("Go to Login")
                }
            }
        )
    }
}
