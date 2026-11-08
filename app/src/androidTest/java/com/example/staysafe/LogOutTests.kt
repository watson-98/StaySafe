package com.example.staysafe

import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.example.staysafe.data.HotelRepository
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LogoutTests : StaySafeTestBase() {
    private fun assertBackClosesTheApp() {
        val activity = composeRule.activity
        composeRule.runOnUiThread { activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()
        assertTrue("the app should close", activity.isFinishing)
    }

    @Test
    fun HOMESCREEN_LOGOUT_001_guest_logs_out_and_back_closes_the_app() {
        createGuest()
        logInAsGuest()
        composeRule.onNodeWithContentDescription("Profile").performClick()   // open the Profile tab
        composeRule.waitForIdle()
        clickText("Logout")

        assertOnScreen("Log In")                                // back on the login screen
        assertNull(HotelRepository.currentUser)                 // the session is cleared
        assertBackClosesTheApp()
    }
    @Test
    fun HOMESCREEN_LOGOUT_002_owner_logs_out_and_back_closes_the_app() {
        createHotel("Mango Lodge", "Zomba")
        logInAsOwner()

        composeRule.onNodeWithContentDescription("Profile").performClick()   // open the owner's profile
        composeRule.waitForIdle()
        clickText("Logout")

        assertOnScreen("Log In")
        assertNull(HotelRepository.currentUser)
        assertBackClosesTheApp()
    }
}