package com.yorklunchclub.app.data.model

import com.google.firebase.Timestamp

data class Meal(
    val mealId: String = "",
    val userId: String = "",
    val restaurantId: String = "",
    val restaurantName: String = "",
    val date: Timestamp = Timestamp.now(),
    val items: List<MealItem> = emptyList()
)
