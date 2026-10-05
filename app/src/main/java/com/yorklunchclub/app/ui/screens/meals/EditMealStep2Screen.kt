package com.yorklunchclub.app.ui.screens.meals

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yorklunchclub.app.ui.theme.YorkLunchClubTheme

@Composable
fun EditMealStep2Screen(
    viewModel: EditMealViewModel,
    onBackToStep1: () -> Unit,
    onConfirmed: (restaurantId: String) -> Unit,
    onLogout: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val restaurantId = state.restaurant?.restaurantId.orEmpty()

    LaunchedEffect(state.savedMealId) {
        val id = state.savedMealId ?: return@LaunchedEffect
        viewModel.onSavedHandled()
        onConfirmed(restaurantId)
    }

    MealStep2Body(
        title = "Edit Meal — Scores",
        state = state,
        onSetFlavour = viewModel::setFlavour,
        onSetPrice = viewModel::setPrice,
        onBackToStep1 = onBackToStep1,
        onConfirm = viewModel::confirm,
        onLogout = onLogout
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFAF8F3)
@Composable
fun EditMealStep2ScreenPreview() {
    YorkLunchClubTheme {
        Box(modifier = Modifier.fillMaxSize()) { Text("Edit Step 2 preview", modifier = Modifier.padding(16.dp)) }
    }
}
