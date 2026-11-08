package com.example.staysafe

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.example.staysafe.data.HotelRepository
import com.example.staysafe.ui.rooms.RoomStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OwnerTests : StaySafeTestBase() {

    private val hotel = "Mango Lodge"
    private val duplicateMessage =
        "A room already exists with the same Room Number, please enter a different room number"

    private fun logInAsOwnerOfMangoLodge() {
        createHotel(
            hotel,
            "Zomba",
            listOf(room("101"), room("102", RoomStatus.Booked))
        )
        logInAsOwner()
    }

    @Test
    fun ADDROOMSCREEN_ADDROOM_001_owner_adds_a_valid_room() {
        logInAsOwnerOfMangoLodge()
        val vacantBefore = HotelRepository.vacantCount(hotel)

        clickTag("menu_Add Room")
        fillAddRoom(number = "110", type = "Standard Room", price = "50000")
        clickText("Save Room")

        composeRule.onNodeWithTag("count_Vacant Rooms")
            .assertTextEquals((vacantBefore + 1).toString())

        val added = HotelRepository.roomsOf(hotel).first { it.number == "110" }

        assertEquals(RoomStatus.Vacant, added.status)
        assertEquals("Standard Room", added.type)
        assertEquals(50000, added.pricePerNight)
        assertTrue(added.hasWifi && added.hasAirConditioning)
    }

    @Test
    fun ADDROOMSCREEN_ADDROOM_002_a_room_price_of_zero_is_rejected() {
        logInAsOwnerOfMangoLodge()

        clickTag("menu_Add Room")
        fillAddRoom(number = "112", type = "Standard Room", price = "0")

        composeRule.onNodeWithText("Price must be a positive number").assertExists()
        composeRule.onNodeWithText("Save Room").assertIsNotEnabled()
        assertOnScreen("Add New Room")

        assertTrue(HotelRepository.roomsOf(hotel).none { it.number == "112" })
    }

    @Test
    fun ADDROOMSCREEN_ADDROOM_003_a_duplicate_room_number_is_rejected() {
        logInAsOwnerOfMangoLodge()
        val roomsBefore = HotelRepository.roomsOf(hotel).size

        clickTag("menu_Add Room")
        fillAddRoom(number = "101", type = "Standard Room", price = "50000")

        composeRule.onNodeWithText(duplicateMessage).assertExists()
        composeRule.onNodeWithText("Save Room").assertIsNotEnabled()
        assertOnScreen("Add New Room")

        assertEquals(roomsBefore, HotelRepository.roomsOf(hotel).size)
    }

    @Test
    fun OWNERROOMLISTSCREEN_ADDROOM_004_a_room_added_by_the_owner_is_visible_to_guests() {
        createGuest()
        logInAsOwnerOfMangoLodge()

        clickTag("menu_Add Room")
        fillAddRoom(number = "109", type = "Standard Room", price = "50000")
        clickText("Save Room")

        clickTag("menu_Logout")
        logInAsGuest()
        selectCity("Zomba")
        openHotel(hotel)

        assertOnScreen("Vacant Rooms (2)")
        assertOnScreen("Room 109")
    }

    @Test
    fun OWNERDASHBOARDSCREEN_MATCH_001_dashboard_counts_match_the_room_list() {
        createHotel(
            hotel,
            "Zomba",
            listOf(
                room("101"),
                room("102"),
                room("103"),
                room("104", RoomStatus.Booked),
                room("105", RoomStatus.Booked)
            )
        )
        logInAsOwner()

        val vacantOnDashboard = textOf("count_Vacant Rooms")
        val bookedOnDashboard = textOf("count_Booked Rooms")

        clickTag("menu_Rooms")

        assertEquals("3", vacantOnDashboard)
        assertEquals("2", bookedOnDashboard)
        assertOnScreen("Vacant ($vacantOnDashboard)")
        assertOnScreen("Booked ($bookedOnDashboard)")
    }
}