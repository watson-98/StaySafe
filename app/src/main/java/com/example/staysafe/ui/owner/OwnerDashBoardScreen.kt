package com.example.staysafe.ui.owner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Hotel
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.staysafe.ui.login.HintGray
import com.example.staysafe.ui.login.ScreenBackground
import com.example.staysafe.ui.login.StayBlue
import com.example.staysafe.ui.login.StayBlueDark
import com.example.staysafe.ui.login.TextGray

private val CardBorder = Color(0xFFD3E2F8)
private val VacantBg = Color(0xFFDDF5E4)
private val VacantGreen = Color(0xFF1E9E4A)
private val BookedBg = Color(0xFFFDE3E6)
private val BookedRed = Color(0xFFD62839)

@Composable
fun OwnerDashboardScreen(
    hotelName: String,
    location: String,
    vacantRooms: Int,
    bookedRooms: Int,
    onProfileIconClick: () -> Unit = {},
    onRoomsClick: () -> Unit = {},
    onAddRoomClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    fun closeDrawerThen(action: () -> Unit) {
        scope.launch { drawerState.close() }
        action()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = Color.White) {
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp)) {
                    Text(
                        text = "StaySafe",
                        color = StayBlue,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(text = hotelName, color = TextGray, fontSize = 13.sp)
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(CardBorder)
                )
                Spacer(modifier = Modifier.height(8.dp))

                DrawerItem("Rooms", Icons.Outlined.Hotel) { closeDrawerThen(onRoomsClick) }
                DrawerItem("Add Room", Icons.Filled.AddCircle) { closeDrawerThen(onAddRoomClick) }
                DrawerItem("Profile", Icons.Outlined.Person) { closeDrawerThen(onProfileClick) }
                DrawerItem("Logout", Icons.AutoMirrored.Filled.ExitToApp) { closeDrawerThen(onLogoutClick) }
            }
        }
    ) {
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
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = "Open menu",
                            tint = StayBlueDark
                        )
                    }
                    Text(
                        text = "StaySafe",
                        color = StayBlueDark,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onProfileIconClick) {
                        Icon(
                            imageVector = Icons.Outlined.AccountCircle,
                            contentDescription = "Profile",
                            tint = StayBlue,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "My Hotel",
                        color = StayBlueDark,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = hotelName, color = TextGray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = HintGray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = location, color = HintGray, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            label = "Vacant Rooms",
                            count = vacantRooms,
                            background = VacantBg,
                            accent = VacantGreen,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = "Booked Rooms",
                            count = bookedRooms,
                            background = BookedBg,
                            accent = BookedRed,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, CardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            MenuRow("Rooms", Icons.Outlined.Hotel, StayBlueDark, onRoomsClick)
                            MenuDivider()
                            MenuRow("Add Room", Icons.Filled.AddCircle, StayBlue, onAddRoomClick)
                            MenuDivider()
                            MenuRow("Profile", Icons.Outlined.Person, StayBlueDark, onProfileClick)
                            MenuDivider()
                            MenuRow("Logout", Icons.AutoMirrored.Filled.ExitToApp, StayBlueDark, onLogoutClick)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun DrawerItem(label: String, icon: ImageVector, onClick: () -> Unit) {
    NavigationDrawerItem(
        label = { Text(text = label, fontSize = 14.sp) },
        selected = false,
        onClick = onClick,
        icon = { Icon(imageVector = icon, contentDescription = null) },
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
        colors = NavigationDrawerItemDefaults.colors(
            unselectedContainerColor = Color.Transparent,
            unselectedTextColor = StayBlueDark,
            unselectedIconColor = StayBlueDark
        )
    )
}

@Composable
private fun StatCard(
    label: String,
    count: Int,
    background: Color,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.Hotel,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                color = accent,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = count.toString(),
            color = accent,
            fontSize = 38.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun MenuRow(
    label: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                color = StayBlueDark,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = TextGray
            )
        }
    }
}

@Composable
private fun MenuDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(1.dp)
            .background(CardBorder)
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun OwnerDashboardScreenPreview() {
    // preview-only placeholder values; the real screen shows the owner's hotel from the database
    OwnerDashboardScreen(
        hotelName = "Sample Hotel",
        location = "Sample City",
        vacantRooms = 1,
        bookedRooms = 1
    )
}