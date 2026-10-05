package com.yorklunchclub.app.data.model

data class Restaurant(
    val restaurantId: String = "",
    val name: String = "",
    val address: String = "",
    val description: String = "",
    val cuisine: String = "",
    val imageUrl: String = "",
    val galleryUrls: List<String> = emptyList(),
    val avgFlavourScore: Double? = null,
    val avgPriceScore: Double? = null
)
