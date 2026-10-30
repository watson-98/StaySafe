package com.example.staysafe.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.staysafe.ui.login.SignUpData
import com.example.staysafe.ui.login.UserRole
import com.example.staysafe.ui.rooms.Room
import com.example.staysafe.ui.rooms.RoomStatus
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// vacant/booked counts are derived from the room list
data class Hotel(
    val name: String,
    val location: String,
    val city: String
)

data class GuestBooking(
    val hotelName: String,
    val roomNumber: String,
    val checkIn: String,
    val checkOut: String,
    val amountPaid: Int,
    val reference: String,
    val guests: Int,
    val paymentMethod: String
)

// reservation that hasn't been paid for yet; the dates come from the phone's clock
data class BookingDraft(
    val hotelName: String,
    val roomNumber: String,
    val roomType: String,
    val pricePerNight: Int,
    val guests: Int,
    val checkIn: String = dateText(daysFromToday = 0),
    val checkOut: String = dateText(daysFromToday = 1)
)

// Calendar and SimpleDateFormat work on API 24; java.time would need API 26
private fun dateText(daysFromToday: Int): String {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, daysFromToday)
    return SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(calendar.time)
}

private data class StoredRoom(val hotelName: String, val room: Room)

// Screens read the in-memory lists (so Compose updates), and every change is also saved
// to the SQLite database. Everything here is loaded from the database when the app starts.
object HotelRepository {

    const val MIN_GUESTS = 1
    const val MAX_GUESTS = 10

    val hotels = mutableStateListOf<Hotel>()
    val bookings = mutableStateListOf<GuestBooking>()   // the logged-in guest's bookings, newest first
    private val rooms = mutableStateListOf<StoredRoom>()

    // reference data, loaded from the cities, room_types, payment_methods and settings tables
    var cities by mutableStateOf<List<String>>(emptyList())
        private set
    var roomTypes by mutableStateOf<List<String>>(emptyList())
        private set
    var paymentMethods by mutableStateOf<List<PaymentMethodInfo>>(emptyList())
        private set
    var upfrontPercent by mutableStateOf(0)
        private set

    var currentUser by mutableStateOf<User?>(null)
        private set

    // in-progress reservation and the last paid booking
    var draft by mutableStateOf<BookingDraft?>(null)
        private set
    var lastBooking by mutableStateOf<GuestBooking?>(null)
        private set

    private var db: StaySafeDatabase? = null

    private fun database(): StaySafeDatabase =
        db ?: error("HotelRepository.initialize(context) must be called before use")

    // Call once at app start (MainActivity.onCreate). Safe to call again.
    fun initialize(context: Context) {
        if (db != null) return
        val database = StaySafeDatabase(context.applicationContext)
        db = database

        cities = database.allCities()
        roomTypes = database.allRoomTypes()
        paymentMethods = database.allPaymentMethods()
        upfrontPercent = database.setting("upfront_percent")?.toIntOrNull()
            ?: error("upfront_percent is missing from the settings table (check assets/seed.sql)")

        hotels.addAll(database.allHotels())
        rooms.addAll(database.allRooms().map { (hotelName, room) -> StoredRoom(hotelName, room) })
    }

    private fun saveUser(database: StaySafeDatabase, user: User, password: String) {
        val salt = PasswordHasher.newSalt()
        database.insertUser(user, PasswordHasher.hash(password, salt), salt)
    }

    // ---------------- accounts ----------------

    // Returns an error message, or null when the account was created (and the user is logged in)
    fun register(signUp: SignUpData): String? {
        val database = database()
        val phone = signUp.phone.trim()
        val isOwner = signUp.role == UserRole.HotelOwner
        val hotelName = signUp.hotelName.trim()

        if (database.findUser(phone) != null) return "This phone number is already registered"
        if (isOwner && database.hotelExists(hotelName)) return "A hotel named $hotelName already exists"

        val user = User(phone, signUp.fullName.trim(), signUp.role, if (isOwner) hotelName else null)
        database.transaction {
            saveUser(database, user, signUp.password)
            if (isOwner) database.insertHotel(Hotel(hotelName, signUp.city, signUp.city), phone)
        }
        if (isOwner) hotels.add(Hotel(hotelName, signUp.city, signUp.city))

        startSession(user)
        return null
    }

