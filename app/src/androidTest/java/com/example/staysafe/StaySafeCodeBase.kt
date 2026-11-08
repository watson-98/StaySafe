package com.example.staysafe

import android.content.Context
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider
import com.example.staysafe.data.HotelRepository
import com.example.staysafe.data.StaySafeDatabase
import com.example.staysafe.ui.login.SignUpData
import com.example.staysafe.ui.login.UserRole
import com.example.staysafe.ui.rooms.Room
import com.example.staysafe.ui.rooms.RoomStatus
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import org.junit.After

open class StaySafeTestBase {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    companion object {
        const val GUEST_PHONE = "0880000000"
        const val GUEST_PASSWORD = "@Password"
        const val OTHER_OWNER_PHONE = "0889999999"
    }

    @Before
    fun setUpBase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        HotelRepository.resetForTests(context)
    }
    @After
    fun cleanUp() {
        HotelRepository.resetForTests(
            androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext
        )
    }

    fun room(
        number: String,
        status: RoomStatus = RoomStatus.Vacant,
        price: Int = 30000,
        wifi: Boolean = true,
        ac: Boolean = true,
        type: String = "Standard Room"
    ): Room = Room(
        number = number,
        type = type,
        status = status,
        pricePerNight = price,
        hasWifi = wifi,
        hasAirConditioning = ac
    )

    fun createHotel(
        name: String,
        city: String,
        rooms: List<Room> = emptyList(),
        ownerPhone: String = "0880112233",
        ownerPassword: String = "@Password",
        ownerName: String = "Hotel Owner",
        location: String = city
    ) {
        val signUp = SignUpData(
            fullName = ownerName,
            phone = ownerPhone,
            password = ownerPassword,
            role = UserRole.HotelOwner,
            hotelName = name,
            city = city
        )

        HotelRepository.register(signUp)
        HotelRepository.logout()

        for (r in rooms) {
            HotelRepository.addRoom(name, r)
        }
    }

    fun createGuest(
        phone: String = GUEST_PHONE,
        password: String = GUEST_PASSWORD,
        fullName: String = "Watson Silumbu"
    ) {
        val signUp = SignUpData(
            fullName = fullName,
            phone = phone,
            password = password,
            role = UserRole.Guest
        )

        HotelRepository.register(signUp)
        HotelRepository.logout()
    }

    fun enterLogin(role: UserRole, phone: String, password: String) {
        if (role == UserRole.Guest) {
            clickText("Guest")
        } else {
            clickText("Hotel Owner")
        }

        typeInto("login_phone", phone)
        typeInto("login_password", password)
    }

    fun logIn(role: UserRole, phone: String, password: String) {
        enterLogin(role, phone, password)
        clickText("Log In")
    }

    fun logInAsGuest(
        phone: String = GUEST_PHONE,
        password: String = GUEST_PASSWORD
    ) {
        logIn(UserRole.Guest, phone, password)
    }

    fun logInAsOwner(
        phone: String = "0880112233",
        password: String = "@Password"
    ) {
        logIn(UserRole.HotelOwner, phone, password)
    }

    fun selectCity(cityName: String) {
        clickTag("city_selector")
        clickTag("city_option_$cityName")
    }

    fun openHotel(hotelName: String) {
        clickText(hotelName)
    }

    fun openRoom(roomNumber: String) {
        clickText("Room $roomNumber")
    }

    fun bookRoom(
        roomNumber: String,
        paymentMethod: String = "Airtel Money"
    ) {
        openRoom(roomNumber)
        clickText("Reserve Room")
        clickText("Proceed to Payment")
        clickText(paymentMethod)
        clickText("Pay Now")
    }

    fun fillAddRoom(
        number: String,
        type: String = "Standard Room",
        price: String = "50000"
    ) {
        typeInto("addroom_number", number)
        clickTag("room_type_field")
        clickTag("room_type_option_$type")
        typeInto("addroom_price", price)
    }

    fun clickText(
        text: String,
        substring: Boolean = false,
        ignoreCase: Boolean = false
    ) {
        composeRule.onNodeWithText(
            text,
            substring = substring,
            ignoreCase = ignoreCase
        ).performClick()

        composeRule.waitForIdle()
    }

    fun clickTag(tag: String) {
        composeRule.onNodeWithTag(tag).performClick()
        composeRule.waitForIdle()
    }

    fun typeInto(tag: String, text: String) {
        composeRule.onNodeWithTag(tag).performTextInput(text)
        composeRule.waitForIdle()
    }

    fun assertOnScreen(text: String) {
        val nodes = composeRule.onAllNodesWithText(
            text,
            substring = true,
            ignoreCase = true
        ).fetchSemanticsNodes()

        assertTrue(
            "Expected '$text' to exist on the screen",
            nodes.isNotEmpty()
        )
    }

    fun assertNotOnScreen(text: String) {
        val nodes = composeRule.onAllNodesWithText(
            text,
            substring = true,
            ignoreCase = true
        ).fetchSemanticsNodes()

        assertTrue(
            "Expected '$text' not to exist on the screen",
            nodes.isEmpty()
        )
    }

    fun mwk(amount: Int): String = "MWK ${"%,d".format(amount)}"

    fun dateText(daysFromToday: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, daysFromToday)

        return SimpleDateFormat(
            "dd MMM yyyy",
            Locale.ENGLISH
        ).format(calendar.time)
    }

    fun hotelRow(name: String): SemanticsMatcher =
        hasTestTag("hotel_row_$name")

    fun textOf(tag: String): String {
        val node = composeRule.onNodeWithTag(tag).fetchSemanticsNode()

        return node.config[SemanticsProperties.Text]
            .joinToString("") { it.text }
    }

    fun <T> withDatabase(block: (StaySafeDatabase) -> T): T {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = StaySafeDatabase(context, StaySafeDatabase.TEST_NAME)

        return try {
            block(db)
        } finally {
            db.close()
        }
    }
}