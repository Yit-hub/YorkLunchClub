package com.yorklunchclub.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yorklunchclub.app.ui.theme.Amber
import com.yorklunchclub.app.ui.theme.Teal
import com.yorklunchclub.app.ui.theme.YorkLunchClubTheme

enum class ScoreBadgeKind { Flavour, Price, Overall }

@Composable
fun ScoreBadge(
    label: String,
    value: String,
    kind: ScoreBadgeKind = ScoreBadgeKind.Overall,
    modifier: Modifier = Modifier
) {
    val bg = when (kind) {
        ScoreBadgeKind.Flavour -> Amber
        ScoreBadgeKind.Price -> Teal
        ScoreBadgeKind.Overall -> Amber
    }
    androidx.compose.foundation.layout.Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label.uppercase(),
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ScoreBadgePreview() {
    YorkLunchClubTheme {
        ScoreBadge(label = "Flavour", value = "4.2", kind = ScoreBadgeKind.Flavour)
    }
}
