package com.example.staysafe

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.example.staysafe.data.HotelRepository
import com.example.staysafe.ui.rooms.RoomStatus
import org.junit.Assert.assertEquals
import org.junit.Test

// Test cases 19, 20, 21 and 23: payment, booking and the guest's booking history
class BookingTests : StaySafeTestBase() {

    private val hotel = "Grand Palace Hotel"


    private fun openHotelRoomList() {
        createHotel(hotel, "Mzuzu", listOf(room("101", price = 45000), room("102", RoomStatus.Booked)))
        createGuest()
        logInAsGuest()
        selectCity("Mzuzu")
        openHotel(hotel)
    }

    private fun reachPaymentScreen() {
        openHotelRoomList()
        openRoom("101")
        clickText("Reserve Room")
        clickText("Proceed to Payment")
    }

    // Test Case 19
    @Test
    fun PAYMENTSCREEN_PAYMENT_001_upfront_amount_is_twenty_percent_of_the_room_price() {
        reachPaymentScreen()

        assertOnScreen(mwk(45000))                      // total room price
        assertOnScreen(mwk(9000))                       // 20% upfront amount
        assertOnScreen("Pay an upfront amount (20%)")
    }

    // Test Case 20
    @Test
    fun PAYMENTSCREEN_PAYMENT_002_a_payment_method_can_be_selected() {
        reachPaymentScreen()
        composeRule.onNodeWithText("Pay Now").assertIsNotEnabled()      // nothing selected yet

        clickText("Airtel Money")

        composeRule.onNodeWithTag("radio_Airtel Money", useUnmergedTree = true).assertIsSelected()
        composeRule.onNodeWithText("Pay Now").assertIsEnabled()
    }

    // Test Case 21
    @Test
    fun BOOKINGCONFIRMATIONSCREEN_BOOKING_001_a_vacant_room_can_be_booked() {
        openHotelRoomList()

        bookRoom("101", "Airtel Money")

        assertOnScreen("Booking Successful!")
        val room = HotelRepository.roomsOf(hotel).first { it.number == "101" }
        assertEquals(RoomStatus.Booked, room.status)
        val savedRoom = withDatabase { db -> db.allRooms().first { it.first == hotel && it.second.number == "101" } }
        assertEquals(RoomStatus.Booked, savedRoom.second.status)
        val bookings = withDatabase { it.bookingsFor(GUEST_PHONE) }
        assertEquals(1, bookings.size)
        assertEquals("101", bookings[0].roomNumber)
        assertEquals(hotel, bookings[0].hotelName)
        assertEquals("Airtel Money", bookings[0].paymentMethod)
    }

    // Test Case 23
    @Test
    fun HOMESCREEN_VIEW_001_booking_history_is_shown() {
        openHotelRoomList()
        bookRoom("101")

        clickText("Back to Home")
        clickText("Bookings")

        assertOnScreen("My Bookings")
        assertOnScreen(hotel)
        assertOnScreen("Room 101")
        assertOnScreen("Paid: " + mwk(9000))
        assertOnScreen(dateText(0) + " - " + dateText(1))     // check-in and check-out dates
        assertOnScreen("Confirmed")
    }
}