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
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ForgotPasswordScreen(
    errorMessage: String? = null,
    onInputChange: () -> Unit = {},
    onSendTemporaryPasswordClick: (phone: String, role: UserRole) -> Pair<String?, String?> = { _, _ -> Pair(null, null) },
    onProceedToChangePasswordClick: (phone: String, role: UserRole) -> Unit = { _, _ -> },
    onBackClick: () -> Unit = {}
) {
    var selectedRole by remember { mutableStateOf(UserRole.Guest) }
    var phone by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }
    var generatedTempPassword by remember { mutableStateOf<String?>(null) }
    var showDialog by remember { mutableStateOf(false) }

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
                    text = "Forgot Password",
                    color = StayBlueDark,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Enter your registered phone number. A temporary password will be sent to your phone, which you can use to set a new password.",
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
                    modifier = Modifier.testTag("forgot_phone"),
                    leadingIcon = Icons.Outlined.Phone,
                    keyboardType = KeyboardType.Phone
                )
            }

            if (phone.isNotEmpty() && !isValidPhone(phone)) {
                Text(
                    text = PHONE_ERROR,
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
                    val (err, temp) = onSendTemporaryPasswordClick(phone.trim(), selectedRole)
                    if (err != null) {
                        localError = err
                    } else {
                        generatedTempPassword = temp
                        showDialog = true
                    }
                },
                enabled = isValidPhone(phone),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("forgot_send_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = StayBlue,
                    disabledContainerColor = Color(0xFFA9C4EE),
                    disabledContentColor = Color.White
                )
            ) {
                Text(text = "Send Temporary Password", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showDialog && generatedTempPassword != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text = "Temporary Password Sent",
                    color = StayBlueDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "A temporary password has been generated and sent to $phone:\n\nTemporary Password: $generatedTempPassword\n\nPlease use this as your existing password to set a new password.",
                    color = TextGray,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        onProceedToChangePasswordClick(phone.trim(), selectedRole)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StayBlue)
                ) {
                    Text("Change Password Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Dismiss", color = TextGray)
                }
            }
        )
    }
}
