package com.dudoji.tangvivor.repository

import com.dudoji.tangvivor.matching.entity.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object UserRepository {
    val COLLECTION_NAME = "users"
    val db = FirebaseFirestore.getInstance()

    var me: User? = null
    var enemy: User? = null

    suspend fun getUser(text: String): User {
        return db.collection(COLLECTION_NAME)
            .document(text)
            .get()
            .await()
            .toObject(User::class.java) ?: throw Exception("사용자 없음")
    }

    suspend fun saveUser(user: User): Boolean {
        val already = db.collection(COLLECTION_NAME)
            .document(user.id!!)
            .get()
            .await()
            .exists()
        if (!already) {
            return try {
                db.collection(COLLECTION_NAME)
                    .document(user.id!!)
                    .set(user).await()
                true
            } catch (e: Exception) {
                false
            }
        }
        return false
    }
}