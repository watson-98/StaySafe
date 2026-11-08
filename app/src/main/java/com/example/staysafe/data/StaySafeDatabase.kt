package com.example.staysafe.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Base64
import com.example.staysafe.ui.login.UserRole
import com.example.staysafe.ui.rooms.Room
import com.example.staysafe.ui.rooms.RoomStatus
import java.io.IOException
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

data class User(
    val phone: String,
    val fullName: String,
    val role: UserRole,
    val hotelName: String?
)

data class UserRecord(val user: User, val passwordHash: String, val salt: String)

data class PaymentMethodInfo(val name: String, val shortLabel: String, val color: String)

object PasswordHasher {
    private const val ITERATIONS = 10_000

    fun newSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    fun hash(password: String, salt: String): String {
        val spec = PBEKeySpec(password.toCharArray(), Base64.decode(salt, Base64.NO_WRAP), ITERATIONS, 256)
        val hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec).encoded
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }

    fun matches(password: String, salt: String, expectedHash: String): Boolean =
        MessageDigest.isEqual(hash(password, salt).toByteArray(), expectedHash.toByteArray())
}

class StaySafeDatabase(
    private val context: Context,
    name: String = NAME
) : SQLiteOpenHelper(context, name, null, 2) {

    companion object {
        const val NAME = "staysafe.db"
        const val TEST_NAME = "staysafe_test.db"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE users (
                phone TEXT PRIMARY KEY,
                full_name TEXT NOT NULL,
                role TEXT NOT NULL,
                password_hash TEXT NOT NULL,
                salt TEXT NOT NULL,
                hotel_name TEXT
            )"""
        )
        db.execSQL(
            """CREATE TABLE hotels (
                name TEXT PRIMARY KEY COLLATE NOCASE,
                location TEXT NOT NULL,
                city TEXT NOT NULL,
                owner_phone TEXT
            )"""
        )
        db.execSQL(
            """CREATE TABLE rooms (
                hotel_name TEXT NOT NULL,
                number TEXT NOT NULL,
                type TEXT NOT NULL,
                status TEXT NOT NULL,
                price INTEGER NOT NULL,
                wifi INTEGER NOT NULL,
                ac INTEGER NOT NULL,
                PRIMARY KEY (hotel_name, number)
            )"""
        )
        db.execSQL(
            """CREATE TABLE bookings (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                guest_phone TEXT NOT NULL,
                hotel_name TEXT NOT NULL,
                room_number TEXT NOT NULL,
                check_in TEXT NOT NULL,
                check_out TEXT NOT NULL,
                amount_paid INTEGER NOT NULL,
                reference TEXT NOT NULL,
                guests INTEGER NOT NULL,
                payment_method TEXT NOT NULL
            )"""
        )
        db.execSQL("CREATE TABLE cities (name TEXT PRIMARY KEY)")
        db.execSQL("CREATE TABLE room_types (name TEXT PRIMARY KEY)")
        db.execSQL(
            """CREATE TABLE payment_methods (
                name TEXT PRIMARY KEY,
                short_label TEXT NOT NULL,
                color TEXT NOT NULL
            )"""
        )
        db.execSQL("CREATE TABLE settings (key TEXT PRIMARY KEY, value TEXT NOT NULL)")

        runSeedScript(db)
    }

    // Fills the reference tables from assets/seed.sql
    private fun runSeedScript(db: SQLiteDatabase) {
        val script = try {
            context.assets.open("seed.sql").bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            throw IllegalStateException("app/src/main/assets/seed.sql is missing", e)
        }
        script.lines()
            .filterNot { it.trimStart().startsWith("--") }
            .joinToString("\n")
            .split(";")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .forEach { db.execSQL(it) }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS settings")
        db.execSQL("DROP TABLE IF EXISTS payment_methods")
        db.execSQL("DROP TABLE IF EXISTS room_types")
        db.execSQL("DROP TABLE IF EXISTS cities")
        db.execSQL("DROP TABLE IF EXISTS bookings")
        db.execSQL("DROP TABLE IF EXISTS rooms")
        db.execSQL("DROP TABLE IF EXISTS hotels")
        db.execSQL("DROP TABLE IF EXISTS users")
        onCreate(db)
    }

    fun <T> transaction(block: () -> T): T {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val result = block()
            db.setTransactionSuccessful()
            return result
        } finally {
            db.endTransaction()
        }
    }

    // users

    fun findUser(phone: String): UserRecord? =
        readableDatabase.rawQuery(
            "SELECT phone, full_name, role, password_hash, salt, hotel_name FROM users WHERE phone = ?",
            arrayOf(phone)
        ).use { c ->
            if (c.moveToFirst()) {
                val user = User(
                    phone = c.getString(0),
                    fullName = c.getString(1),
                    role = UserRole.valueOf(c.getString(2)),
                    hotelName = if (c.isNull(5)) null else c.getString(5)
                )
                UserRecord(user, c.getString(3), c.getString(4))
            } else {
                null
            }
        }

    fun insertUser(user: User, passwordHash: String, salt: String) {
        val values = ContentValues().apply {
            put("phone", user.phone)
            put("full_name", user.fullName)
            put("role", user.role.name)
            put("password_hash", passwordHash)
            put("salt", salt)
            put("hotel_name", user.hotelName)
        }
        writableDatabase.insertOrThrow("users", null, values)
    }

    // reference data
    private fun names(sql: String): List<String> {
        val list = mutableListOf<String>()
        readableDatabase.rawQuery(sql, null).use { c -> while (c.moveToNext()) list.add(c.getString(0)) }
        return list
    }

    fun allCities(): List<String> = names("SELECT name FROM cities ORDER BY rowid")

    fun allRoomTypes(): List<String> = names("SELECT name FROM room_types ORDER BY rowid")

    fun allPaymentMethods(): List<PaymentMethodInfo> {
        val methods = mutableListOf<PaymentMethodInfo>()
        readableDatabase.rawQuery(
            "SELECT name, short_label, color FROM payment_methods ORDER BY rowid", null
        ).use { c ->
            while (c.moveToNext()) methods.add(PaymentMethodInfo(c.getString(0), c.getString(1), c.getString(2)))
        }
        return methods
    }

    fun setting(key: String): String? =
        readableDatabase.rawQuery("SELECT value FROM settings WHERE key = ?", arrayOf(key)).use {
            if (it.moveToFirst()) it.getString(0) else null
        }

    //  hotels

    fun hotelExists(name: String): Boolean =
        readableDatabase.rawQuery("SELECT 1 FROM hotels WHERE name = ?", arrayOf(name)).use { it.moveToFirst() }

    fun allHotels(): List<Hotel> {
        val hotels = mutableListOf<Hotel>()
        readableDatabase.rawQuery(
            "SELECT name, location, city FROM hotels ORDER BY rowid", null
        ).use { c ->
            while (c.moveToNext()) hotels.add(Hotel(c.getString(0), c.getString(1), c.getString(2)))
        }
        return hotels
    }

    fun insertHotel(hotel: Hotel, ownerPhone: String?) {
        val values = ContentValues().apply {
            put("name", hotel.name)
            put("location", hotel.location)
            put("city", hotel.city)
            put("owner_phone", ownerPhone)
        }
        writableDatabase.insertOrThrow("hotels", null, values)
    }

    // rooms

    fun allRooms(): List<Pair<String, Room>> {
        val rooms = mutableListOf<Pair<String, Room>>()
        readableDatabase.rawQuery(
            "SELECT hotel_name, number, type, status, price, wifi, ac FROM rooms ORDER BY rowid", null
        ).use { c ->
            while (c.moveToNext()) {
                rooms.add(
                    c.getString(0) to Room(
                        number = c.getString(1),
                        type = c.getString(2),
                        status = RoomStatus.valueOf(c.getString(3)),
                        pricePerNight = c.getInt(4),
                        hasWifi = c.getInt(5) == 1,
                        hasAirConditioning = c.getInt(6) == 1
                    )
                )
            }
        }
        return rooms
    }

    // Returns false if the hotel already has a room with this number
    fun insertRoom(hotelName: String, room: Room): Boolean {
        val values = ContentValues().apply {
            put("hotel_name", hotelName)
            put("number", room.number)
            put("type", room.type)
            put("status", room.status.name)
            put("price", room.pricePerNight)
            put("wifi", if (room.hasWifi) 1 else 0)
            put("ac", if (room.hasAirConditioning) 1 else 0)
        }
        return writableDatabase.insertWithOnConflict(
            "rooms", null, values, SQLiteDatabase.CONFLICT_IGNORE
        ) != -1L
    }

    fun updateRoomStatus(hotelName: String, roomNumber: String, status: RoomStatus) {
        val values = ContentValues().apply { put("status", status.name) }
        writableDatabase.update(
            "rooms", values, "hotel_name = ? AND number = ?", arrayOf(hotelName, roomNumber)
        )
    }

    //  bookings

    fun insertBooking(guestPhone: String, booking: GuestBooking) {
        val values = ContentValues().apply {
            put("guest_phone", guestPhone)
            put("hotel_name", booking.hotelName)
            put("room_number", booking.roomNumber)
            put("check_in", booking.checkIn)
            put("check_out", booking.checkOut)
            put("amount_paid", booking.amountPaid)
            put("reference", booking.reference)
            put("guests", booking.guests)
            put("payment_method", booking.paymentMethod)
        }
        writableDatabase.insertOrThrow("bookings", null, values)
    }


    fun bookingsFor(guestPhone: String): List<GuestBooking> {
        val bookings = mutableListOf<GuestBooking>()
        readableDatabase.rawQuery(
            "SELECT hotel_name, room_number, check_in, check_out, amount_paid, reference, guests, payment_method " +
                    "FROM bookings WHERE guest_phone = ? ORDER BY id DESC",
            arrayOf(guestPhone)
        ).use { c ->
            while (c.moveToNext()) {
                bookings.add(
                    GuestBooking(
                        hotelName = c.getString(0),
                        roomNumber = c.getString(1),
                        checkIn = c.getString(2),
                        checkOut = c.getString(3),
                        amountPaid = c.getInt(4),
                        reference = c.getString(5),
                        guests = c.getInt(6),
                        paymentMethod = c.getString(7)
                    )
                )
            }
        }
        return bookings
    }
}