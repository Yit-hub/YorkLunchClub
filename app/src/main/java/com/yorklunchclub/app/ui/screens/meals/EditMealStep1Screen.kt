package com.yorklunchclub.app.ui.screens.meals

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yorklunchclub.app.ui.theme.YorkLunchClubTheme

@Composable
fun EditMealStep1Screen(
    viewModel: EditMealViewModel,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onLogout: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    MealStep1Body(
        title = "Edit Meal",
        state = state,
        onToggleSelection = viewModel::toggleSelection,
        canProceed = viewModel.canProceedToStep2(),
        onBack = onBack,
        onNext = onNext,
        onLogout = onLogout
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFAF8F3)
@Composable
fun EditMealStep1ScreenPreview() {
    YorkLunchClubTheme {
        Box(modifier = Modifier.fillMaxSize()) { Text("Edit Step 1 preview", modifier = Modifier.padding(16.dp)) }
    }
}
