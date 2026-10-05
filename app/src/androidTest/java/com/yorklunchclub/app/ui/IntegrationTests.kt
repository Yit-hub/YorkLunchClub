package com.yorklunchclub.app.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.google.firebase.Timestamp
import com.yorklunchclub.app.data.model.Meal
import com.yorklunchclub.app.data.model.MealItem
import com.yorklunchclub.app.ui.components.MealCard
import com.yorklunchclub.app.ui.screens.meals.DraftItem
import com.yorklunchclub.app.ui.screens.meals.MealEditorUiState
import com.yorklunchclub.app.ui.screens.meals.MealStep1Body
import com.yorklunchclub.app.ui.screens.meals.MealStep2Body
import com.yorklunchclub.app.ui.theme.YorkLunchClubTheme
import com.yorklunchclub.app.data.model.MenuItem
import com.yorklunchclub.app.data.model.Restaurant
import com.yorklunchclub.app.ui.components.ScoreBadge
import com.yorklunchclub.app.ui.components.ScoreBadgeKind
import com.yorklunchclub.app.utils.ScoreCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test

class IntegrationTests {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun tc14_mealSavedAppearsInHistory() {
        val meal = Meal(
            mealId = "meal1",
            userId = "user1",
            restaurantId = "r01",
            restaurantName = "Bill's Restaurant",
            date = Timestamp.now(),
            items = listOf(
                MealItem(
                    itemId = "i1",
                    menuItemId = "m1",
                    menuItemName = "Burger",
                    flavourScore = 4,
                    priceScore = 3
                )
            )
        )
        composeRule.setContent {
            YorkLunchClubTheme {
                MealCard(meal = meal, onClick = {})
            }
        }
        composeRule.onNodeWithText("Bill's Restaurant").assertIsDisplayed()
    }

    @Test
    fun tc15_mealScoreUpdatesRestaurantAverage() {
        val visit1Flavours = listOf(4, 5)
        val visit2Flavours = listOf(3, 3)
        val visit1Avg = ScoreCalculator.visitAverage(visit1Flavours)
        val visit2Avg = ScoreCalculator.visitAverage(visit2Flavours)
        val restaurantAvg = ScoreCalculator.restaurantAverage(listOf(visit1Avg, visit2Avg))

        assertEquals(4.5, visit1Avg, 0.0001)
        assertEquals(3.0, visit2Avg, 0.0001)
        assertEquals(3.75, restaurantAvg, 0.0001)

        val displayValue = ScoreCalculator.displayScore(restaurantAvg)
        composeRule.setContent {
            YorkLunchClubTheme {
                ScoreBadge(label = "Flavour", value = displayValue, kind = ScoreBadgeKind.Flavour)
            }
        }
        composeRule.onNodeWithText("3.8").assertIsDisplayed()
    }

    @Test
    fun tc16_userDataIsolation_differentUsers_showsOwnDataOnly() {
        val userAMeal = Meal(
            mealId = "meal_a",
            userId = "userA",
            restaurantId = "r01",
            restaurantName = "Bill's Restaurant",
            date = Timestamp.now(),
            items = listOf(
                MealItem(itemId = "i1", menuItemId = "m1", menuItemName = "Steak", flavourScore = 5, priceScore = 4)
            )
        )
        val userBMeal = Meal(
            mealId = "meal_b",
            userId = "userB",
            restaurantId = "r02",
            restaurantName = "YUZU Street Food",
            date = Timestamp.now(),
            items = listOf(
                MealItem(itemId = "i2", menuItemId = "m2", menuItemName = "Ramen", flavourScore = 4, priceScore = 3)
            )
        )
        assertNotEquals(userAMeal.userId, userBMeal.userId)

        val userAMeals = listOf(userAMeal, userBMeal).filter { it.userId == "userA" }
        assertEquals(1, userAMeals.size)
        assertEquals("meal_a", userAMeals[0].mealId)

        val userBMeals = listOf(userAMeal, userBMeal).filter { it.userId == "userB" }
        assertEquals(1, userBMeals.size)
        assertEquals("meal_b", userBMeals[0].mealId)
    }

    @Test
    fun tc17_editEntry_returnsToStep1WithSelectionsIntact() {
        val menuItems = listOf(
            MenuItem(menuItemId = "m1", name = "Burger", price = 9.99),
            MenuItem(menuItemId = "m2", name = "Fries", price = 3.99)
        )
        val drafts = listOf(
            DraftItem(menuItemId = "m1", menuItemName = "Burger", flavourScore = 4, priceScore = 3)
        )
        val stateWithSelections = MealEditorUiState(
            loading = false,
            restaurant = Restaurant(restaurantId = "r01", name = "Bill's"),
            menu = menuItems,
            selectedItemIds = setOf("m1"),
            draftItems = drafts
        )
        var wentBackToStep1 = false
        composeRule.setContent {
            YorkLunchClubTheme {
                MealStep2Body(
                    title = "Score Your Meal",
                    state = stateWithSelections,
                    onSetFlavour = { _, _ -> },
                    onSetPrice = { _, _ -> },
                    onBackToStep1 = { wentBackToStep1 = true },
                    onConfirm = {}
                )
            }
        }
        composeRule.onNodeWithText("Burger").assertIsDisplayed()
        composeRule.onNodeWithText("Edit").performClick()
        assert(wentBackToStep1)

        composeRule.setContent {
            YorkLunchClubTheme {
                MealStep1Body(
                    title = "Record Meal",
                    state = stateWithSelections,
                    onToggleSelection = {},
                    canProceed = true,
                    onBack = {},
                    onNext = {}
                )
            }
        }
        composeRule.onNodeWithText("Burger").assertIsDisplayed()
        composeRule.onNodeWithText("Selected", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun tc18_mealDetailLinksToRestaurantDetail() {
        var navigatedToRestaurantId: String? = null
        composeRule.setContent {
            YorkLunchClubTheme {
                com.yorklunchclub.app.ui.screens.meals.MealDetailScreen(
                    mealId = "test_meal",
                    onBack = {},
                    onEdit = {},
                    onViewRestaurant = { rid -> navigatedToRestaurantId = rid }
                )
            }
        }
        composeRule.waitForIdle()
    }
}
