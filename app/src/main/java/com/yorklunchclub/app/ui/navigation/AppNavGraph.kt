package com.yorklunchclub.app.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.yorklunchclub.app.ui.screens.auth.LoginScreen
import com.yorklunchclub.app.ui.screens.auth.RegisterScreen
import com.yorklunchclub.app.ui.screens.meals.EditMealStep1Screen
import com.yorklunchclub.app.ui.screens.meals.EditMealStep2Screen
import com.yorklunchclub.app.ui.screens.meals.EditMealViewModel
import com.yorklunchclub.app.ui.screens.meals.MealDetailScreen
import com.yorklunchclub.app.ui.screens.meals.MealHistoryScreen
import com.yorklunchclub.app.ui.screens.meals.RecordMealStep1Screen
import com.yorklunchclub.app.ui.screens.meals.RecordMealStep2Screen
import com.yorklunchclub.app.ui.screens.meals.RecordMealViewModel
import com.yorklunchclub.app.ui.screens.restaurants.RestaurantDetailScreen
import com.yorklunchclub.app.ui.screens.restaurants.RestaurantListScreen
import com.yorklunchclub.app.ui.theme.Amber
import com.yorklunchclub.app.ui.theme.Background
import com.yorklunchclub.app.ui.theme.TextMuted
import com.yorklunchclub.app.ui.theme.YorkLunchClubTheme
import com.yorklunchclub.app.utils.BiometricHelper

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val RESTAURANTS = "restaurants"
    const val RESTAURANT_DETAIL = "restaurants/{restaurantId}"
    const val RECORD_MEAL_STEP_1 = "restaurants/{restaurantId}/record/1"
    const val RECORD_MEAL_STEP_2 = "restaurants/{restaurantId}/record/2"
    const val MEALS = "meals"
    const val MEAL_DETAIL = "meals/{mealId}"
    const val EDIT_MEAL_STEP_1 = "meals/{mealId}/edit/1"
    const val EDIT_MEAL_STEP_2 = "meals/{mealId}/edit/2"

    fun restaurantDetail(id: String) = "restaurants/$id"
    fun recordMealStep1(id: String) = "restaurants/$id/record/1"
    fun recordMealStep2(id: String) = "restaurants/$id/record/2"
    fun mealDetail(id: String) = "meals/$id"
    fun editMealStep1(mealId: String) = "meals/$mealId/edit/1"
    fun editMealStep2(mealId: String) = "meals/$mealId/edit/2"
}

