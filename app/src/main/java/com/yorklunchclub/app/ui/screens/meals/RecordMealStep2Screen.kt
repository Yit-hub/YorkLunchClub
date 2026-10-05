package com.yorklunchclub.app.ui.screens.meals

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yorklunchclub.app.ui.components.FlavourSlider
import com.yorklunchclub.app.ui.components.PriceSlider
import com.yorklunchclub.app.ui.theme.Amber
import com.yorklunchclub.app.ui.theme.Background
import com.yorklunchclub.app.ui.theme.Teal
import com.yorklunchclub.app.ui.theme.TextMuted
import com.yorklunchclub.app.ui.theme.TextPrimary
import com.yorklunchclub.app.ui.theme.YorkLunchClubTheme
import androidx.compose.material.icons.automirrored.filled.ExitToApp

@Composable
fun RecordMealStep2Screen(
    viewModel: RecordMealViewModel,
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
        title = "Score Your Meal",
        state = state,
        onSetFlavour = viewModel::setFlavour,
        onSetPrice = viewModel::setPrice,
        onBackToStep1 = onBackToStep1,
        onConfirm = viewModel::confirm,
        onLogout = onLogout
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MealStep2Body(
    title: String,
    state: MealEditorUiState,
    onSetFlavour: (String, Int) -> Unit,
    onSetPrice: (String, Int) -> Unit,
    onBackToStep1: () -> Unit,
    onConfirm: () -> Unit,
    onLogout: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBackToStep1) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Edit selections")
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = TextMuted)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        containerColor = Background
    ) { inner ->
        Column(modifier = Modifier.fillMaxSize().padding(inner)) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text("Step 2 of 2", style = MaterialTheme.typography.labelSmall, color = Amber, fontWeight = FontWeight.Bold)
                Text(
                    "Rate flavour and price (1–5) for each item",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.draftItems, key = { it.menuItemId }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(0.5.dp, TextMuted.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(item.menuItemName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            FlavourSlider(
                                value = item.flavourScore,
                                onValueChange = { onSetFlavour(item.menuItemId, it) }
                            )
                            PriceSlider(
                                value = item.priceScore,
                                onValueChange = { onSetPrice(item.menuItemId, it) }
                            )
                        }
                    }
                }
            }
            if (state.error != null) {
                Text(state.error.orEmpty(), color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
            }
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onBackToStep1,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.weight(1f).height(52.dp)
                ) {
                    Text("Edit", color = Teal, fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = onConfirm,
                    enabled = !state.saving && state.draftItems.isNotEmpty(),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Amber),
                    modifier = Modifier.weight(1f).height(52.dp)
                ) {
                    if (state.saving) {
                        CircularProgressIndicator(modifier = Modifier.height(22.dp), color = Color.White)
                    } else {
                        Text("Confirm", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFAF8F3)
@Composable
fun RecordMealStep2ScreenPreview() {
    YorkLunchClubTheme {
        Box(modifier = Modifier.fillMaxSize()) { Text("Step 2 preview", modifier = Modifier.padding(16.dp)) }
    }
}
