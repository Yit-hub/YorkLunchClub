package com.yorklunchclub.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yorklunchclub.app.ui.theme.Amber
import com.yorklunchclub.app.ui.theme.Teal
import com.yorklunchclub.app.ui.theme.TextMuted
import com.yorklunchclub.app.ui.theme.YorkLunchClubTheme

@Composable
fun ScoreSlider(
    title: String,
    leftLabel: String,
    rightLabel: String,
    value: Int,
    accent: androidx.compose.ui.graphics.Color,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = accent
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt().coerceIn(1, 5)) },
            valueRange = 1f..5f,
            steps = 3,
            colors = SliderDefaults.colors(
                thumbColor = accent,
                activeTrackColor = accent,
                inactiveTrackColor = accent.copy(alpha = 0.2f)
            )
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = leftLabel, style = MaterialTheme.typography.labelSmall, color = TextMuted)
            Text(text = rightLabel, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        }
    }
}

@Composable
fun FlavourSlider(value: Int, onValueChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    ScoreSlider(
        title = "Flavour",
        leftLabel = "Bland",
        rightLabel = "Exceptional",
        value = value,
        accent = Amber,
        onValueChange = onValueChange,
        modifier = modifier
    )
}

@Composable
fun PriceSlider(value: Int, onValueChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    ScoreSlider(
        title = "Price",
        leftLabel = "Overpriced",
        rightLabel = "Steal",
        value = value,
        accent = Teal,
        onValueChange = onValueChange,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun ScoreSliderPreview() {
    YorkLunchClubTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            FlavourSlider(value = 4, onValueChange = {})
            PriceSlider(value = 3, onValueChange = {})
        }
    }
}
