package com.example.staysafe.data

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.staysafe.ui.login.UserRole
import com.example.staysafe.ui.rooms.Room
import com.example.staysafe.ui.rooms.RoomStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Source
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// vacant/booked counts are derived from the room list
data class Hotel(
    val name: String = "",
    val location: String = "",
    val city: String = ""
)

data class GuestBooking(
    val hotelName: String = "",
    val roomNumber: String = "",
    val checkIn: String = "",
    val checkOut: String = "",
    val amountPaid: Int = 0,
    val reference: String = ""
)

// reservation that hasn't been paid for yet
data class BookingDraft(
    val hotelName: String = "Sunset Hotel",
    val roomNumber: String = "101",
    val roomType: String = "Standard Room",
    val pricePerNight: Int = 45000,
    val checkIn: String = dateText(daysFromToday = 0),
    val checkOut: String = dateText(daysFromToday = 1),
    val guests: String = "2 Adults"
)

// Calendar and SimpleDateFormat work on API 24; java.time would need API 26
private fun dateText(daysFromToday: Int): String {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, daysFromToday)
    return SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(calendar.time)
}

val availableCities = listOf("Lilongwe", "Blantyre", "Mzuzu", "Zomba")

private data class StoredRoom(
    val hotelName: String = "",
    val room: Room = Room("", "", RoomStatus.Vacant)
)

const val ROOM_TAKEN_ERROR = "Sorry, this room has just been booked by someone else."

// Store backed by Firebase Firestore with reactive Compose states.
// Listeners only run while a user is logged in (start/stop), so the data is
// never read before login or kept after logout.
object HotelRepository {

    private const val TAG = "HotelRepository"
    private const val STANDARD_PRICE = 45000
    private const val DELUXE_PRICE = 60000
    private const val UPFRONT_PERCENT = 20

    private val db by lazy { FirebaseFirestore.getInstance() }

    val hotels = mutableStateListOf<Hotel>()
    val bookings = mutableStateListOf<GuestBooking>()   // logged-in guest only, newest first
    private val rooms = mutableStateListOf<StoredRoom>()

    // false until the first real result for hotels has arrived
    var hotelsLoaded by mutableStateOf(false)
        private set

    // hotel managed by the logged-in owner (empty for guests)
    var ownerHotelName by mutableStateOf("")
        private set

    // in-progress reservation and the last paid booking
    var draft by mutableStateOf<BookingDraft?>(null)
        private set
    var lastBooking by mutableStateOf<GuestBooking?>(null)
        private set

    private val listeners = mutableListOf<ListenerRegistration>()
    private var activeUid: String? = null

    // document ids can't contain "/"
    private fun hotelDocId(name: String) = name.replace("/", "-")
    private fun roomDocId(hotelName: String, number: String) = "${hotelDocId(hotelName)}_${number.replace("/", "-")}"

    // ---- session ----

    fun start(profile: UserProfile) {
        ownerHotelName = if (profile.role == UserRole.HotelOwner) profile.hotelName else ""
        if (activeUid == profile.uid) return   // already listening for this user

        stop()
        activeUid = profile.uid
        ensureSeeded { }
        listenToHotels()
        listenToRooms()
        listenToBookings(profile.uid)
    }

    fun stop() {
        listeners.forEach { it.remove() }
        listeners.clear()
        hotels.clear()
        rooms.clear()
        bookings.clear()
        hotelsLoaded = false
        ownerHotelName = ""
        draft = null
        lastBooking = null
        activeUid = null
    }

    // ---- Firestore listeners ----

    private fun listenToHotels() {
        listeners += db.collection("hotels").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error listening to hotels", error)
                return@addSnapshotListener
            }
            if (snapshot == null) return@addSnapshotListener
            // an empty cached result may just mean the server hasn't answered yet
            if (snapshot.isEmpty && snapshot.metadata.isFromCache) return@addSnapshotListener