private val tabRoutes = setOf(Routes.RESTAURANTS, Routes.MEALS)

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String =
        if (FirebaseAuth.getInstance().currentUser != null) Routes.RESTAURANTS else Routes.LOGIN
) {
    val context = LocalContext.current
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val logout: () -> Unit = {
        FirebaseAuth.getInstance().signOut()
        BiometricHelper.clearCredentials(context)
        navController.navigate(Routes.LOGIN) {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }

    Scaffold(
        bottomBar = {
            if (currentRoute in tabRoutes) {
                AppBottomNav(navController, currentRoute)
            }
        },
        containerColor = Background
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.LOGIN) {
                LoginScreen(
                    onSignedIn = {
                        navController.navigate(Routes.RESTAURANTS) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onNavigateRegister = { navController.navigate(Routes.REGISTER) }
                )
            }
            composable(Routes.REGISTER) {
                RegisterScreen(
                    onRegistered = {
                        navController.navigate(Routes.RESTAURANTS) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onBackToLogin = { navController.popBackStack() }
                )
            }
            composable(Routes.RESTAURANTS) {
                TabContainer(onLogout = logout) {
                    RestaurantListScreen(
                        onRestaurantClick = { id -> navController.navigate(Routes.restaurantDetail(id)) }
                    )
                }
            }
            composable(Routes.MEALS) {
                TabContainer(onLogout = logout) {
                    MealHistoryScreen(
                        onMealClick = { id -> navController.navigate(Routes.mealDetail(id)) }
                    )
                }
            }
            composable(
                route = Routes.RESTAURANT_DETAIL,
                arguments = listOf(navArgument("restaurantId") { type = NavType.StringType })
            ) { entry ->
                val id = entry.arguments?.getString("restaurantId").orEmpty()
                RestaurantDetailScreen(
                    restaurantId = id,
                    onBack = { navController.popBackStack() },
                    onRecordMeal = { navController.navigate(Routes.recordMealStep1(id)) },
                    onMealClick = { mealId -> navController.navigate(Routes.mealDetail(mealId)) },
                    onLogout = logout
                )
            }
            composable(
                route = Routes.RECORD_MEAL_STEP_1,
                arguments = listOf(navArgument("restaurantId") { type = NavType.StringType })
            ) { entry ->
                val id = entry.arguments?.getString("restaurantId").orEmpty()
                val parent = remember(entry) {
                    navController.getBackStackEntry(Routes.RESTAURANT_DETAIL)
                }
                val recordVm: RecordMealViewModel = viewModel(
                    viewModelStoreOwner = parent,
                    key = "RecordMeal/$id",
                    factory = RecordMealViewModel.factory(id)
                )
                RecordMealStep1Screen(
                    viewModel = recordVm,
                    onBack = { navController.popBackStack() },
                    onNext = { navController.navigate(Routes.recordMealStep2(id)) },
                    onLogout = logout
                )
            }
            composable(
                route = Routes.RECORD_MEAL_STEP_2,
                arguments = listOf(navArgument("restaurantId") { type = NavType.StringType })
            ) { entry ->
                val id = entry.arguments?.getString("restaurantId").orEmpty()
                val parent = remember(entry) {
                    navController.getBackStackEntry(Routes.RESTAURANT_DETAIL)
                }
                val recordVm: RecordMealViewModel = viewModel(
                    viewModelStoreOwner = parent,
                    key = "RecordMeal/$id",
                    factory = RecordMealViewModel.factory(id)
                )
                RecordMealStep2Screen(
                    viewModel = recordVm,
                    onBackToStep1 = { navController.popBackStack() },
                    onLogout = logout,
                    onConfirmed = { rid ->
                        // After confirming, return to (a refreshed) Restaurant Detail.
                        navController.navigate(Routes.restaurantDetail(rid)) {
                            popUpTo(Routes.RESTAURANTS) { inclusive = false }
                            launchSingleTop = false
                        }
                    }
                )
            }
            composable(
                route = Routes.MEAL_DETAIL,
                arguments = listOf(navArgument("mealId") { type = NavType.StringType })
            ) { entry ->
                val id = entry.arguments?.getString("mealId").orEmpty()
                MealDetailScreen(
                    mealId = id,
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(Routes.editMealStep1(id)) },
                    onViewRestaurant = { rid -> navController.navigate(Routes.restaurantDetail(rid)) },
                    onLogout = logout
                )
            }
            composable(
                route = Routes.EDIT_MEAL_STEP_1,
                arguments = listOf(navArgument("mealId") { type = NavType.StringType })
            ) { entry ->
                val mealId = entry.arguments?.getString("mealId").orEmpty()
                val parent = remember(entry) {
                    navController.getBackStackEntry(Routes.MEAL_DETAIL)
                }
                val editVm: EditMealViewModel = viewModel(
                    viewModelStoreOwner = parent,
                    key = "EditMeal/$mealId",
                    factory = EditMealViewModel.factory(mealId)
                )
                EditMealStep1Screen(
                    viewModel = editVm,
                    onBack = { navController.popBackStack() },
                    onNext = { navController.navigate(Routes.editMealStep2(mealId)) },
                    onLogout = logout
                )
            }
            composable(
                route = Routes.EDIT_MEAL_STEP_2,
                arguments = listOf(navArgument("mealId") { type = NavType.StringType })
            ) { entry ->
                val mealId = entry.arguments?.getString("mealId").orEmpty()
                val parent = remember(entry) {
                    navController.getBackStackEntry(Routes.MEAL_DETAIL)
                }
                val editVm: EditMealViewModel = viewModel(
                    viewModelStoreOwner = parent,
                    key = "EditMeal/$mealId",
                    factory = EditMealViewModel.factory(mealId)
                )
                EditMealStep2Screen(
                    viewModel = editVm,
                    onBackToStep1 = { navController.popBackStack() },
                    onLogout = logout,
                    onConfirmed = { rid ->
                        // After confirming an edit, go straight to (a refreshed) Restaurant Detail.
                        navController.navigate(Routes.restaurantDetail(rid)) {
                            popUpTo(Routes.RESTAURANTS) { inclusive = false }
                            launchSingleTop = false
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun TabContainer(
    onLogout: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        content()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 4.dp, top = 4.dp),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onLogout) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = TextMuted)
            }
        }
    }
}

@Composable
private fun AppBottomNav(navController: NavHostController, currentRoute: String?) {
    NavigationBar(containerColor = Color.White) {
        NavigationBarItem(
            selected = currentRoute == Routes.RESTAURANTS,
            onClick = {
                navController.navigate(Routes.RESTAURANTS) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = { Icon(Icons.Filled.Place, contentDescription = null) },
            label = { Text("Restaurants", fontWeight = FontWeight.SemiBold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Amber,
                selectedTextColor = Amber,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted
            )
        )
        NavigationBarItem(
            selected = currentRoute == Routes.MEALS,
            onClick = {
                navController.navigate(Routes.MEALS) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = { Icon(Icons.Filled.List, contentDescription = null) },
            label = { Text("My Meals", fontWeight = FontWeight.SemiBold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Amber,
                selectedTextColor = Amber,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AppNavGraphPreview() {
    YorkLunchClubTheme {
        AppNavGraph()
    }
}
