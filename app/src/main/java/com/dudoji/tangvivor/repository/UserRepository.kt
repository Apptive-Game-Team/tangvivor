package com.dudoji.tangvivor.repository

import com.dudoji.tangvivor.matching.entity.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object UserRepository {
    val COLLECTION_NAME = "users"
    val db = FirebaseFirestore.getInstance()

    lateinit var me: User
    lateinit var enemy: User
    val emptyUser: User = User()

    suspend fun login(loginText: String): User {
        me = db.collection(COLLECTION_NAME)
            .document(loginText)
            .get()
            .await()
            .toObject(User::class.java) ?: throw Exception("사용자 없음")
        me.id = loginText
        return me
    }

    fun setEnemyUser(user: User) {
        enemy = user
    }

    suspend fun getUser(text: String): User {
        return db.collection(COLLECTION_NAME)
            .document(text)
            .get()
            .await()
            .toObject(User::class.java) ?: throw Exception("사용자 없음")
    }

    suspend fun saveUser(user: User): Boolean {
        return try {
            db.collection(COLLECTION_NAME).add(user).await()
            true
        } catch (e: Exception) {
            false
        }
    }
}