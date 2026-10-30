package com.example.staysafe.ui.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Hotel
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.staysafe.data.HotelRepository

private val SignUpFieldBackground = Color(0xFFEAF2FF)
private val SignUpFieldBorder = Color(0xFFD3E2F8)

// hotelName and city are only set for hotel owners
data class SignUpData(
    val fullName: String,
    val phone: String,
    val password: String,
    val role: UserRole,
    val hotelName: String = "",
    val city: String = ""
)

@Composable
fun SignUpScreen(
    cities: List<String> = HotelRepository.cities,
    errorMessage: String? = null,
    onInputChange: () -> Unit = {},
    onSignUpClick: (SignUpData) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    var selectedRole by remember { mutableStateOf(UserRole.Guest) }
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var hotelName by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPassword by remember { mutableStateOf("") }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val isOwner = selectedRole == UserRole.HotelOwner

    val canSignUp = fullName.isNotBlank() &&
            isValidPhone(phone) &&
            password.isNotBlank() &&
            confirmPassword == password &&
            (!isOwner || (hotelName.isNotBlank() && city.isNotBlank()))

    val passwordsMismatch = confirmPassword.isNotEmpty() && confirmPassword != password

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
                    text = "Create Account",
                    color = StayBlueDark,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            RoleToggle(
                selectedRole = selectedRole,
                onRoleSelected = { selectedRole = it }
            )

            Spacer(modifier = Modifier.height(20.dp))

            InputField(
                value = fullName,
                onValueChange = { fullName = it },
                placeholder = "Full Name",
                leadingIcon = Icons.Outlined.Person,
                keyboardType = KeyboardType.Text
            )

            Spacer(modifier = Modifier.height(14.dp))

            InputField(
                value = phone,
                onValueChange = {
                    phone = it
                    onInputChange()
                },
                placeholder = "Phone Number",
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

            if (isOwner) {
                Spacer(modifier = Modifier.height(14.dp))

                InputField(
                    value = hotelName,
                    onValueChange = {
                        hotelName = it
                        onInputChange()
                    },
                    placeholder = "Hotel Name",
                    leadingIcon = Icons.Outlined.Hotel,
                    keyboardType = KeyboardType.Text
                )

                Spacer(modifier = Modifier.height(14.dp))

                CityDropdown(
                    selectedCity = city,
                    cities = cities,
                    onCitySelected = { city = it }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            InputField(
                value = password,
                onValueChange = { password = it },
                placeholder = "Password",
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

            Spacer(modifier = Modifier.height(14.dp))

            InputField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = "Confirm Password",
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
            if (passwordsMismatch) {
                Text(
                    text = "Passwords do not match",
                    color = Color(0xFFD62839),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    onSignUpClick(
                        SignUpData(
                            fullName = fullName.trim(),
                            phone = phone.trim(),
                            password = password,
                            role = selectedRole,
                            hotelName = if (isOwner) hotelName.trim() else "",
                            city = if (isOwner) city else ""
                        )
                    )
                },
                enabled = canSignUp,
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
                Text(text = "Sign Up", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            val termsText = buildAnnotatedString {
                withStyle(SpanStyle(color = TextGray)) {
                    append("By signing up, you agree to our\n")
                }
                withStyle(SpanStyle(color = StayBlue)) {
                    append("Terms")
                }
                withStyle(SpanStyle(color = TextGray)) {
                    append(" and ")
                }
                withStyle(SpanStyle(color = StayBlue)) {
                    append("Privacy Policy")
                }
            }
            Text(
                text = termsText,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun CityDropdown(
    selectedCity: String,
    cities: List<String>,
    onCitySelected: (String) -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }
    val hasSelection = selectedCity.isNotEmpty()

    Box {
        Surface(
            onClick = { menuOpen = true },
            shape = RoundedCornerShape(4.dp),
            color = SignUpFieldBackground,
            border = BorderStroke(1.dp, SignUpFieldBorder),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = TextGray
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = if (hasSelection) selectedCity else "City",
                    color = if (hasSelection) StayBlueDark else HintGray,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Outlined.KeyboardArrowDown,
                    contentDescription = "Choose city",
                    tint = TextGray
                )
            }
        }

        DropdownMenu(
            expanded = menuOpen,
            onDismissRequest = { menuOpen = false },
            containerColor = Color.White
        ) {
            cities.forEach { cityName ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = cityName,
                            color = StayBlueDark,
                            fontSize = 14.sp,
                            fontWeight = if (cityName == selectedCity) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        onCitySelected(cityName)
                        menuOpen = false
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun SignUpScreenPreview() {
    SignUpScreen()
}