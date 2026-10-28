package com.example.staysafe

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.staysafe.data.AuthRepository
import com.example.staysafe.data.BookingDraft
import com.example.staysafe.data.HotelRepository
import com.example.staysafe.data.ROOM_TAKEN_ERROR
import com.example.staysafe.data.UserProfile
import com.example.staysafe.ui.booking.BookingConfirmationScreen
import com.example.staysafe.ui.booking.BookingSuccessScreen
import com.example.staysafe.ui.home.HomeScreen
import com.example.staysafe.ui.login.LoginScreen
import com.example.staysafe.ui.login.ScreenBackground
import com.example.staysafe.ui.login.SignUpScreen
import com.example.staysafe.ui.login.StayBlue
import com.example.staysafe.ui.login.UserRole
import com.example.staysafe.ui.owner.AddRoomScreen
import com.example.staysafe.ui.owner.ROOM_EXISTS_ERROR
import com.example.staysafe.ui.owner.OwnerDashboardScreen
import com.example.staysafe.ui.owner.OwnerProfileScreen
import com.example.staysafe.ui.owner.OwnerRoomListScreen
import com.example.staysafe.ui.payment.PaymentScreen
import com.example.staysafe.ui.rooms.Room
import com.example.staysafe.ui.rooms.RoomDetailsScreen
import com.example.staysafe.ui.rooms.RoomListScreen
import com.example.staysafe.ui.rooms.RoomStatus

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val SIGN_UP = "sign_up"
    const val HOME_ROUTE = "home?city={city}&hotelName={hotelName}"
    const val ROOM_LIST = "room_list/{hotelName}"
    const val ROOM_DETAILS = "room_details/{hotelName}/{roomNumber}"
    const val BOOKING_CONFIRM = "booking_confirm"
    const val PAYMENT = "payment"
    const val BOOKING_SUCCESS = "booking_success"
    const val OWNER_DASHBOARD = "owner_dashboard"
    const val OWNER_ROOMS = "owner_rooms"
    const val OWNER_PROFILE = "owner_profile"
    const val ADD_ROOM = "add_room"

    fun home(city: String = "", hotelName: String = "") =
        "home?city=${Uri.encode(city)}&hotelName=${Uri.encode(hotelName)}"

    // Uri.encode keeps names with spaces valid in a route
    fun roomList(hotelName: String) = "room_list/${Uri.encode(hotelName)}"

    fun roomDetails(hotelName: String, roomNumber: String) =
        "room_details/${Uri.encode(hotelName)}/${Uri.encode(roomNumber)}"
}

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    val context = LocalContext.current

    // Replaces the screen we came from, so Back never returns to login/splash
    // while someone is logged in.
    fun enterApp(profile: UserProfile, popFrom: String) {
        HotelRepository.start(profile)
        val destination =
            if (profile.role == UserRole.HotelOwner) Routes.OWNER_DASHBOARD else Routes.home()
        navController.navigate(destination) {
            popUpTo(popFrom) { inclusive = true }
        }
    }

    // ends the session and clears the whole back stack, so Back exits the app
    fun logout() {
        AuthRepository.logout()
        HotelRepository.stop()
        navController.navigate(Routes.LOGIN) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }

    NavHost(navController = navController, startDestination = Routes.SPLASH) {

        // decides between login and the right home screen from the saved session
        composable(Routes.SPLASH) {
            LaunchedEffect(Unit) {
                AuthRepository.restoreSession { profile ->
                    if (profile != null) {
                        enterApp(profile, Routes.SPLASH)
                    } else {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ScreenBackground),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = StayBlue)
            }
        }

        composable(Routes.LOGIN) {
            var error by remember { mutableStateOf<String?>(null) }
            var loading by remember { mutableStateOf(false) }

            LoginScreen(
                errorMessage = error,
                isLoading = loading,
                onInputChanged = { error = null },
                onLoginClick = { phone, password, role ->
                    loading = true
                    error = null
                    AuthRepository.login(phone, password, role) { profile, message ->
                        loading = false
                        if (profile != null) enterApp(profile, Routes.LOGIN) else error = message
                    }
                },
                onSignUpClick = { navController.navigate(Routes.SIGN_UP) }
            )
        }
        composable(Routes.SIGN_UP) {
            var error by remember { mutableStateOf<String?>(null) }
            var loading by remember { mutableStateOf(false) }

            SignUpScreen(
                errorMessage = error,
                isLoading = loading,
                onInputChanged = { error = null },
                onSignUpClick = { signUp ->
                    loading = true
                    error = null
                    AuthRepository.signUp(
                        fullName = signUp.fullName,
                        phone = signUp.phone,
                        password = signUp.password,
                        role = signUp.role,
                        hotelName = signUp.hotelName,
                        city = signUp.city
                    ) { profile, message ->
                        loading = false
                        if (profile != null) {
                            // pops sign up and login, then opens the guest or owner home
                            enterApp(profile, Routes.LOGIN)
                            if (profile.role == UserRole.HotelOwner) {
                                HotelRepository.registerOwnerHotel(profile.hotelName, profile.city)
                            }
                        } else {
                            error = message
                        }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.HOME_ROUTE,
            arguments = listOf(
                navArgument("city") { type = NavType.StringType; defaultValue = "" },
                navArgument("hotelName") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val cityArg = backStackEntry.arguments?.getString("city") ?: ""
            val hotelNameArg = backStackEntry.arguments?.getString("hotelName") ?: ""
            val initialCity = if (cityArg.isNotBlank()) cityArg else "Lilongwe"

            HomeScreen(
                initialCity = initialCity,
                initialSearchText = hotelNameArg,
                onHotelClick = { hotel -> navController.navigate(Routes.roomList(hotel.name)) },
                onLogoutClick = { logout() }
            )
        }
        composable(
            route = Routes.ROOM_LIST,
            arguments = listOf(navArgument("hotelName") { type = NavType.StringType })
        ) { backStackEntry ->
            val hotelName = backStackEntry.arguments?.getString("hotelName") ?: ""
            val hotel = HotelRepository.findHotel(hotelName)
            RoomListScreen(
                hotelName = hotel?.name ?: hotelName,
                hotelLocation = hotel?.location ?: "",
                rooms = HotelRepository.roomsOf(hotelName),
                onBackClick = { navController.popBackStack() },
                onRoomClick = { room ->
                    navController.navigate(Routes.roomDetails(hotelName, room.number))
                }
            )
        }
        composable(
            route = Routes.ROOM_DETAILS,
            arguments = listOf(
                navArgument("hotelName") { type = NavType.StringType },
                navArgument("roomNumber") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val hotelName = backStackEntry.arguments?.getString("hotelName") ?: ""
            val roomNumber = backStackEntry.arguments?.getString("roomNumber") ?: ""
            val room = HotelRepository.roomsOf(hotelName).firstOrNull { it.number == roomNumber }

            RoomDetailsScreen(
                roomNumber = roomNumber,
                roomType = room?.type ?: "Standard Room",
                hasWifi = room?.hasWifi ?: false,
                hasAirConditioning = room?.hasAirConditioning ?: false,
                pricePerNight = room?.pricePerNight ?: 0,
                onBackClick = { navController.popBackStack() },
                onReserveClick = {
                    if (room != null) {
                        HotelRepository.startBooking(hotelName, room)
                        navController.navigate(Routes.BOOKING_CONFIRM)
                    }
                }
            )
        }
        composable(Routes.BOOKING_CONFIRM) {
            val draft = HotelRepository.draft ?: BookingDraft()
            BookingConfirmationScreen(
                roomNumber = draft.roomNumber,
                roomType = draft.roomType,
                hotelName = draft.hotelName,
                checkIn = draft.checkIn,
                checkOut = draft.checkOut,
                guests = draft.guests,
                totalPrice = draft.pricePerNight,
                onBackClick = { navController.popBackStack() },
                onProceedClick = { navController.navigate(Routes.PAYMENT) }
            )
        }
        composable(Routes.PAYMENT) {
            val draft = HotelRepository.draft ?: BookingDraft()
            var paying by remember { mutableStateOf(false) }
            PaymentScreen(
                roomNumber = draft.roomNumber,
                hotelName = draft.hotelName,
                totalAmount = draft.pricePerNight,
                onBackClick = { navController.popBackStack() },
                onPayNowClick = {
                    if (!paying) {
                        paying = true
                        HotelRepository.confirmBooking { booking, message ->
                            paying = false
                            if (booking != null) {
                                navController.navigate(Routes.BOOKING_SUCCESS)
                            } else {
                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                // someone else took the room: back to the room list
                                if (message == ROOM_TAKEN_ERROR) {
                                    navController.popBackStack(Routes.ROOM_LIST, inclusive = false)
                                }
                            }
                        }
                    }
                }
            )
        }
        composable(Routes.BOOKING_SUCCESS) {
            val booking = HotelRepository.lastBooking
            BookingSuccessScreen(
                referenceNumber = booking?.reference ?: "",
                hotelName = booking?.hotelName ?: "",
                roomNumber = booking?.roomNumber ?: "",
                checkIn = booking?.checkIn ?: "",
                checkOut = booking?.checkOut ?: "",
                totalPaid = booking?.amountPaid ?: 0,
                // pop the booking flow off the back stack
                onBackToHomeClick = {
                    navController.popBackStack(Routes.HOME_ROUTE, inclusive = false)
                }
            )
        }

        composable(Routes.OWNER_DASHBOARD) {
            val hotel = HotelRepository.ownerHotel()
            OwnerDashboardScreen(
                hotelName = hotel.name,
                location = hotel.city,
                vacantRooms = HotelRepository.vacantCount(hotel.name),
                bookedRooms = HotelRepository.bookedCount(hotel.name),
                onRoomsClick = { navController.navigate(Routes.OWNER_ROOMS) },
                onAddRoomClick = { navController.navigate(Routes.ADD_ROOM) },
                onProfileClick = { navController.navigate(Routes.OWNER_PROFILE) },
                onProfileIconClick = { navController.navigate(Routes.OWNER_PROFILE) },
                onLogoutClick = { logout() }
            )
        }
        composable(Routes.OWNER_PROFILE) {
            val hotel = HotelRepository.ownerHotel()
            OwnerProfileScreen(
                hotelName = hotel.name,
                location = hotel.city,
                onBackClick = { navController.popBackStack() },
                onLogoutClick = { logout() }
            )
        }
        composable(Routes.OWNER_ROOMS) {
            val hotel = HotelRepository.ownerHotel()
            OwnerRoomListScreen(
                rooms = HotelRepository.roomsOf(hotel.name),
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(Routes.ADD_ROOM) {
            val hotel = HotelRepository.ownerHotel()
            AddRoomScreen(
                existingRoomNumbers = HotelRepository.roomsOf(hotel.name).map { it.number }.toSet(),
                onBackClick = { navController.popBackStack() },
                onSaveClick = { newRoom ->
                    val added = HotelRepository.addRoom(
                        hotelName = hotel.name,
                        room = Room(
                            number = newRoom.roomNumber,
                            type = newRoom.roomType,
                            status = RoomStatus.Vacant,
                            pricePerNight = newRoom.pricePerNight,
                            hasWifi = newRoom.hasWifi,
                            hasAirConditioning = newRoom.hasAirConditioning
                        )
                    )
                    if (added) {
                        navController.popBackStack()
                    } else {
                        Toast.makeText(context, ROOM_EXISTS_ERROR, Toast.LENGTH_LONG).show()
                    }
                }
            )
        }
    }
}