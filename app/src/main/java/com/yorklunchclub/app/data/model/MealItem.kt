package com.yorklunchclub.app.data.model

data class MealItem(
    val itemId: String = "",
    val menuItemId: String = "",
    val menuItemName: String = "",
    val flavourScore: Int = 1,
    val priceScore: Int = 1
)