            val fetched = snapshot.documents.mapNotNull { doc ->
                val name = doc.getString("name") ?: return@mapNotNull null
                Hotel(
                    name = name,
                    location = doc.getString("location") ?: "",
                    city = doc.getString("city") ?: ""
                )
            }.sortedBy { it.name }
            hotels.clear()
            hotels.addAll(fetched)
            hotelsLoaded = true
        }
    }

    private fun listenToRooms() {
        listeners += db.collection("rooms").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error listening to rooms", error)
                return@addSnapshotListener
            }
            if (snapshot == null) return@addSnapshotListener

            val fetched = snapshot.documents.mapNotNull { doc ->
                val hotelName = doc.getString("hotelName") ?: return@mapNotNull null
                val number = doc.getString("number") ?: return@mapNotNull null
                val status = if (doc.getString("status") == RoomStatus.Booked.name) {
                    RoomStatus.Booked
                } else {
                    RoomStatus.Vacant
                }
                StoredRoom(
                    hotelName = hotelName,
                    room = Room(
                        number = number,
                        type = doc.getString("type") ?: "Standard Room",
                        status = status,
                        pricePerNight = (doc.getLong("pricePerNight") ?: STANDARD_PRICE.toLong()).toInt(),
                        hasWifi = doc.getBoolean("hasWifi") ?: true,
                        hasAirConditioning = doc.getBoolean("hasAirConditioning") ?: true
                    )
                )
            }.sortedWith(compareBy({ it.hotelName }, { it.room.number.toIntOrNull() ?: Int.MAX_VALUE }, { it.room.number }))
            rooms.clear()
            rooms.addAll(fetched)
        }
    }

    // only this guest's bookings; sorted here so no composite index is needed
    private fun listenToBookings(uid: String) {
        listeners += db.collection("bookings").whereEqualTo("guestId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to bookings", error)
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener

                val fetched = snapshot.documents.mapNotNull { doc ->
                    val hotelName = doc.getString("hotelName") ?: return@mapNotNull null
                    val booking = GuestBooking(
                        hotelName = hotelName,
                        roomNumber = doc.getString("roomNumber") ?: "",
                        checkIn = doc.getString("checkIn") ?: "",
                        checkOut = doc.getString("checkOut") ?: "",
                        amountPaid = (doc.getLong("amountPaid") ?: 0L).toInt(),
                        reference = doc.getString("reference") ?: ""
                    )
                    (doc.getLong("timestamp") ?: 0L) to booking
                }.sortedByDescending { it.first }.map { it.second }
                bookings.clear()
                bookings.addAll(fetched)
            }
    }

    // ---- first-run sample data ----

    // Seeds sample hotels once, only if the server really has no hotels.
    // Never overwrites existing data.
    private fun ensureSeeded(onDone: () -> Unit) {
        db.collection("hotels").limit(1).get(Source.SERVER)
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    seedFirestore().addOnCompleteListener { onDone() }
                } else {
                    onDone()
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Could not check for sample data", e)
                onDone()
            }
    }

    private fun seedFirestore() = db.batch().also { batch ->
        val initialData = listOf(
            Triple("Sunset Hotel", "Lilongwe", Pair(3, 5)),
            Triple("Capital Hill Hotel", "Lilongwe", Pair(4, 6)),
            Triple("Lakeview Lodge", "Lilongwe", Pair(2, 3)),
            Triple("Shire View Lodge", "Blantyre", Pair(5, 4)),
            Triple("Old Nice Hotel", "Zomba", Pair(2, 6)),
            Triple("Mango Lodge", "Zomba", Pair(4, 2)),
            Triple("Grand Palace Hotel", "Mzuzu", Pair(3, 7)),
            Triple("Lughano Park", "Mzuzu", Pair(1, 4))
        )

        for ((name, city, counts) in initialData) {
            val location = if (name == "Lakeview Lodge") "Area 47" else city
            batch.set(
                db.collection("hotels").document(hotelDocId(name)),
                hashMapOf("name" to name, "location" to location, "city" to city)
            )

            val vacant = counts.first
            val booked = counts.second
            for (i in 1..(vacant + booked)) {
                val type = if (i % 3 == 0) "Deluxe Room" else "Standard Room"
                val roomNumber = (100 + i).toString()
                batch.set(
                    db.collection("rooms").document(roomDocId(name, roomNumber)),
                    hashMapOf(
                        "hotelName" to name,
                        "number" to roomNumber,
                        "type" to type,
                        "status" to if (i <= vacant) RoomStatus.Vacant.name else RoomStatus.Booked.name,
                        "pricePerNight" to if (type == "Deluxe Room") DELUXE_PRICE else STANDARD_PRICE,
                        "hasWifi" to true,
                        "hasAirConditioning" to true
                    )
                )
            }
        }
    }.commit()

    // ---- queries ----

    fun findHotel(name: String): Hotel? = hotels.firstOrNull { it.name == name }

    fun roomsOf(hotelName: String): List<Room> =
        rooms.filter { it.hotelName == hotelName }.map { it.room }

    fun vacantCount(hotelName: String): Int =
        rooms.count { it.hotelName == hotelName && it.room.status == RoomStatus.Vacant }

    fun bookedCount(hotelName: String): Int =
        rooms.count { it.hotelName == hotelName && it.room.status == RoomStatus.Booked }

    // falls back to the owner's sign-up details while hotels are still loading
    fun ownerHotel(): Hotel {
        findHotel(ownerHotelName)?.let { return it }
        val city = AuthRepository.currentUser?.city ?: ""
        return Hotel(name = ownerHotelName, location = city, city = city)
    }

    // ---- writes ----

    // creates the owner's hotel unless it already exists (e.g. a sample hotel)
    fun registerOwnerHotel(name: String, city: String) {
        ensureSeeded {
            val ref = db.collection("hotels").document(hotelDocId(name))
            ref.get()
                .addOnSuccessListener { doc ->
                    if (!doc.exists()) {
                        ref.set(hashMapOf("name" to name, "location" to city, "city" to city))
                    }
                }
                .addOnFailureListener { Log.e(TAG, "Could not register hotel", it) }
        }
    }

    // returns false if the room number already exists
    fun addRoom(hotelName: String, room: Room): Boolean {
        if (rooms.any { it.hotelName == hotelName && it.room.number == room.number }) return false

        db.collection("rooms").document(roomDocId(hotelName, room.number)).set(
            hashMapOf(
                "hotelName" to hotelName,
                "number" to room.number,
                "type" to room.type,
                "status" to room.status.name,
                "pricePerNight" to room.pricePerNight,
                "hasWifi" to room.hasWifi,
                "hasAirConditioning" to room.hasAirConditioning
            )
        ).addOnFailureListener { Log.e(TAG, "Could not add room", it) }
        return true
    }

    fun startBooking(hotelName: String, room: Room) {
        draft = BookingDraft(
            hotelName = hotelName,
            roomNumber = room.number,
            roomType = room.type,
            pricePerNight = room.pricePerNight
        )
    }

    // Marks the room as booked and saves the booking in one transaction, so two
    // guests can't book the same room. onResult gets the booking or an error message.
    fun confirmBooking(onResult: (GuestBooking?, String?) -> Unit) {
        val uid = activeUid
        if (uid == null) {
            onResult(null, "Please log in again.")
            return
        }
        val d = draft ?: BookingDraft()
        val booking = GuestBooking(
            hotelName = d.hotelName,
            roomNumber = d.roomNumber,
            checkIn = d.checkIn,
            checkOut = d.checkOut,
            amountPaid = d.pricePerNight * UPFRONT_PERCENT / 100,
            reference = "#SB" + (10000..99999).random()
        )

        val roomRef = db.collection("rooms").document(roomDocId(d.hotelName, d.roomNumber))
        val bookingRef = db.collection("bookings").document()
        val bookingMap = hashMapOf(
            "guestId" to uid,
            "hotelName" to booking.hotelName,
            "roomNumber" to booking.roomNumber,
            "checkIn" to booking.checkIn,
            "checkOut" to booking.checkOut,
            "amountPaid" to booking.amountPaid,
            "reference" to booking.reference,
            "timestamp" to System.currentTimeMillis()
        )

        db.runTransaction { transaction ->
            val roomSnapshot = transaction.get(roomRef)
            if (roomSnapshot.getString("status") == RoomStatus.Booked.name) {
                throw FirebaseFirestoreException(
                    ROOM_TAKEN_ERROR,
                    FirebaseFirestoreException.Code.ABORTED
                )
            }
            transaction.update(roomRef, "status", RoomStatus.Booked.name)
            transaction.set(bookingRef, bookingMap)
            null
        }.addOnSuccessListener {
            lastBooking = booking
            onResult(booking, null)
        }.addOnFailureListener { e ->
            Log.e(TAG, "Booking failed", e)
            val taken = e is FirebaseFirestoreException &&
                    e.code == FirebaseFirestoreException.Code.ABORTED
            onResult(
                null,
                if (taken) ROOM_TAKEN_ERROR
                else "Could not complete the booking. Check your connection and try again."
            )
        }
    }
}
