package com.yorklunchclub.app.ui.screens.meals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yorklunchclub.app.data.model.MealItem
import com.yorklunchclub.app.data.repository.AuthRepository
import com.yorklunchclub.app.data.repository.MealRepository
import com.yorklunchclub.app.data.repository.RestaurantRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditMealViewModel(
    private val mealId: String,
    private val authRepository: AuthRepository = AuthRepository(),
    private val restaurantRepository: RestaurantRepository = RestaurantRepository(),
    private val mealRepository: MealRepository = MealRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(MealEditorUiState())
    val state: StateFlow<MealEditorUiState> = _state.asStateFlow()

    init { load() }

    companion object {
        fun factory(mealId: String): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                EditMealViewModel(mealId) as T
        }
    }

    private fun load() {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val meal = mealRepository.fetchMealById(mealId).getOrNull()
            if (meal == null) {
                _state.update { it.copy(loading = false, error = "Meal not found.") }
                return@launch
            }
            // Ownership guard at VM layer; Firestore rules also enforce this.
            val uid = authRepository.currentUserId
            if (uid != null && meal.userId != uid) {
                _state.update { it.copy(loading = false, error = "You cannot edit another user's meal.") }
                return@launch
            }

            val restaurant = restaurantRepository.fetchById(meal.restaurantId).getOrNull()
            val menu = restaurantRepository.fetchMenu(meal.restaurantId).getOrDefault(emptyList())

            val selected = meal.items.map { it.menuItemId }.toSet()
            val drafts = meal.items.map {
                DraftItem(
                    menuItemId = it.menuItemId,
                    menuItemName = it.menuItemName,
                    flavourScore = it.flavourScore,
                    priceScore = it.priceScore
                )
            }

            _state.update {
                it.copy(
                    loading = false,
                    restaurant = restaurant,
                    menu = menu,
                    selectedItemIds = selected,
                    draftItems = drafts
                )
            }
        }
    }

    fun toggleSelection(menuItemId: String) = toggleSelectionImpl(_state, menuItemId)
    fun setFlavour(menuItemId: String, score: Int) = setFlavourImpl(_state, menuItemId, score)
    fun setPrice(menuItemId: String, score: Int) = setPriceImpl(_state, menuItemId, score)
    fun canProceedToStep2(): Boolean = _state.value.draftItems.isNotEmpty()

    fun confirm() {
        val s = _state.value
        if (s.draftItems.isEmpty()) { _state.update { it.copy(error = "Select at least one item.") }; return }
        _state.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            val items = s.draftItems.map {
                MealItem(
                    menuItemId = it.menuItemId,
                    menuItemName = it.menuItemName,
                    flavourScore = it.flavourScore,
                    priceScore = it.priceScore
                )
            }
            val result = mealRepository.updateMealItems(mealId, items)
            result.fold(
                onSuccess = { _state.update { it.copy(saving = false, savedMealId = mealId) } },
                onFailure = { e ->
                    val detail = e.message ?: e::class.java.simpleName
                    _state.update { it.copy(saving = false, error = "Save failed: $detail") }
                }
            )
        }
    }

    fun onSavedHandled() = _state.update { it.copy(savedMealId = null) }
}
