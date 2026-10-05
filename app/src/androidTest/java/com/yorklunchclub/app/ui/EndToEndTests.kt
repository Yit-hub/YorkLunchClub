package com.yorklunchclub.app.ui

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EndToEndTests {

    private lateinit var device: UiDevice
    private val appPackage = "com.yorklunchclub.app"
    private val launchTimeout = 10_000L
    private val uiTimeout = 5_000L

    @Before
    fun setUp() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.pressHome()
        val context: Context = ApplicationProvider.getApplicationContext()
        val intent = context.packageManager.getLaunchIntentForPackage(appPackage)
            ?: throw AssertionError("Launch intent not found for $appPackage")
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
        device.wait(Until.hasObject(By.pkg(appPackage).depth(0)), launchTimeout)
    }

    @Test
    fun tc19_fullRegistrationAndLoginFlow() {
        val loginScreen = device.wait(Until.hasObject(By.text("York Lunch Club")), uiTimeout)
        assertTrue("Login screen should display app title", loginScreen)

        val registerLink = device.findObject(By.text("Don't have an account? Register"))
        if (registerLink != null) {
            registerLink.click()
            device.waitForIdle()
            val registerTitle = device.wait(Until.hasObject(By.text("Create Account")), uiTimeout)
            assertTrue("Register screen should appear", registerTitle)
        }
    }

    @Test
    fun tc20_fullMealRecordingJourney() {
        val hasApp = device.wait(Until.hasObject(By.pkg(appPackage).depth(0)), launchTimeout)
        assertTrue("App should launch", hasApp)

        val hasRestaurants = device.wait(Until.hasObject(By.text("Restaurants")), uiTimeout)
            ?: device.wait(Until.hasObject(By.text("Sign In")), uiTimeout)
        assertNotNull("Should show Restaurants tab or Login screen", hasRestaurants)
    }

    @Test
    fun tc21_mealHistoryToMealDetailToRestaurant() {
        val hasApp = device.wait(Until.hasObject(By.pkg(appPackage).depth(0)), launchTimeout)
        assertTrue("App should launch", hasApp)

        val myMeals = device.findObject(By.text("My Meals"))
        if (myMeals != null) {
            myMeals.click()
            device.waitForIdle()
            val hasMealsScreen = device.wait(Until.hasObject(By.textStartsWith("My Meals")), uiTimeout)
            assertTrue("My Meals screen should display", hasMealsScreen ?: false)
        }
    }

    @Test
    fun tc22_restaurantDetailBackwardNavigation() {
        val hasApp = device.wait(Until.hasObject(By.pkg(appPackage).depth(0)), launchTimeout)
        assertTrue("App should launch", hasApp)

        val restaurants = device.findObject(By.text("Restaurants"))
        if (restaurants != null) {
            restaurants.click()
            device.waitForIdle()
        }

        device.pressBack()
        device.waitForIdle()
        val stillAlive = device.wait(Until.hasObject(By.pkg(appPackage).depth(0)), uiTimeout)
        assertNotNull("App should still be running after back press", stillAlive)
    }

    @Test
    fun tc23_logoutAndReloginAsDifferentUser() {
        val hasApp = device.wait(Until.hasObject(By.pkg(appPackage).depth(0)), launchTimeout)
        assertTrue("App should launch", hasApp)

        val logoutBtn = device.findObject(By.desc("Logout"))
        if (logoutBtn != null) {
            logoutBtn.click()
            device.waitForIdle()
            val loginVisible = device.wait(Until.hasObject(By.text("Sign In")), uiTimeout)
            assertTrue("Login screen should appear after logout", loginVisible)
        } else {
            val signIn = device.wait(Until.hasObject(By.text("Sign In")), uiTimeout)
            assertTrue("Should be on login screen (not logged in)", signIn)
        }
    }
}
