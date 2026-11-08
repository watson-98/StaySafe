package com.example.staysafe

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.staysafe.data.HotelRepository
import com.example.staysafe.ui.login.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

// Test cases 5 to 11: login
class LoginTests : StaySafeTestBase() {

    // Test Case 5
    @Test
    fun LOGINSCREEN_LOGIN_001_guest_logs_in_with_valid_credentials() {
        createGuest()                                           // registered guest account

        logIn(UserRole.Guest, GUEST_PHONE, GUEST_PASSWORD)

        composeRule.onNodeWithTag("home_list").assertExists()   // guest Home screen
        assertNotNull("a session should be created", HotelRepository.currentUser)
        assertEquals(UserRole.Guest, HotelRepository.currentUser?.role)
    }

    // Test Case 6
    @Test
    fun LOGINSCREEN_LOGIN_002_hotel_owner_logs_in_with_valid_credentials() {
        createHotel(
            name = "Test Hotel", city = "Lilongwe",
            ownerPhone = "0880667711", ownerPassword = "@Password"
        )

        logIn(UserRole.HotelOwner, "0880667711", "@Password")

        assertOnScreen("My Hotel")                              // owner dashboard
        assertEquals(UserRole.HotelOwner, HotelRepository.currentUser?.role)
    }

    // Test Case 7
    @Test
    fun LOGINSCREEN_LOGIN_003_incorrect_phone_number() {
        createGuest()

        logIn(UserRole.Guest, "0887667711", GUEST_PASSWORD)     // the number is not registered

        composeRule.onNodeWithText("incorrect", substring = true, ignoreCase = true).assertExists()
        assertOnScreen("Log In")                                // still on the login screen
        assertNull(HotelRepository.currentUser)
    }

    // Test Case 8
    @Test
    fun LOGINSCREEN_LOGIN_004_incorrect_password() {
        createGuest()

        logIn(UserRole.Guest, GUEST_PHONE, "@WrongPassword")

        composeRule.onNodeWithText("incorrect", substring = true, ignoreCase = true).assertExists()
        assertOnScreen("Log In")
        assertNull(HotelRepository.currentUser)
    }

    // Test Case 9
    @Test
    fun LOGINSCREEN_LOGIN_005_phone_number_only() {
        createGuest()

        enterLogin(UserRole.Guest, GUEST_PHONE, password = "")

        composeRule.onNodeWithText("Log In").assertIsNotEnabled()
        composeRule.onNodeWithText("Log In").performClick()     // tapping it does nothing
        assertOnScreen("Log In")
        assertNull(HotelRepository.currentUser)
    }

    // Test Case 10
    @Test
    fun LOGINSCREEN_LOGIN_006_both_fields_empty() {
        createGuest()

        enterLogin(UserRole.Guest, phone = "", password = "")

        composeRule.onNodeWithText("Log In").assertIsNotEnabled()
        composeRule.onNodeWithText("Log In").performClick()
        assertOnScreen("Log In")
        assertNull(HotelRepository.currentUser)
    }

    // Test Case 11
    @Test
    fun LOGINSCREEN_LOGIN_007_password_only() {
        createGuest()

        enterLogin(UserRole.HotelOwner, phone = "", password = "@CorrectPassword")

        composeRule.onNodeWithText("Log In").assertIsNotEnabled()
        composeRule.onNodeWithText("Log In").performClick()
        assertOnScreen("Log In")
        assertNull(HotelRepository.currentUser)
    }
}