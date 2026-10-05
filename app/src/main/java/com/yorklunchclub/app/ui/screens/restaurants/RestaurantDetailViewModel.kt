package com.yorklunchclub.app.ui.screens.restaurants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yorklunchclub.app.data.model.Meal
import com.yorklunchclub.app.data.model.Restaurant
import com.yorklunchclub.app.data.repository.AuthRepository
import com.yorklunchclub.app.data.repository.MealRepository
import com.yorklunchclub.app.data.repository.RestaurantRepository
import com.yorklunchclub.app.utils.ScoreCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RestaurantDetailUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val restaurant: Restaurant? = null,
    /** Cross-user averages straight from the restaurant doc. */
    val flavourAverage: String = "N/A",
    val priceAverage: String = "N/A",
    /** Current user's visits only. */
    val visitCount: Int = 0,
    val meals: List<Meal> = emptyList()
)

class RestaurantDetailViewModel(
    private val restaurantId: String,
    private val authRepository: AuthRepository = AuthRepository(),
    private val restaurantRepository: RestaurantRepository = RestaurantRepository(),
    private val mealRepository: MealRepository = MealRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(RestaurantDetailUiState())
    val state: StateFlow<RestaurantDetailUiState> = _state.asStateFlow()

    init { load() }

    companion object {
        fun factory(restaurantId: String): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                RestaurantDetailViewModel(restaurantId) as T
        }
    }

    fun refresh() = load()

    fun load() {
        val uid = authRepository.currentUserId ?: return
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val restaurantResult = restaurantRepository.fetchById(restaurantId)
            val mealsResult = mealRepository.fetchMealsForRestaurant(uid, restaurantId)
            if (restaurantResult.isFailure) {
                _state.update { it.copy(loading = false, error = restaurantResult.exceptionOrNull()?.message) }
                return@launch
            }
            val restaurant = restaurantResult.getOrNull()
            val meals = mealsResult.getOrDefault(emptyList())

            _state.update {
                it.copy(
                    loading = false,
                    restaurant = restaurant,
                    visitCount = meals.size,
                    flavourAverage = ScoreCalculator.displayScore(restaurant?.avgFlavourScore),
                    priceAverage = ScoreCalculator.displayScore(restaurant?.avgPriceScore),
                    meals = meals
                )
            }
        }
    }
}
