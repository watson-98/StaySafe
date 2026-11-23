package com.example.staysafe.ui.owner

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.staysafe.data.HotelRepository
import com.example.staysafe.ui.login.HintGray
import com.example.staysafe.ui.login.ScreenBackground
import com.example.staysafe.ui.login.StayBlue
import com.example.staysafe.ui.login.StayBlueDark
import com.example.staysafe.ui.login.TextGray

private val CardBorder = Color(0xFFD3E2F8)

data class NewRoom(
    val roomNumber: String,
    val roomType: String,
    val pricePerNight: Int,
    val hasWifi: Boolean,
    val hasAirConditioning: Boolean
)

@Composable
fun AddRoomScreen(
    roomTypes: List<String> = HotelRepository.roomTypes,
    existingRoomNumbers: Set<String> = emptySet(),
    onBackClick: () -> Unit = {},
    onSaveClick: (NewRoom) -> Unit = {}
) {
    var roomNumber by remember { mutableStateOf("") }
    var roomType by remember { mutableStateOf(roomTypes.firstOrNull().orEmpty()) }
    var price by remember { mutableStateOf("") }
    var hasWifi by remember { mutableStateOf(true) }
    var hasAirConditioning by remember { mutableStateOf(true) }

    val isDuplicate = roomNumber.trim() in existingRoomNumbers
    val priceValue = price.toIntOrNull()
    val priceInvalid = price.isNotEmpty() && (priceValue == null || priceValue <= 0)
    val canSave = roomNumber.isNotBlank() && price.isNotBlank() && !isDuplicate && !priceInvalid

    Scaffold(containerColor = ScreenBackground) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = StayBlueDark
                    )
                }
                Text(
                    text = "Add New Room",
                    color = StayBlueDark,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                FieldLabel("Room Number")
                FormTextField(
                    value = roomNumber,
                    onValueChange = { roomNumber = it },
                    placeholder = "e.g. 104",
                    keyboardType = KeyboardType.Text,
                    tag = "addroom_number"
                )
                if (isDuplicate) {
                    Text(
                        text = "A room already exists with the same Room Number, please enter a different room number",
                        color = Color(0xFFD62839),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                FieldLabel("Room Type")
                RoomTypeDropdown(
                    selected = roomType,
                    options = roomTypes,
                    onSelected = { roomType = it }
                )

                Spacer(modifier = Modifier.height(18.dp))

                FieldLabel("Price per Night (MWK)")
                FormTextField(
                    value = price,
                    onValueChange = { input -> price = input.filter { it.isDigit() } },
                    placeholder = "e.g. 45000",
                    keyboardType = KeyboardType.Number,
                    tag = "addroom_price"
                )
                if (priceInvalid) {
                    Text(
                        text = "Price must be a positive number",
                        color = Color(0xFFD62839),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                FieldLabel("Facilities")
                FacilityCheckbox(
                    label = "WiFi",
                    checked = hasWifi,
                    onCheckedChange = { hasWifi = it }
                )
                FacilityCheckbox(
                    label = "Air Conditioning",
                    checked = hasAirConditioning,
                    onCheckedChange = { hasAirConditioning = it }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = {
                    onSaveClick(
                        NewRoom(
                            roomNumber = roomNumber.trim(),
                            roomType = roomType,
                            pricePerNight = price.toIntOrNull() ?: 0,
                            hasWifi = hasWifi,
                            hasAirConditioning = hasAirConditioning
                        )
                    )
                },
                enabled = canSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = StayBlue,
                    disabledContainerColor = Color(0xFFA9C4EE),
                    disabledContentColor = Color.White
                )
            ) {
                Text(text = "Save Room", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        color = StayBlueDark,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    tag: String
) {
    CompositionLocalProvider(LocalAutofill provides null) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag(tag),
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            placeholder = { Text(text = placeholder, color = HintGray, fontSize = 14.sp) },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = StayBlue,
                unfocusedBorderColor = CardBorder,
                cursorColor = StayBlue
            )
        )
    }
}

@Composable
private fun RoomTypeDropdown(
    selected: String,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Surface(
            onClick = { expanded = true },
            shape = RoundedCornerShape(10.dp),
            color = Color.White,
            border = BorderStroke(1.dp, CardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("room_type_field")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selected,
                    color = StayBlueDark,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Outlined.KeyboardArrowDown,
                    contentDescription = "Choose room type",
                    tint = TextGray
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = Color.White
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    modifier = Modifier.testTag("room_type_option_$option"),
                    text = { Text(option, color = StayBlueDark, fontSize = 14.sp) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun FacilityCheckbox(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = StayBlue,
                uncheckedColor = TextGray
            )
        )
        Text(text = label, color = TextGray, fontSize = 14.sp)
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun AddRoomScreenPreview() {
    AddRoomScreen()
}