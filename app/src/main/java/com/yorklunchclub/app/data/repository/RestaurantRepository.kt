package com.yorklunchclub.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.yorklunchclub.app.data.firebase.FirestoreCollections
import com.yorklunchclub.app.data.model.MenuItem
import com.yorklunchclub.app.data.model.Restaurant
import kotlinx.coroutines.tasks.await

class RestaurantRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun fetchAll(): Result<List<Restaurant>> = runCatching {
        val snap = firestore.collection(FirestoreCollections.Restaurants.COLLECTION)
            .get()
            .await()
        snap.documents.map { it.toRestaurant() }
    }

    suspend fun fetchById(restaurantId: String): Result<Restaurant?> = runCatching {
        val doc = firestore.collection(FirestoreCollections.Restaurants.COLLECTION)
            .document(restaurantId)
            .get()
            .await()
        if (!doc.exists()) null else doc.toRestaurant()
    }

    suspend fun fetchMenu(restaurantId: String): Result<List<MenuItem>> = runCatching {
        val snap = firestore.collection(FirestoreCollections.Restaurants.COLLECTION)
            .document(restaurantId)
            .collection(FirestoreCollections.Restaurants.Menu.SUBCOLLECTION)
            .get()
            .await()
        snap.documents.map { doc ->
            MenuItem(
                menuItemId = doc.id,
                name = doc.getString(FirestoreCollections.Restaurants.Menu.FIELD_NAME).orEmpty(),
                price = doc.getDouble(FirestoreCollections.Restaurants.Menu.FIELD_PRICE) ?: 0.0
            )
        }
    }

    suspend fun updateAverages(
        restaurantId: String,
        avgFlavourScore: Double?,
        avgPriceScore: Double?
    ): Result<Unit> = runCatching {
        val data = mapOf(
            FirestoreCollections.Restaurants.FIELD_AVG_FLAVOUR_SCORE to avgFlavourScore,
            FirestoreCollections.Restaurants.FIELD_AVG_PRICE_SCORE to avgPriceScore
        )
        firestore.collection(FirestoreCollections.Restaurants.COLLECTION)
            .document(restaurantId)
            .set(data, com.google.firebase.firestore.SetOptions.merge())
            .await()
    }
}

private fun com.google.firebase.firestore.DocumentSnapshot.toRestaurant(): Restaurant =
    Restaurant(
        restaurantId = id,
        name = getString(FirestoreCollections.Restaurants.FIELD_NAME).orEmpty(),
        address = getString(FirestoreCollections.Restaurants.FIELD_ADDRESS).orEmpty(),
        description = getString(FirestoreCollections.Restaurants.FIELD_DESCRIPTION).orEmpty(),
        cuisine = getString(FirestoreCollections.Restaurants.FIELD_CUISINE).orEmpty(),
        imageUrl = getString(FirestoreCollections.Restaurants.FIELD_IMAGE_URL).orEmpty(),
        galleryUrls = (get(FirestoreCollections.Restaurants.FIELD_GALLERY_URLS) as? List<*>)
            ?.filterIsInstance<String>()
            .orEmpty(),
        avgFlavourScore = getDouble(FirestoreCollections.Restaurants.FIELD_AVG_FLAVOUR_SCORE),
        avgPriceScore = getDouble(FirestoreCollections.Restaurants.FIELD_AVG_PRICE_SCORE)
    )
