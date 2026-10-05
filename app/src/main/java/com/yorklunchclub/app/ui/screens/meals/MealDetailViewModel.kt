package com.yorklunchclub.app.ui.screens.meals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yorklunchclub.app.data.model.Meal
import com.yorklunchclub.app.data.repository.MealRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MealDetailUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val meal: Meal? = null
)

class MealDetailViewModel(
    private val mealId: String,
    private val mealRepository: MealRepository = MealRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(MealDetailUiState())
    val state: StateFlow<MealDetailUiState> = _state.asStateFlow()

    init { load() }

    companion object {
        fun factory(mealId: String): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                MealDetailViewModel(mealId) as T
        }
    }

    fun refresh() = load()

    private fun load() {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val result = mealRepository.fetchMealById(mealId)
            result.fold(
                onSuccess = { meal -> _state.update { it.copy(loading = false, meal = meal) } },
                onFailure = { e -> _state.update { it.copy(loading = false, error = e.message ?: "Failed to load meal.") } }
            )
        }
    }
}
