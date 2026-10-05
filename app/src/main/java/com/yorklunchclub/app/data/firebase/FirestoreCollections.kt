package com.yorklunchclub.app.data.firebase

object FirestoreCollections {

    object Users {
        const val COLLECTION = "users"
        const val FIELD_FULL_NAME = "fullName"
        const val FIELD_USERNAME = "username"
    }

    object Restaurants {
        const val COLLECTION = "restaurants"
        const val FIELD_NAME = "name"
        const val FIELD_ADDRESS = "address"
        const val FIELD_DESCRIPTION = "description"
        const val FIELD_CUISINE = "cuisine"
        const val FIELD_IMAGE_URL = "imageUrl"
        const val FIELD_GALLERY_URLS = "galleryUrls"
        const val FIELD_AVG_FLAVOUR_SCORE = "avgFlavourScore"
        const val FIELD_AVG_PRICE_SCORE = "avgPriceScore"

        object Menu {
            const val SUBCOLLECTION = "menu"
            const val FIELD_NAME = "name"
            const val FIELD_PRICE = "price"
        }
    }

    object Meals {
        const val COLLECTION = "meals"
        const val FIELD_USER_ID = "userId"
        const val FIELD_RESTAURANT_ID = "restaurantId"
        const val FIELD_RESTAURANT_NAME = "restaurantName"
        const val FIELD_DATE = "date"

        object Items {
            const val SUBCOLLECTION = "items"
            const val FIELD_MENU_ITEM_ID = "menuItemId"
            const val FIELD_MENU_ITEM_NAME = "menuItemName"
            const val FIELD_FLAVOUR_SCORE = "flavourScore"
            const val FIELD_PRICE_SCORE = "priceScore"
        }
    }
}
