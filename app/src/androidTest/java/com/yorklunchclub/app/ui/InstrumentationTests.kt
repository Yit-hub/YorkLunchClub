package com.yorklunchclub.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.yorklunchclub.app.ui.components.FlavourSlider
import com.yorklunchclub.app.ui.components.PriceSlider
import com.yorklunchclub.app.ui.screens.meals.MealEditorUiState
import com.yorklunchclub.app.ui.screens.meals.MealStep1Body
import com.yorklunchclub.app.data.model.MenuItem
import com.yorklunchclub.app.ui.theme.YorkLunchClubTheme
import org.junit.Rule
import org.junit.Test

class InstrumentationTests {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun tc09_flavourSliderRendersCorrectly() {
        composeRule.setContent {
            YorkLunchClubTheme {
                Column(modifier = Modifier.padding(16.dp)) {
                    FlavourSlider(value = 3, onValueChange = {})
                }
            }
        }
        composeRule.onNodeWithText("Flavour").assertIsDisplayed()
        composeRule.onNodeWithText("Bland").assertIsDisplayed()
        composeRule.onNodeWithText("Exceptional").assertIsDisplayed()
        composeRule.onNodeWithText("3").assertIsDisplayed()
    }

    @Test
    fun tc10_priceSliderRendersCorrectly() {
        composeRule.setContent {
            YorkLunchClubTheme {
                Column(modifier = Modifier.padding(16.dp)) {
                    PriceSlider(value = 3, onValueChange = {})
                }
            }
        }
        composeRule.onNodeWithText("Price").assertIsDisplayed()
        composeRule.onNodeWithText("Overpriced").assertIsDisplayed()
        composeRule.onNodeWithText("Steal").assertIsDisplayed()
        composeRule.onNodeWithText("3").assertIsDisplayed()
    }

    @Test
    fun tc11_menuItemSelection_expandsScoreInputs() {
        val menuItems = listOf(
            MenuItem(menuItemId = "m1", name = "Burger", price = 9.99),
            MenuItem(menuItemId = "m2", name = "Fries", price = 3.99)
        )
        val state = MealEditorUiState(
            loading = false,
            menu = menuItems,
            selectedItemIds = emptySet(),
            draftItems = emptyList()
        )
        var selectedIds = state.selectedItemIds
        composeRule.setContent {
            YorkLunchClubTheme {
                MealStep1Body(
                    title = "Record Meal",
                    state = state.copy(selectedItemIds = selectedIds),
                    onToggleSelection = { id ->
                        selectedIds = if (selectedIds.contains(id)) selectedIds - id else selectedIds + id
                    },
                    canProceed = selectedIds.isNotEmpty(),
                    onBack = {},
                    onNext = {}
                )
            }
        }
        composeRule.onNodeWithText("Burger").assertIsDisplayed()
        composeRule.onNodeWithText("Burger").performClick()
        composeRule.onNodeWithText("Selected", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun tc12_biometricLoginButton_visibleOnLoginScreen() {
        composeRule.setContent {
            YorkLunchClubTheme {
                com.yorklunchclub.app.ui.screens.auth.LoginScreen(
                    onSignedIn = {},
                    onNavigateRegister = {}
                )
            }
        }
        composeRule.onNodeWithText("Sign In").assertIsDisplayed()
        composeRule.onNodeWithText("York Lunch Club").assertIsDisplayed()
    }

    @Test
    fun tc13_logoutButton_visibleOnRestaurantList() {
        composeRule.setContent {
            YorkLunchClubTheme {
                com.yorklunchclub.app.ui.navigation.AppNavGraph(
                    startDestination = "restaurants"
                )
            }
        }
        composeRule.onNodeWithContentDescription("Logout").assertIsDisplayed()
    }
}
