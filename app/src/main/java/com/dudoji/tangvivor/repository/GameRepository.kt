package com.dudoji.tangvivor.repository

import com.dudoji.tangvivor.game.entity.Session
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object GameRepository {
    val db = FirebaseFirestore.getInstance()
    val COLLECTION_NAME = "sessions"

    suspend fun saveGame(name: String): Boolean {
        return try {
            db.collection(COLLECTION_NAME).document(name).set(Session()).await()
            true
        } catch (e: Exception) {
            false
        }
    }
}