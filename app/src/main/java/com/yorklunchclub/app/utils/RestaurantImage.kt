package com.yorklunchclub.app.utils

object RestaurantImage {

    private const val ASSET_DIR = "restaurants"

    private val assetByRestaurantId = mapOf(
        "r01" to "$ASSET_DIR/bills_restaurant.jpeg",
        "r02" to "$ASSET_DIR/yuzu.jpeg",
        "r03" to "$ASSET_DIR/cosy_club.jpeg",
        "r04" to "$ASSET_DIR/prezzo.jpeg",
        "r05" to "$ASSET_DIR/losteria_italiana.jpeg",
        "r06" to "$ASSET_DIR/il_paradiso_del_cibo.jpeg",
        "r07" to "$ASSET_DIR/ambiente_tapas.jpeg",
        "r08" to "$ASSET_DIR/byron.jpg",
        "r09" to "$ASSET_DIR/burgsys.jpeg",
        "r10" to "$ASSET_DIR/rustique.jpeg"
    )

    fun resolve(restaurantId: String, imageUrl: String): String {
        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) return imageUrl
        if (imageUrl.startsWith("gs://")) return imageUrl
        val asset = assetByRestaurantId[restaurantId]
            ?: imageUrl.takeIf { it.isNotEmpty() }
            ?: return ""
        return "file:///android_asset/$asset"
    }
}
