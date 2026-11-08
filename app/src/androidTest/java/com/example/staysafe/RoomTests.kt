package com.example.staysafe

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.staysafe.ui.rooms.RoomStatus
import org.junit.Test

// Test cases 14, 15, 16, 18 and 22: room list and room details
class RoomTests : StaySafeTestBase() {

    private fun openLughanoPark() {
        createHotel(
            "Lughano Park", "Mzuzu",
            listOf(
                room("101"), room("102"),
                room("103", RoomStatus.Booked), room("104", RoomStatus.Booked)
            )
        )
        createGuest()
        logInAsGuest()
        selectCity("Mzuzu")
        openHotel("Lughano Park")
    }

    // Test Case 14
    @Test
    fun ROOMDETAILSSCREEN_DISPLAY_001_room_status_is_shown_for_a_hotel() {
        openLughanoPark()
        assertOnScreen("Vacant Rooms (2)")
        assertOnScreen("Booked Rooms (2)")
        assertOnScreen("Room 101")
        assertOnScreen("Room 102")
        assertNotOnScreen("Room 103")
    }

    // Test Case 15
    @Test
    fun ROOMDETAILS_DISPLAY_002_vacant_rooms_tab_shows_only_vacant_rooms() {
        openLughanoPark()

        composeRule.onAllNodesWithText("Vacant").assertCountEquals(2)   // status labels
        composeRule.onAllNodesWithText("Booked").assertCountEquals(0)
        assertNotOnScreen("Room 103")
        assertNotOnScreen("Room 104")
    }

    // Test Case 16
    @Test
    fun ROOMDETAILS_DISPLAY_003_booked_rooms_tab_shows_only_booked_rooms() {
        openLughanoPark()

        clickText("Booked Rooms (2)")

        assertOnScreen("Room 103")
        assertOnScreen("Room 104")
        assertNotOnScreen("Room 101")
        composeRule.onAllNodesWithText("Booked").assertCountEquals(2)
        composeRule.onAllNodesWithText("Vacant").assertCountEquals(0)
    }

    // Test Case 18
    @Test
    fun ROOMDETAILSSCREEN_DISPLAY_005_room_details_are_shown() {
        createHotel("Lughano Park", "Mzuzu", listOf(room("101", price = 45000)))
        createGuest()
        logInAsGuest()
        selectCity("Mzuzu")
        openHotel("Lughano Park")

        openRoom("101")

        assertOnScreen("Room 101")
        assertOnScreen("Standard Room")
        assertOnScreen("Free WiFi")
        assertOnScreen("Air Conditioning")
        assertOnScreen(mwk(45000))
    }

    // Test Case 22
    @Test
    fun ROOMLISTSCREEN_SELECTROOM_001_a_booked_room_cannot_be_opened() {
        createHotel(
            "Grand Palace Hotel", "Mzuzu",
            listOf(room("101"), room("102", RoomStatus.Booked))
        )
        createGuest()
        logInAsGuest()
        selectCity("Mzuzu")
        openHotel("Grand Palace Hotel")
        clickText("Booked Rooms (1)")

        composeRule.onNodeWithText("Room 102").performClick()   // tapping a booked room does nothing
        composeRule.waitForIdle()

        assertNotOnScreen("Reserve Room")                       // the details screen did not open
        assertOnScreen("Booked Rooms (1)")                      // still on the room list
    }
}