package com.yorklunchclub.app.ui.screens.meals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.yorklunchclub.app.data.model.Meal
import com.yorklunchclub.app.data.model.MealItem
import com.yorklunchclub.app.data.model.MenuItem
import com.yorklunchclub.app.data.model.Restaurant
import com.yorklunchclub.app.data.repository.AuthRepository
import com.yorklunchclub.app.data.repository.MealRepository
import com.yorklunchclub.app.data.repository.RestaurantRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Per-item draft state shared by Record and Edit meal flows. */
data class DraftItem(
    val menuItemId: String,
    val menuItemName: String,
    val flavourScore: Int = 3,
    val priceScore: Int = 3
)

data class MealEditorUiState(
    val loading: Boolean = false,
    val saving: Boolean = false,
    val error: String? = null,
    val restaurant: Restaurant? = null,
    val menu: List<MenuItem> = emptyList(),
    val selectedItemIds: Set<String> = emptySet(),
    val draftItems: List<DraftItem> = emptyList(),
    val savedMealId: String? = null
)

class RecordMealViewModel(
    private val restaurantId: String,
    private val authRepository: AuthRepository = AuthRepository(),
    private val restaurantRepository: RestaurantRepository = RestaurantRepository(),
    private val mealRepository: MealRepository = MealRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(MealEditorUiState())
    val state: StateFlow<MealEditorUiState> = _state.asStateFlow()

    init { load() }

    companion object {
        fun factory(restaurantId: String): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                RecordMealViewModel(restaurantId) as T
        }
    }

    private fun load() {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val restaurant = restaurantRepository.fetchById(restaurantId).getOrNull()
            val menu = restaurantRepository.fetchMenu(restaurantId).getOrDefault(emptyList())
            _state.update { it.copy(loading = false, restaurant = restaurant, menu = menu) }
        }
    }

    fun toggleSelection(menuItemId: String) = toggleSelectionImpl(_state, menuItemId)
    fun setFlavour(menuItemId: String, score: Int) = setFlavourImpl(_state, menuItemId, score)
    fun setPrice(menuItemId: String, score: Int) = setPriceImpl(_state, menuItemId, score)
    fun canProceedToStep2(): Boolean = _state.value.draftItems.isNotEmpty()

    fun confirm() {
        val s = _state.value
        val uid = authRepository.currentUserId
        val restaurant = s.restaurant
        if (uid == null) { _state.update { it.copy(error = "Not signed in.") }; return }
        if (restaurant == null) { _state.update { it.copy(error = "Restaurant missing.") }; return }
        if (s.draftItems.isEmpty()) { _state.update { it.copy(error = "Select at least one item.") }; return }
        _state.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            val meal = Meal(
                userId = uid,
                restaurantId = restaurant.restaurantId,
                restaurantName = restaurant.name,
                date = Timestamp.now(),
                items = s.draftItems.map {
                    MealItem(
                        menuItemId = it.menuItemId,
                        menuItemName = it.menuItemName,
                        flavourScore = it.flavourScore,
                        priceScore = it.priceScore
                    )
                }
            )
            val result = mealRepository.saveMeal(meal)
            result.fold(
                onSuccess = { id -> _state.update { it.copy(saving = false, savedMealId = id) } },
                onFailure = { e ->
                    val detail = e.message ?: e::class.java.simpleName
                    _state.update { it.copy(saving = false, error = "Save failed: $detail") }
                }
            )
        }
    }

    fun onSavedHandled() = _state.update { it.copy(savedMealId = null) }
}

// --- Shared draft helpers, used by both RecordMealViewModel and EditMealViewModel ---

internal fun toggleSelectionImpl(stateFlow: MutableStateFlow<MealEditorUiState>, menuItemId: String) {
    stateFlow.update { s ->
        val newSet = if (s.selectedItemIds.contains(menuItemId)) s.selectedItemIds - menuItemId
        else s.selectedItemIds + menuItemId
        val newDrafts = if (s.selectedItemIds.contains(menuItemId)) {
            s.draftItems.filterNot { it.menuItemId == menuItemId }
        } else {
            val menuItem = s.menu.firstOrNull { it.menuItemId == menuItemId } ?: return@update s
            val existing = s.draftItems.firstOrNull { it.menuItemId == menuItemId }
            s.draftItems + (existing ?: DraftItem(menuItemId = menuItem.menuItemId, menuItemName = menuItem.name))
        }
        s.copy(selectedItemIds = newSet, draftItems = newDrafts, error = null)
    }
}

internal fun setFlavourImpl(stateFlow: MutableStateFlow<MealEditorUiState>, menuItemId: String, score: Int) {
    stateFlow.update { s ->
        s.copy(draftItems = s.draftItems.map {
            if (it.menuItemId == menuItemId) it.copy(flavourScore = score.coerceIn(1, 5)) else it
        })
    }
}

internal fun setPriceImpl(stateFlow: MutableStateFlow<MealEditorUiState>, menuItemId: String, score: Int) {
    stateFlow.update { s ->
        s.copy(draftItems = s.draftItems.map {
            if (it.menuItemId == menuItemId) it.copy(priceScore = score.coerceIn(1, 5)) else it
        })
    }
}
