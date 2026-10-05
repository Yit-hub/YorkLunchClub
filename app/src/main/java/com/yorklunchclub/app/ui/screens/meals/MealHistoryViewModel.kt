package com.yorklunchclub.app.ui.screens.meals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yorklunchclub.app.data.model.Meal
import com.yorklunchclub.app.data.repository.AuthRepository
import com.yorklunchclub.app.data.repository.MealRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MealHistoryUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val meals: List<Meal> = emptyList()
)

class MealHistoryViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val mealRepository: MealRepository = MealRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(MealHistoryUiState())
    val state: StateFlow<MealHistoryUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        val uid = authRepository.currentUserId ?: return
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val result = mealRepository.fetchUserMeals(uid)
            result.fold(
                onSuccess = { meals -> _state.update { it.copy(loading = false, meals = meals) } },
                onFailure = { e -> _state.update { it.copy(loading = false, error = e.message ?: "Failed to load meals.") } }
            )
        }
    }
}
