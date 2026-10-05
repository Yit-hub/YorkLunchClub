package com.yorklunchclub.app.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.yorklunchclub.app.ui.theme.AmberLight
import com.yorklunchclub.app.ui.theme.TextMuted
import com.yorklunchclub.app.utils.RestaurantImage

private const val ASSET_PREFIX = "file:///android_asset/"

@Composable
fun RestaurantPhoto(
    restaurantId: String,
    imageUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val resolved = RestaurantImage.resolve(restaurantId, imageUrl)
    when {
        resolved.startsWith(ASSET_PREFIX) -> AssetImage(
            assetPath = resolved.removePrefix(ASSET_PREFIX),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
        resolved.startsWith("http://") || resolved.startsWith("https://") || resolved.startsWith("gs://") ->
            AsyncImage(
                model = resolved,
                contentDescription = contentDescription,
                modifier = modifier.background(AmberLight),
                contentScale = contentScale
            )
        else -> Placeholder(modifier)
    }
}

@Composable
private fun AssetImage(
    assetPath: String,
    contentDescription: String?,
    modifier: Modifier,
    contentScale: ContentScale
) {
    val context = LocalContext.current
    val bitmap = remember(assetPath) {
        runCatching {
            context.assets.open(assetPath).use { BitmapFactory.decodeStream(it) }
        }.getOrNull()
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        Placeholder(modifier)
    }
}

@Composable
private fun Placeholder(modifier: Modifier) {
    Box(modifier = modifier.background(AmberLight), contentAlignment = Alignment.Center) {
        Text("No image", style = MaterialTheme.typography.labelSmall, color = TextMuted)
    }
}
