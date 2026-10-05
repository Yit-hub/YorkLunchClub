package com.yorklunchclub.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yorklunchclub.app.data.model.Restaurant
import com.yorklunchclub.app.ui.theme.Amber
import com.yorklunchclub.app.ui.theme.AmberLight
import com.yorklunchclub.app.ui.theme.TealLight
import com.yorklunchclub.app.ui.theme.TextMuted
import com.yorklunchclub.app.ui.theme.TextPrimary
import com.yorklunchclub.app.ui.theme.YorkLunchClubTheme

@Composable
fun RestaurantCard(
    restaurant: Restaurant,
    averageDisplay: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(0.5.dp, TextMuted.copy(alpha = 0.3f))
    ) {
        Column {
            RestaurantPhoto(
                restaurantId = restaurant.restaurantId,
                imageUrl = restaurant.imageUrl,
                contentDescription = restaurant.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(AmberLight)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = restaurant.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = restaurant.cuisine,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Text(
                        text = restaurant.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
                AverageBadge(averageDisplay)
            }
        }
    }
}

@Composable
private fun AverageBadge(text: String) {
    val isNA = text == "N/A"
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isNA) TealLight else Amber),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isNA) TextMuted else Color.White
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RestaurantCardPreview() {
    YorkLunchClubTheme {
        RestaurantCard(
            restaurant = Restaurant(
                restaurantId = "r01",
                name = "Bill's Restaurant",
                address = "12 Coney Street, York",
                cuisine = "British"
            ),
            averageDisplay = "4.2",
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
