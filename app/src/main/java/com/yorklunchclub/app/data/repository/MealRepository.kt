package com.yorklunchclub.app.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.yorklunchclub.app.data.firebase.FirestoreCollections
import com.yorklunchclub.app.data.model.Meal
import com.yorklunchclub.app.data.model.MealItem
import com.yorklunchclub.app.utils.ScoreCalculator
import kotlinx.coroutines.tasks.await

class MealRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val restaurantRepository: RestaurantRepository = RestaurantRepository(firestore)
) {

    suspend fun fetchUserMeals(userId: String): Result<List<Meal>> = runCatching {
        // Single-field where + client-side sort: avoids needing a composite index for (userId, date).
        val mealsSnap = firestore.collection(FirestoreCollections.Meals.COLLECTION)
            .whereEqualTo(FirestoreCollections.Meals.FIELD_USER_ID, userId)
            .get()
            .await()
        mealsSnap.documents
            .map { it.toMealWithItems() }
            .sortedByDescending { it.date.seconds }
    }

    suspend fun fetchMealsForRestaurant(userId: String, restaurantId: String): Result<List<Meal>> =
        runCatching {
            val mealsSnap = firestore.collection(FirestoreCollections.Meals.COLLECTION)
                .whereEqualTo(FirestoreCollections.Meals.FIELD_USER_ID, userId)
                .get()
                .await()
            mealsSnap.documents
                .filter { it.getString(FirestoreCollections.Meals.FIELD_RESTAURANT_ID) == restaurantId }
                .map { it.toMealWithItems() }
                .sortedByDescending { it.date.seconds }
        }

    suspend fun fetchMealById(mealId: String): Result<Meal?> = runCatching {
        val doc = firestore.collection(FirestoreCollections.Meals.COLLECTION)
            .document(mealId)
            .get()
            .await()
        if (!doc.exists()) return@runCatching null
        doc.toMealWithItems()
    }

    suspend fun saveMeal(meal: Meal): Result<String> = runCatching {
        require(meal.userId.isNotEmpty()) { "Meal must have userId for security rule" }
        require(meal.items.isNotEmpty()) { "Meal must have at least one item" }
        meal.items.forEach {
            require(it.flavourScore in 1..5) { "flavourScore out of range" }
            require(it.priceScore in 1..5) { "priceScore out of range" }
        }
        val mealsCol = firestore.collection(FirestoreCollections.Meals.COLLECTION)
        val mealRef = mealsCol.document()
        val mealData = mapOf(
            FirestoreCollections.Meals.FIELD_USER_ID to meal.userId,
            FirestoreCollections.Meals.FIELD_RESTAURANT_ID to meal.restaurantId,
            FirestoreCollections.Meals.FIELD_RESTAURANT_NAME to meal.restaurantName,
            FirestoreCollections.Meals.FIELD_DATE to meal.date
        )
        mealRef.set(mealData).await()
        meal.items.forEach { item ->
            val itemRef = mealRef.collection(FirestoreCollections.Meals.Items.SUBCOLLECTION).document()
            itemRef.set(item.toFirestoreMap()).await()
        }
        recomputeRestaurantAverages(meal.restaurantId)
        mealRef.id
    }

    suspend fun updateMealItems(mealId: String, items: List<MealItem>): Result<Unit> = runCatching {
        require(items.isNotEmpty()) { "Meal must have at least one item" }
        items.forEach {
            require(it.flavourScore in 1..5) { "flavourScore out of range" }
            require(it.priceScore in 1..5) { "priceScore out of range" }
        }
        val mealRef = firestore.collection(FirestoreCollections.Meals.COLLECTION).document(mealId)
        val mealDoc = mealRef.get().await()
        if (!mealDoc.exists()) error("Meal not found: $mealId")
        val restaurantId = mealDoc.getString(FirestoreCollections.Meals.FIELD_RESTAURANT_ID).orEmpty()

        val itemsCol = mealRef.collection(FirestoreCollections.Meals.Items.SUBCOLLECTION)
        val existing = itemsCol.get().await()
        existing.documents.forEach { it.reference.delete().await() }

        items.forEach { item ->
            itemsCol.document().set(item.toFirestoreMap()).await()
        }

        if (restaurantId.isNotEmpty()) {
            recomputeRestaurantAverages(restaurantId)
        }
    }

    /**
     * Rebuilds avgFlavourScore + avgPriceScore for one restaurant from ALL users' meals.
     *
     * For each meal at this restaurant, compute the visit average of flavour scores and the
     * visit average of price scores; then average those visit averages. Writes both back to
     * restaurants/{id}. If no meals exist, writes null on both fields.
     */
    private suspend fun recomputeRestaurantAverages(restaurantId: String) {
        val mealsSnap = firestore.collection(FirestoreCollections.Meals.COLLECTION)
            .whereEqualTo(FirestoreCollections.Meals.FIELD_RESTAURANT_ID, restaurantId)
            .get()
            .await()

        val visitFlavourAvgs = mutableListOf<Double>()
        val visitPriceAvgs = mutableListOf<Double>()

        for (mealDoc in mealsSnap.documents) {
            val itemsSnap = mealDoc.reference
                .collection(FirestoreCollections.Meals.Items.SUBCOLLECTION)
                .get()
                .await()
            if (itemsSnap.isEmpty) continue
            val flavours = itemsSnap.documents
                .map { (it.getLong(FirestoreCollections.Meals.Items.FIELD_FLAVOUR_SCORE) ?: 0L).toInt() }
            val prices = itemsSnap.documents
                .map { (it.getLong(FirestoreCollections.Meals.Items.FIELD_PRICE_SCORE) ?: 0L).toInt() }
            visitFlavourAvgs += ScoreCalculator.visitAverage(flavours)
            visitPriceAvgs += ScoreCalculator.visitAverage(prices)
        }

        val avgFlavour = if (visitFlavourAvgs.isEmpty()) null else ScoreCalculator.restaurantAverage(visitFlavourAvgs)
        val avgPrice = if (visitPriceAvgs.isEmpty()) null else ScoreCalculator.restaurantAverage(visitPriceAvgs)

        restaurantRepository.updateAverages(restaurantId, avgFlavour, avgPrice).getOrThrow()
    }

    private fun MealItem.toFirestoreMap(): Map<String, Any> = mapOf(
        FirestoreCollections.Meals.Items.FIELD_MENU_ITEM_ID to menuItemId,
        FirestoreCollections.Meals.Items.FIELD_MENU_ITEM_NAME to menuItemName,
        FirestoreCollections.Meals.Items.FIELD_FLAVOUR_SCORE to flavourScore,
        FirestoreCollections.Meals.Items.FIELD_PRICE_SCORE to priceScore
    )

    private suspend fun DocumentSnapshot.toMealWithItems(): Meal {
        val itemsSnap = reference
            .collection(FirestoreCollections.Meals.Items.SUBCOLLECTION)
            .get()
            .await()
        val items = itemsSnap.documents.map { itemDoc ->
            MealItem(
                itemId = itemDoc.id,
                menuItemId = itemDoc.getString(FirestoreCollections.Meals.Items.FIELD_MENU_ITEM_ID).orEmpty(),
                menuItemName = itemDoc.getString(FirestoreCollections.Meals.Items.FIELD_MENU_ITEM_NAME).orEmpty(),
                flavourScore = (itemDoc.getLong(FirestoreCollections.Meals.Items.FIELD_FLAVOUR_SCORE) ?: 1L).toInt(),
                priceScore = (itemDoc.getLong(FirestoreCollections.Meals.Items.FIELD_PRICE_SCORE) ?: 1L).toInt()
            )
        }
        return Meal(
            mealId = id,
            userId = getString(FirestoreCollections.Meals.FIELD_USER_ID).orEmpty(),
            restaurantId = getString(FirestoreCollections.Meals.FIELD_RESTAURANT_ID).orEmpty(),
            restaurantName = getString(FirestoreCollections.Meals.FIELD_RESTAURANT_NAME).orEmpty(),
            date = getTimestamp(FirestoreCollections.Meals.FIELD_DATE) ?: Timestamp.now(),
            items = items
        )
    }
}
