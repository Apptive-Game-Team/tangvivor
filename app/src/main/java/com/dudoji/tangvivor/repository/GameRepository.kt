package com.dudoji.tangvivor.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object GameRepository {
    val db = FirebaseFirestore.getInstance()
    val COLLECTION_NAME = "sessions"

    suspend fun saveGame(name: String): Boolean {
        return try {
            db.collection(COLLECTION_NAME).document(name).set(mapOf("user1X" to 0.5f, "user2X" to 0.5f)).await()
            true
        } catch (e: Exception) {
            false
        }
    }
}