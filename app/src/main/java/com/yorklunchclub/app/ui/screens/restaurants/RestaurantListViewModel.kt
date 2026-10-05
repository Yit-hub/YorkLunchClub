package com.yorklunchclub.app.ui.screens.restaurants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yorklunchclub.app.data.model.Restaurant
import com.yorklunchclub.app.data.repository.RestaurantRepository
import com.yorklunchclub.app.utils.ScoreCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RestaurantListItem(
    val restaurant: Restaurant,
    val averageDisplay: String
)

data class RestaurantListUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val items: List<RestaurantListItem> = emptyList()
)

class RestaurantListViewModel(
    private val restaurantRepository: RestaurantRepository = RestaurantRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(RestaurantListUiState())
    val state: StateFlow<RestaurantListUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val result = restaurantRepository.fetchAll()
            if (result.isFailure) {
                _state.update { it.copy(loading = false, error = result.exceptionOrNull()?.message ?: "Failed to load restaurants") }
                return@launch
            }
            val restaurants = result.getOrDefault(emptyList())
            val items = restaurants.map { restaurant ->
                RestaurantListItem(
                    restaurant = restaurant,
                    averageDisplay = combinedDisplay(restaurant.avgFlavourScore, restaurant.avgPriceScore)
                )
            }
            _state.update { it.copy(loading = false, items = items) }
        }
    }

    private fun combinedDisplay(avgFlavour: Double?, avgPrice: Double?): String {
        if (avgFlavour == null && avgPrice == null) return "N/A"
        val present = listOfNotNull(avgFlavour, avgPrice)
        return ScoreCalculator.displayScore(present.average())
    }
}