    // Returns an error message, or null when the login succeeded
    fun login(phone: String, password: String, role: UserRole): String? {
        val record = database().findUser(phone.trim())
        if (record == null || !PasswordHasher.matches(password, record.salt, record.passwordHash)) {
            return "Invalid phone number or password"
        }
        if (record.user.role != role) {
            val registeredAs = if (record.user.role == UserRole.HotelOwner) "a Hotel Owner" else "a Guest"
            return "This number is registered as $registeredAs. Choose the other tab."
        }
        startSession(record.user)
        return null
    }

    fun logout() {
        currentUser = null
        bookings.clear()
        draft = null
        lastBooking = null
    }

    private fun startSession(user: User) {
        currentUser = user
        draft = null
        lastBooking = null
        bookings.clear()
        if (user.role == UserRole.Guest) bookings.addAll(database().bookingsFor(user.phone))
    }

    // ---------------- reading ----------------

    fun findHotel(name: String): Hotel? = hotels.firstOrNull { it.name == name }

    fun roomsOf(hotelName: String): List<Room> =
        rooms.filter { it.hotelName == hotelName }.map { it.room }

    fun vacantCount(hotelName: String): Int =
        rooms.count { it.hotelName == hotelName && it.room.status == RoomStatus.Vacant }

    fun bookedCount(hotelName: String): Int =
        rooms.count { it.hotelName == hotelName && it.room.status == RoomStatus.Booked }

    // the hotel managed by the logged-in owner (empty if nobody is logged in)
    fun ownerHotel(): Hotel =
        currentUser?.hotelName?.let { findHotel(it) } ?: Hotel(name = "", location = "", city = "")

    // ---------------- owner actions ----------------

    // returns false if the room number already exists
    fun addRoom(hotelName: String, room: Room): Boolean {
        if (!database().insertRoom(hotelName, room)) return false
        rooms.add(StoredRoom(hotelName, room))
        return true
    }

    // ---------------- guest booking flow ----------------

    fun startBooking(hotelName: String, room: Room) {
        draft = BookingDraft(
            hotelName = hotelName,
            roomNumber = room.number,
            roomType = room.type,
            pricePerNight = room.pricePerNight,
            guests = MIN_GUESTS
        )
    }

    fun setGuests(count: Int) {
        draft = draft?.copy(guests = count.coerceIn(MIN_GUESTS, MAX_GUESTS))
    }

    // Saves the booking and marks the room as booked; null if there is no reservation in progress
    fun confirmBooking(paymentMethod: String): GuestBooking? {
        val database = database()
        val d = draft ?: return null
        val booking = GuestBooking(
            hotelName = d.hotelName,
            roomNumber = d.roomNumber,
            checkIn = d.checkIn,
            checkOut = d.checkOut,
            amountPaid = d.pricePerNight * upfrontPercent / 100,
            reference = "#SB" + (10000..99999).random(),
            guests = d.guests,
            paymentMethod = paymentMethod
        )

        // the booking and the room's new status are saved together
        database.transaction {
            database.insertBooking(currentUser?.phone.orEmpty(), booking)
            database.updateRoomStatus(d.hotelName, d.roomNumber, RoomStatus.Booked)
        }

        val index = rooms.indexOfFirst {
            it.hotelName == d.hotelName && it.room.number == d.roomNumber
        }
        if (index >= 0) {
            val stored = rooms[index]
            rooms[index] = stored.copy(room = stored.room.copy(status = RoomStatus.Booked))
        }

        bookings.add(0, booking)
        lastBooking = booking
        return booking
    }
}