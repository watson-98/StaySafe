package com.example.staysafe

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.example.staysafe.data.HotelRepository
import com.example.staysafe.ui.login.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

// Test cases 1 to 4: sign-up
class SignUpTests : StaySafeTestBase() {

    private val phone = "0880667711"

    // Test Case 1
    @Test
    fun SIGNUPSCREEN_SIGNUP_001_guest_can_sign_up() {
        clickText("Sign Up")
        clickText("Guest")
        typeInto("signup_name", "Watson Silumbu")
        typeInto("signup_phone", phone)
        typeInto("signup_password", "@Password")
        typeInto("signup_confirm", "@Password")

        composeRule.onNodeWithText("Sign Up").assertIsEnabled()
        clickText("Sign Up")

        // direct guest to Home screen and save account in the database
        composeRule.onNodeWithTag("home_list").assertExists()
        assertEquals(phone, HotelRepository.currentUser?.phone)
        assertEquals(UserRole.Guest, HotelRepository.currentUser?.role)
        val saved = withDatabase { it.findUser(phone) }
        assertNotNull("the guest account should be saved", saved)
        assertEquals("Watson Silumbu", saved!!.user.fullName)
    }

    // Test Case 2
    @Test
    fun SIGNUPSCREEN_SIGNUP_002_hotel_owner_can_sign_up() {
        clickText("Sign Up")
        clickText("Hotel Owner")
        typeInto("signup_name", "Watson Silumbu")
        typeInto("signup_phone", phone)
        typeInto("signup_hotel", "Mango Lodge")
        clickTag("signup_city")
        clickTag("signup_city_option_Zomba")
        typeInto("signup_password", "@Password")
        typeInto("signup_confirm", "@Password")

        composeRule.onNodeWithText("Sign Up").assertIsEnabled()
        clickText("Sign Up")

        // direct owner to dashboard and save account in the database
        assertOnScreen("My Hotel")
        assertOnScreen("Mango Lodge")
        assertEquals(UserRole.HotelOwner, HotelRepository.currentUser?.role)
        assertEquals("Zomba", HotelRepository.findHotel("Mango Lodge")?.city)
        val saved = withDatabase { it.findUser(phone) }
        assertNotNull("the owner account should be saved", saved)
        assertEquals("Mango Lodge", saved!!.user.hotelName)
    }

    // Test Case 3
    @Test
    fun SIGNUPSCREEN_SIGNUP_003_password_and_confirm_password_do_not_match() {
        clickText("Sign Up")
        clickText("Guest")
        typeInto("signup_name", "Watson Silumbu")
        typeInto("signup_phone", phone)
        typeInto("signup_password", "@Password")
        typeInto("signup_confirm", "password")

        // error message shown, button stays disabled, user stays on the sign-up screen
        composeRule.onNodeWithText("passwords do not match", ignoreCase = true).assertExists()
        composeRule.onNodeWithText("Sign Up").assertIsNotEnabled()
        assertOnScreen("Create Account")
        assertNull("no account should be created", withDatabase { it.findUser(phone) })
        assertNull(HotelRepository.currentUser)
    }

    // Test Case 4
    @Test
    fun SIGNUPSCREEN_SIGNUP_004_hotel_owner_sign_up_with_empty_hotel_name() {
        clickText("Sign Up")
        clickText("Hotel Owner")
        typeInto("signup_name", "Watson Silumbu")
        typeInto("signup_phone", phone)
        // the Hotel Name field is left empty
        clickTag("signup_city")
        clickTag("signup_city_option_Mzuzu")
        typeInto("signup_password", "@Password")
        typeInto("signup_confirm", "@Password")

        composeRule.onNodeWithText("Sign Up").assertIsNotEnabled()
        assertOnScreen("Create Account")
        assertNull("no account should be created", withDatabase { it.findUser(phone) })
        assertNull(HotelRepository.currentUser)
    }
}