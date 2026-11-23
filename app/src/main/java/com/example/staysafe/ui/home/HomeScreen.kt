package com.example.staysafe.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Hotel
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.staysafe.data.GuestBooking
import com.example.staysafe.data.Hotel
import com.example.staysafe.data.HotelRepository
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
private val ImagePlaceholder = Color(0xFFBBD3F5)

@Composable
fun HomeScreen(
    hotels: List<Hotel> = HotelRepository.hotels,
    cities: List<String> = HotelRepository.cities,
    initialCity: String = cities.firstOrNull().orEmpty(),
    bookings: List<GuestBooking> = HotelRepository.bookings,
    userName: String = HotelRepository.currentUser?.fullName.orEmpty(),
    userPhone: String = HotelRepository.currentUser?.phone.orEmpty(),
    onHotelClick: (Hotel) -> Unit = {},
    onProfileClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var searchText by rememberSaveable { mutableStateOf("") }
    var selectedCity by rememberSaveable { mutableStateOf(initialCity) }

    // Back on Bookings/Profile returns to the Home tab
    BackHandler(enabled = selectedTab != 0) { selectedTab = 0 }

    val hotelsInCity = hotels.filter { it.city == selectedCity }
    val vacantTotal = hotelsInCity.sumOf { HotelRepository.vacantCount(it.name) }
    val bookedTotal = hotelsInCity.sumOf { HotelRepository.bookedCount(it.name) }

    val visibleHotels = hotelsInCity.filter {
        it.name.contains(searchText, ignoreCase = true)
    }

    Scaffold(
        containerColor = ScreenBackground,
        bottomBar = {
            HomeBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { innerPadding ->
        when (selectedTab) {
            0 -> HomeTab(
                innerPadding = innerPadding,
                hotels = visibleHotels,
                city = selectedCity,
                cities = cities,
                onCitySelected = { selectedCity = it },
                vacantRooms = vacantTotal,
                bookedRooms = bookedTotal,
                searchText = searchText,
                onSearchTextChange = { searchText = it },
                onHotelClick = onHotelClick,
                onProfileClick = {
                    selectedTab = 2
                    onProfileClick()
                }
            )
            1 -> BookingsTab(innerPadding = innerPadding, bookings = bookings)
            else -> ProfileTab(
                innerPadding = innerPadding,
                fullName = userName,
                phone = userPhone,
                bookingCount = bookings.size,
                onLogoutClick = onLogoutClick
            )
        }
    }
}

@Composable
private fun HomeTab(
    innerPadding: PaddingValues,
    hotels: List<Hotel>,
    city: String,
    cities: List<String>,
    onCitySelected: (String) -> Unit,
    vacantRooms: Int,
    bookedRooms: Int,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onHotelClick: (Hotel) -> Unit,
    onProfileClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .testTag("home_list"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "StaySafe",
                    color = StayBlue,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                IconButton(onClick = onProfileClick) {
                    Icon(
                        imageVector = Icons.Outlined.AccountCircle,
                        contentDescription = "Profile",
                        tint = StayBlue,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
        }

        item {
            CitySelector(
                selectedCity = city,
                cities = cities,
                onCitySelected = onCitySelected
            )
        }

        item {
            OutlinedTextField(
                value = searchText,
                onValueChange = onSearchTextChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("search_field"),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                placeholder = { Text("Search hotels...", color = HintGray, fontSize = 14.sp) },
                leadingIcon = {
                    Icon(Icons.Outlined.Search, contentDescription = null, tint = TextGray)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = StayBlue,
                    unfocusedBorderColor = CardBorder,
                    cursorColor = StayBlue
                )
            )
        }

        item { SectionTitle("Rooms in $city") }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    label = "Vacant",
                    count = vacantRooms,
                    caption = "rooms available",
                    background = VacantBg,
                    accent = VacantGreen,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Booked",
                    count = bookedRooms,
                    caption = "rooms booked",
                    background = BookedBg,
                    accent = BookedRed,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item { SectionTitle("Nearby Hotels") }
        if (hotels.isEmpty()) {
            item {
                Text(
                    text = if (searchText.isEmpty()) "No hotels found" else "No hotels match that name",
                    color = HintGray,
                    fontSize = 13.sp
                )
            }
        }
        items(hotels) { hotel ->
            HotelRow(hotel = hotel, onClick = { onHotelClick(hotel) })
        }
    }
}

@Composable
private fun CitySelector(
    selectedCity: String,
    cities: List<String>,
    onCitySelected: (String) -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }

    Box {
        Surface(
            onClick = { menuOpen = true },
            shape = RoundedCornerShape(10.dp),
            color = Color.White,
            border = BorderStroke(1.dp, CardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("city_selector")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = StayBlueDark
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = selectedCity,
                    color = StayBlueDark,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Outlined.KeyboardArrowDown,
                    contentDescription = "Change city",
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
                    modifier = Modifier.testTag("city_option_$cityName"),
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

@Composable
private fun BookingsTab(
    innerPadding: PaddingValues,
    bookings: List<GuestBooking>
) {
    if (bookings.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.CalendarMonth,
                contentDescription = null,
                tint = StayBlue,
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No bookings yet",
                color = StayBlueDark,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Rooms you book will appear here.",
                color = HintGray,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .testTag("bookings_list"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "My Bookings",
                color = StayBlueDark,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
        items(bookings) { booking ->
            BookingCard(booking)
        }
    }
}

@Composable
private fun BookingCard(booking: GuestBooking) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, CardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = booking.hotelName,
                        color = StayBlueDark,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Room ${booking.roomNumber}",
                        color = TextGray,
                        fontSize = 13.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(VacantBg)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Confirmed",
                        color = VacantGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    tint = StayBlueDark,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${booking.checkIn} - ${booking.checkOut}",
                    color = TextGray,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Paid: MWK ${"%,d".format(booking.amountPaid)}",
                color = StayBlueDark,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ProfileTab(
    innerPadding: PaddingValues,
    fullName: String,
    phone: String,
    bookingCount: Int,
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.AccountCircle,
            contentDescription = null,
            tint = StayBlue,
            modifier = Modifier.size(88.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = fullName,
            color = StayBlueDark,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(text = "Guest", color = HintGray, fontSize = 13.sp)

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            border = BorderStroke(1.dp, CardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ProfileDetail(label = "Full Name", value = fullName)
                ProfileDivider()
                ProfileDetail(label = "Phone Number", value = phone)
                ProfileDivider()
                ProfileDetail(label = "Bookings", value = bookingCount.toString())
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(
            onClick = onLogoutClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, StayBlue)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = null,
                tint = StayBlue
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Logout",
                color = StayBlue,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ProfileDetail(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, color = HintGray, fontSize = 12.sp)
        Text(
            text = value,
            color = TextGray,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ProfileDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .height(1.dp)
            .background(CardBorder)
    )
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = StayBlueDark,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun StatCard(
    label: String,
    count: Int,
    caption: String,
    background: Color,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.Hotel,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, color = accent, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = count.toString(),
            color = accent,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )
        Text(text = caption, color = accent, fontSize = 11.sp)
    }
}

@Composable
private fun HotelRow(hotel: Hotel, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, CardBorder),
        modifier = Modifier.fillMaxWidth().testTag("hotel_row_${hotel.name}")
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // To be replaced with real photos
            Box(
                modifier = Modifier
                    .size(width = 72.dp, height = 56.dp)
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
                    text = hotel.name,
                    color = StayBlueDark,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(text = hotel.location, color = HintGray, fontSize = 12.sp)
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Open ${hotel.name}",
                tint = TextGray
            )
        }
    }
}

private data class BottomTab(val label: String, val icon: ImageVector)

@Composable
private fun HomeBottomBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val tabs = listOf(
        BottomTab("Home", Icons.Outlined.Home),
        BottomTab("Bookings", Icons.Outlined.CalendarMonth)
    )
    NavigationBar(containerColor = Color.White) {
        tabs.forEachIndexed { index, tab ->
            NavigationBarItem(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label, fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = StayBlue,
                    selectedTextColor = StayBlue,
                    indicatorColor = Color(0xFFEAF2FF),
                    unselectedIconColor = HintGray,
                    unselectedTextColor = HintGray
                )
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun HomeScreenPreview() {
    HomeScreen()
}