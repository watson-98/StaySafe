package com.example.staysafe

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.staysafe.data.HotelRepository
import com.example.staysafe.ui.booking.BookingConfirmationScreen
import com.example.staysafe.ui.booking.BookingSuccessScreen
import com.example.staysafe.ui.home.HomeScreen
import com.example.staysafe.ui.login.LoginScreen
import com.example.staysafe.ui.login.SignUpScreen
import com.example.staysafe.ui.login.UserRole
import com.example.staysafe.ui.owner.AddRoomScreen
import com.example.staysafe.ui.owner.OwnerDashboardScreen
import com.example.staysafe.ui.owner.OwnerProfileScreen
import com.example.staysafe.ui.owner.OwnerRoomListScreen
import com.example.staysafe.ui.payment.PaymentScreen
import com.example.staysafe.ui.rooms.Room
import com.example.staysafe.ui.rooms.RoomDetailsScreen
import com.example.staysafe.ui.rooms.RoomListScreen
import com.example.staysafe.ui.rooms.RoomStatus

object Routes {
    const val LOGIN = "login"
    const val SIGN_UP = "sign_up"
    const val HOME = "home"
    const val ROOM_LIST = "room_list/{hotelName}"
    const val ROOM_DETAILS = "room_details/{hotelName}/{roomNumber}"
    const val BOOKING_CONFIRM = "booking_confirm"
    const val PAYMENT = "payment"
    const val BOOKING_SUCCESS = "booking_success"
    const val OWNER_DASHBOARD = "owner_dashboard"
    const val OWNER_ROOMS = "owner_rooms"
    const val OWNER_PROFILE = "owner_profile"
    const val ADD_ROOM = "add_room"

    // Uri.encode keeps names with spaces valid in a route
    fun roomList(hotelName: String) = "room_list/${Uri.encode(hotelName)}"

    fun roomDetails(hotelName: String, roomNumber: String) =
        "room_details/${Uri.encode(hotelName)}/${Uri.encode(roomNumber)}"
}

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {

    // errors returned by the database checks, shown on the Login and Sign Up screens
    var loginError by remember { mutableStateOf<String?>(null) }
    var signUpError by remember { mutableStateOf<String?>(null) }

    // login stays on the back stack so Back returns to it
    fun goToStartScreen(role: UserRole) {
        val destination = if (role == UserRole.HotelOwner) Routes.OWNER_DASHBOARD else Routes.HOME
        navController.navigate(destination)
    }

    // end the session, then clear the back stack back to a fresh login
    fun logout() {
        HotelRepository.logout()
        navController.navigate(Routes.LOGIN) {
            popUpTo(Routes.LOGIN) { inclusive = true }
        }
    }

    NavHost(navController = navController, startDestination = Routes.LOGIN) {

        // ---------- Login / Sign Up ----------
        composable(Routes.LOGIN) {
            LoginScreen(
                errorMessage = loginError,
                onInputChange = { loginError = null },
                onLoginClick = { phone, password, role ->
                    // checks the phone number, password and role against the database
                    val error = HotelRepository.login(phone, password, role)
                    loginError = error
                    if (error == null) goToStartScreen(role)
                },
                onSignUpClick = {
                    loginError = null
                    navController.navigate(Routes.SIGN_UP)
                }
            )
        }
        composable(Routes.SIGN_UP) {
            SignUpScreen(
                errorMessage = signUpError,
                onInputChange = { signUpError = null },
                onSignUpClick = { signUp ->
                    // saves the account (and the hotel, for owners) and logs the user in
                    val error = HotelRepository.register(signUp)
                    signUpError = error
                    if (error == null) goToStartScreen(signUp.role)
                },
                onBackClick = {
                    signUpError = null
                    navController.popBackStack()
                }
            )
        }

        // ---------- Guest flow ----------
        composable(Routes.HOME) {
            HomeScreen(
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
                roomType = room?.type.orEmpty(),
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
            // no reservation in progress (for example after the app was restarted): go back
            val draft = HotelRepository.draft
            if (draft == null) {
                LaunchedEffect(Unit) { navController.popBackStack() }
                return@composable
            }
            BookingConfirmationScreen(
                roomNumber = draft.roomNumber,
                roomType = draft.roomType,
                hotelName = draft.hotelName,
                checkIn = draft.checkIn,
                checkOut = draft.checkOut,
                guestCount = draft.guests,
                minGuests = HotelRepository.MIN_GUESTS,
                maxGuests = HotelRepository.MAX_GUESTS,
                totalPrice = draft.pricePerNight,
                onGuestCountChange = { HotelRepository.setGuests(it) },
                onBackClick = { navController.popBackStack() },
                onProceedClick = { navController.navigate(Routes.PAYMENT) }
            )
        }
        composable(Routes.PAYMENT) {
            val draft = HotelRepository.draft
            if (draft == null) {
                LaunchedEffect(Unit) { navController.popBackStack() }
                return@composable
            }
            PaymentScreen(
                roomNumber = draft.roomNumber,
                hotelName = draft.hotelName,
                totalAmount = draft.pricePerNight,
                upfrontPercent = HotelRepository.upfrontPercent,
                methods = HotelRepository.paymentMethods,
                onBackClick = { navController.popBackStack() },
                onPayNowClick = { method ->
                    // saves the booking and marks the room as booked in the database
                    if (HotelRepository.confirmBooking(method.name) != null) {
                        navController.navigate(Routes.BOOKING_SUCCESS)
                    }
                }
            )
        }
        composable(Routes.BOOKING_SUCCESS) {
            val booking = HotelRepository.lastBooking
            if (booking == null) {
                LaunchedEffect(Unit) { navController.popBackStack(Routes.HOME, inclusive = false) }
                return@composable
            }
            BookingSuccessScreen(
                referenceNumber = booking.reference,
                hotelName = booking.hotelName,
                roomNumber = booking.roomNumber,
                checkIn = booking.checkIn,
                checkOut = booking.checkOut,
                totalPaid = booking.amountPaid,
                // pop the booking flow off the back stack
                onBackToHomeClick = {
                    navController.popBackStack(Routes.HOME, inclusive = false)
                }
            )
        }

        // ---------- Owner flow ----------
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
                    HotelRepository.addRoom(
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
                    navController.popBackStack()
                }
            )
        }
    }
}