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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Timestamp
import com.yorklunchclub.app.ui.components.ScoreBadge
import com.yorklunchclub.app.ui.components.ScoreBadgeKind
import com.yorklunchclub.app.ui.theme.Amber
import com.yorklunchclub.app.ui.theme.Background
import com.yorklunchclub.app.ui.theme.Teal
import com.yorklunchclub.app.ui.theme.TextMuted
import com.yorklunchclub.app.ui.theme.TextPrimary
import com.yorklunchclub.app.ui.theme.YorkLunchClubTheme
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import com.yorklunchclub.app.utils.ScoreCalculator
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealDetailScreen(
    mealId: String,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onViewRestaurant: (restaurantId: String) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val viewModel: MealDetailViewModel = viewModel(
        key = "MealDetail/$mealId",
        factory = MealDetailViewModel.factory(mealId)
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    androidx.compose.runtime.LaunchedEffect(Unit) { viewModel.refresh() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meal", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onEdit, enabled = state.meal != null) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit meal")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = TextMuted)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        containerColor = Background
    ) { inner ->
        when {
            state.loading -> Box(modifier = Modifier.fillMaxSize().padding(inner), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.error != null -> Box(modifier = Modifier.fillMaxSize().padding(inner), contentAlignment = Alignment.Center) {
                Text(state.error.orEmpty(), color = MaterialTheme.colorScheme.error)
            }
            state.meal == null -> Box(modifier = Modifier.fillMaxSize().padding(inner), contentAlignment = Alignment.Center) {
                Text("Meal not found", color = TextMuted)
            }
            else -> {
                val meal = state.meal!!
                val flavourAvg = if (meal.items.isEmpty()) "N/A"
                    else ScoreCalculator.displayScore(ScoreCalculator.visitAverage(meal.items.map { it.flavourScore }))
                val priceAvg = if (meal.items.isEmpty()) "N/A"
                    else ScoreCalculator.displayScore(ScoreCalculator.visitAverage(meal.items.map { it.priceScore }))
                val dateText = remember(meal.date) { formatDate(meal.date) }

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(inner),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Column {
                            Text(meal.restaurantName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(dateText, style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ScoreBadge(label = "Flavour", value = flavourAvg, kind = ScoreBadgeKind.Flavour)
                                ScoreBadge(label = "Price", value = priceAvg, kind = ScoreBadgeKind.Price)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = { onViewRestaurant(meal.restaurantId) },
                                shape = RoundedCornerShape(50),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Teal)
                            ) {
                                Text("View Restaurant", color = Teal, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Items", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                    }
                    items(meal.items, key = { it.itemId.ifEmpty { it.menuItemId } }) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(0.5.dp, TextMuted.copy(alpha = 0.3f))
                        ) {
                            Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    item.menuItemName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary,
                                    modifier = Modifier.fillMaxWidth().padding(end = 8.dp)
                                )
                            }
                            Row(modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, bottom = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ScoreBadge(label = "Flv", value = item.flavourScore.toString(), kind = ScoreBadgeKind.Flavour)
                                ScoreBadge(label = "Prc", value = item.priceScore.toString(), kind = ScoreBadgeKind.Price)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatDate(ts: Timestamp): String =
    SimpleDateFormat("d MMM yyyy", Locale.UK).format(ts.toDate())

@Preview(showBackground = true, backgroundColor = 0xFFFAF8F3)
@Composable
fun MealDetailScreenPreview() {
    YorkLunchClubTheme {
        MealDetailScreen(mealId = "m1", onBack = {}, onEdit = {})
    }
}
