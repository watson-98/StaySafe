package com.example.staysafe

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onNodeWithText
import org.junit.Test

class HomeTests : StaySafeTestBase() {

    @Test
    fun HOMESCREEN_SEARCH_001_search_for_a_registered_hotel() {
        createHotel("Grand Palace Hotel", "Mzuzu", listOf(room("101")))
        createHotel(
            "Lughano Park",
            "Mzuzu",
            listOf(room("101")),
            ownerPhone = OTHER_OWNER_PHONE
        )
        createGuest()
        logInAsGuest()

        selectCity("Mzuzu")
        typeInto("search_field", "Grand Palace Hotel")

        composeRule.onAllNodes(hotelRow("Grand Palace Hotel"))
            .assertCountEquals(1)
        composeRule.onAllNodes(hotelRow("Lughano Park"))
            .assertCountEquals(0)
    }

    @Test
    fun HOMESCREEN_SEARCH_002_search_for_a_hotel_that_does_not_exist() {
        createHotel("Grand Palace Hotel", "Mzuzu", listOf(room("101")))
        createGuest()
        logInAsGuest()

        selectCity("Mzuzu")
        typeInto("search_field", "12345")

        composeRule.onNodeWithText("No hotels match that name").assertExists()
        composeRule.onAllNodes(hotelRow("Grand Palace Hotel"))
            .assertCountEquals(0)
    }

    @Test
    fun HOMESCREEN_DISPLAY_004_hotels_change_with_the_selected_city() {
        createHotel("Grand Palace Hotel", "Mzuzu", listOf(room("101")))
        createHotel(
            "Old Nice Hotel",
            "Zomba",
            listOf(room("101")),
            ownerPhone = OTHER_OWNER_PHONE
        )
        createGuest()
        logInAsGuest()

        selectCity("Mzuzu")

        composeRule.onAllNodes(hotelRow("Grand Palace Hotel"))
            .assertCountEquals(1)
        composeRule.onAllNodes(hotelRow("Old Nice Hotel"))
            .assertCountEquals(0)
    }
}