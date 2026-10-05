package com.yorklunchclub.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.Timestamp
import com.yorklunchclub.app.data.model.Meal
import com.yorklunchclub.app.data.model.MealItem
import com.yorklunchclub.app.ui.theme.TextMuted
import com.yorklunchclub.app.ui.theme.TextPrimary
import com.yorklunchclub.app.ui.theme.YorkLunchClubTheme
import com.yorklunchclub.app.utils.ScoreCalculator
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun MealCard(
    meal: Meal,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val flavour = ScoreCalculator.displayScore(
        if (meal.items.isEmpty()) null else ScoreCalculator.visitAverage(meal.items.map { it.flavourScore })
    )
    val price = ScoreCalculator.displayScore(
        if (meal.items.isEmpty()) null else ScoreCalculator.visitAverage(meal.items.map { it.priceScore })
    )
    val dateText = remember(meal.date) { formatDate(meal.date) }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(0.5.dp, TextMuted.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = meal.restaurantName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = "$dateText  ·  ${meal.items.size} item${if (meal.items.size == 1) "" else "s"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ScoreBadge(label = "Flv", value = flavour, kind = ScoreBadgeKind.Flavour)
                ScoreBadge(label = "Prc", value = price, kind = ScoreBadgeKind.Price)
            }
        }
    }
}

private fun formatDate(ts: Timestamp): String =
    SimpleDateFormat("d MMM yyyy", Locale.UK).format(ts.toDate())

@Preview(showBackground = true)
@Composable
fun MealCardPreview() {
    YorkLunchClubTheme {
        MealCard(
            meal = Meal(
                mealId = "m1",
                restaurantName = "Bill's Restaurant",
                date = Timestamp.now(),
                items = listOf(
                    MealItem(menuItemName = "Burger", flavourScore = 4, priceScore = 3),
                    MealItem(menuItemName = "Fries", flavourScore = 5, priceScore = 4)
                )
            ),
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
