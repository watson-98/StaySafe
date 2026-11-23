package com.example.staysafe.ui.owner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Hotel
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.staysafe.ui.login.HintGray
import com.example.staysafe.ui.login.ScreenBackground
import com.example.staysafe.ui.login.StayBlue
import com.example.staysafe.ui.login.StayBlueDark
import com.example.staysafe.ui.login.TextGray
import com.example.staysafe.ui.rooms.Room
import com.example.staysafe.ui.rooms.RoomStatus

private val CardBorder = Color(0xFFD3E2F8)
private val VacantBg = Color(0xFFDDF5E4)
private val VacantGreen = Color(0xFF1E9E4A)
private val BookedBg = Color(0xFFFDE3E6)
private val BookedRed = Color(0xFFD62839)
private val TabInactiveBg = Color(0xFFE6ECF5)
private val ImagePlaceholder = Color(0xFFBBD3F5)

@Composable
fun OwnerRoomListScreen(
    rooms: List<Room>,
    onBackClick: () -> Unit = {},
    onRoomClick: (Room) -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(RoomStatus.Vacant) }

    val vacantCount = rooms.count { it.status == RoomStatus.Vacant }
    val bookedCount = rooms.count { it.status == RoomStatus.Booked }
    val visibleRooms = rooms.filter { it.status == selectedTab }

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
                    text = "My Rooms",
                    color = StayBlueDark,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(TabInactiveBg)
            ) {
                OwnerRoomTab(
                    label = "Vacant ($vacantCount)",
                    selected = selectedTab == RoomStatus.Vacant,
                    selectedColor = VacantGreen,
                    onClick = { selectedTab = RoomStatus.Vacant },
                    modifier = Modifier.weight(1f)
                )
                OwnerRoomTab(
                    label = "Booked ($bookedCount)",
                    selected = selectedTab == RoomStatus.Booked,
                    selectedColor = BookedRed,
                    onClick = { selectedTab = RoomStatus.Booked },
                    modifier = Modifier.weight(1f)
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().testTag("owner_room_list"),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (visibleRooms.isEmpty()) {
                    item {
                        Text(
                            text = "No rooms here yet. Use Add Room from the dashboard.",
                            color = HintGray,
                            fontSize = 13.sp
                        )
                    }
                }
                items(visibleRooms) { room ->
                    OwnerRoomRow(room = room, onClick = { onRoomClick(room) })
                }
            }
        }
    }
}

@Composable
private fun OwnerRoomTab(
    label: String,
    selected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) selectedColor else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else TextGray,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun OwnerRoomRow(room: Room, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, CardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 76.dp, height = 72.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ImagePlaceholder),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Hotel,
                    contentDescription = null,
                    tint = StayBlue
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Room ${room.number}",
                    color = StayBlueDark,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(text = room.type, color = HintGray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                OwnerStatusChip(status = room.status)
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Open room ${room.number}",
                tint = TextGray
            )
        }
    }
}

@Composable
private fun OwnerStatusChip(status: RoomStatus) {
    val isVacant = status == RoomStatus.Vacant
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (isVacant) VacantBg else BookedBg)
            .padding(horizontal = 14.dp, vertical = 4.dp)
    ) {
        Text(
            text = if (isVacant) "Vacant" else "Booked",
            color = if (isVacant) VacantGreen else BookedRed,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun OwnerRoomListScreenPreview() {
    OwnerRoomListScreen(
        rooms = listOf(
            Room("A1", "Sample Type", RoomStatus.Vacant, 1000, hasWifi = true, hasAirConditioning = true),
            Room("A2", "Sample Type", RoomStatus.Booked, 1000, hasWifi = false, hasAirConditioning = false)
        )
    )
}