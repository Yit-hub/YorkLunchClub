package com.yorklunchclub.app.ui.screens.meals

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yorklunchclub.app.ui.theme.Amber
import com.yorklunchclub.app.ui.theme.AmberLight
import com.yorklunchclub.app.ui.theme.Background
import com.yorklunchclub.app.ui.theme.TextMuted
import com.yorklunchclub.app.ui.theme.TextPrimary
import com.yorklunchclub.app.ui.theme.YorkLunchClubTheme
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import java.text.NumberFormat
import java.util.Locale

@Composable
fun RecordMealStep1Screen(
    viewModel: RecordMealViewModel,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onLogout: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    MealStep1Body(
        title = "Record Meal",
        state = state,
        onToggleSelection = viewModel::toggleSelection,
        canProceed = viewModel.canProceedToStep2(),
        onBack = onBack,
        onNext = onNext,
        onLogout = onLogout
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MealStep1Body(
    title: String,
    state: MealEditorUiState,
    onToggleSelection: (String) -> Unit,
    canProceed: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onLogout: () -> Unit = {}
) {
    val priceFormat = remember { NumberFormat.getCurrencyInstance(Locale.UK) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
        when {
            state.loading -> Box(modifier = Modifier.fillMaxSize().padding(inner), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            else -> Column(modifier = Modifier.fillMaxSize().padding(inner)) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text("Step 1 of 2", style = MaterialTheme.typography.labelSmall, color = Amber, fontWeight = FontWeight.Bold)
                    Text(
                        "Select items you had at ${state.restaurant?.name ?: ""}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(state.menu, key = { it.menuItemId }) { item ->
                        val selected = state.selectedItemIds.contains(item.menuItemId)
                        Card(
                            onClick = { onToggleSelection(item.menuItemId) },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selected) AmberLight else Color.White
                            ),
                            border = BorderStroke(
                                width = if (selected) 1.5.dp else 0.5.dp,
                                color = if (selected) Amber else TextMuted.copy(alpha = 0.3f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.name, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                                    Text(priceFormat.format(item.price), style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                }
                                if (selected) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(Amber)
                                            .padding(6.dp)
                                    ) {
                                        Icon(Icons.Filled.Check, contentDescription = "Selected", tint = Color.White)
                                    }
                                }
                            }
                        }
                    }
                    if (state.menu.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("No menu items.", color = TextMuted)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { if (canProceed) onNext() },
                    enabled = canProceed,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Amber, disabledContainerColor = Amber.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(52.dp)
                ) {
                    Text("Next — Score Your Meal", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFAF8F3)
@Composable
fun RecordMealStep1ScreenPreview() {
    YorkLunchClubTheme {
        Box(modifier = Modifier.fillMaxSize()) { Text("Step 1 preview", modifier = Modifier.padding(16.dp)) }
    }
}
