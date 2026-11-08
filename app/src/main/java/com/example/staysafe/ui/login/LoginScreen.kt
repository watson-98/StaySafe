package com.example.staysafe.ui.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Hotel
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val StayBlue = Color(0xFF1565D8)
val StayBlueDark = Color(0xFF0D47A1)
val ScreenBackground = Color(0xFFF4F8FF)
private val FieldBackground = Color(0xFFEAF2FF)
private val FieldBorder = Color(0xFFD3E2F8)
val HintGray = Color(0xFF8A94A6)
val TextGray = Color(0xFF3C4858)

enum class UserRole { Guest, HotelOwner }

const val PHONE_ERROR = "Enter 10 digits starting with 099, 098, 088 or 089"

private val phoneRegex = Regex("^(099|098|088|089)\\d{7}$")

fun isValidPhone(phone: String): Boolean = phoneRegex.matches(phone.trim())

@Composable
fun LoginScreen(
    errorMessage: String? = null,
    onInputChange: () -> Unit = {},
    onLoginClick: (phone: String, password: String, role: UserRole) -> Unit = { _, _, _ -> },
    onSignUpClick: () -> Unit = {}
) {
    var selectedRole by remember { mutableStateOf(UserRole.Guest) }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(72.dp))

            // to be replaced with a logo
            Icon(
                imageVector = Icons.Outlined.Hotel,
                contentDescription = "StaySafe logo",
                tint = StayBlue,
                modifier = Modifier.size(84.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "StaySafe",
                color = StayBlueDark,
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Book Rooms. Stay Safe.",
                color = TextGray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(36.dp))

            RoleToggle(
                selectedRole = selectedRole,
                onRoleSelected = { selectedRole = it }
            )

            Spacer(modifier = Modifier.height(20.dp))

            InputField(
                value = phone,
                onValueChange = {
                    phone = it
                    onInputChange()
                },
                placeholder = "Phone Number",
                modifier = Modifier.testTag("login_phone"),
                leadingIcon = Icons.Outlined.Person,
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
                value = password,
                onValueChange = {
                    password = it
                    onInputChange()
                },
                placeholder = "Password",
                modifier = Modifier.testTag("login_password"),
                leadingIcon = Icons.Outlined.Lock,
                keyboardType = KeyboardType.Password,
                visualTransformation = if (passwordVisible) VisualTransformation.None
                else PasswordVisualTransformation(),
                trailingContent = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff
                            else Icons.Outlined.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = TextGray
                        )
                    }
                }
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = Color(0xFFD62839),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { onLoginClick(phone.trim(), password, selectedRole) },
                enabled = isValidPhone(phone) && password.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = StayBlue,
                    disabledContainerColor = Color(0xFFA9C4EE),
                    disabledContentColor = Color.White
                )
            ) {
                Text(text = "Log In", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Don't have an account? ", color = TextGray, fontSize = 13.sp)
                Text(
                    text = "Sign Up",
                    color = StayBlue,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onSignUpClick() }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun RoleToggle(
    selectedRole: UserRole,
    onRoleSelected: (UserRole) -> Unit
) {
    val shape = RoundedCornerShape(10.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(shape)
            .background(Color.White)
            .border(BorderStroke(1.dp, FieldBorder), shape)
    ) {
        RoleTab(
            label = "Guest",
            selected = selectedRole == UserRole.Guest,
            onClick = { onRoleSelected(UserRole.Guest) },
            modifier = Modifier.weight(1f)
        )
        RoleTab(
            label = "Hotel Owner",
            selected = selectedRole == UserRole.HotelOwner,
            onClick = { onRoleSelected(UserRole.HotelOwner) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun RoleTab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) StayBlue else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else TextGray,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun InputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingContent: (@Composable () -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        singleLine = true,
        shape = RoundedCornerShape(10.dp),
        placeholder = { Text(text = placeholder, color = HintGray, fontSize = 14.sp) },
        leadingIcon = {
            Icon(imageVector = leadingIcon, contentDescription = null, tint = TextGray)
        },
        trailingIcon = trailingContent,
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = FieldBackground,
            unfocusedContainerColor = FieldBackground,
            focusedBorderColor = StayBlue,
            unfocusedBorderColor = FieldBorder,
            cursorColor = StayBlue
        )
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun LoginScreenPreview() {
    LoginScreen()
}