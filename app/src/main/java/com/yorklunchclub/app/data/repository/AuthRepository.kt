package com.yorklunchclub.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.yorklunchclub.app.data.firebase.FirestoreCollections
import com.yorklunchclub.app.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    val currentUser: FirebaseUser? get() = auth.currentUser
    val currentUserId: String? get() = auth.currentUser?.uid

    fun authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun register(email: String, password: String, fullName: String, username: String): Result<String> = runCatching {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val firebaseUser = result.user ?: error("Missing user after register")
        val uid = firebaseUser.uid
        val data = mapOf(
            FirestoreCollections.Users.FIELD_FULL_NAME to fullName,
            FirestoreCollections.Users.FIELD_USERNAME to username
        )
        try {
            firestore.collection(FirestoreCollections.Users.COLLECTION)
                .document(uid)
                .set(data)
                .await()
        } catch (t: Throwable) {
            // Roll back the auth user so re-registration isn't blocked by "email already in use".
            runCatching { firebaseUser.delete().await() }
            throw t
        }
        uid
    }

    suspend fun login(email: String, password: String): Result<String> = runCatching {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        result.user?.uid ?: error("Missing UID after login")
    }

    suspend fun fetchUserProfile(uid: String): Result<User> = runCatching {
        val snap = firestore.collection(FirestoreCollections.Users.COLLECTION)
            .document(uid)
            .get()
            .await()
        User(
            userId = uid,
            fullName = snap.getString(FirestoreCollections.Users.FIELD_FULL_NAME).orEmpty(),
            username = snap.getString(FirestoreCollections.Users.FIELD_USERNAME).orEmpty()
        )
    }

    fun logout() {
        auth.signOut()
    }
}
