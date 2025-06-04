package com.dudoji.tangvivor.repository

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import com.dudoji.tangvivor.matching.entity.Room
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object RoomRepository {
    val COLLECTION_NAME = "matchings"
    val db = FirebaseFirestore.getInstance()

    fun createRoom(roomName: String, activity: ComponentActivity) {
        activity.lifecycleScope.launch{
            val room = Room(name = roomName, user1 = UserRepository.me?.id, user2 = "")
            db.collection(COLLECTION_NAME).document(roomName).set(room).addOnSuccessListener { documentReference ->
                Toast.makeText(activity, "Room created with ID", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { e ->
                Toast.makeText(activity, "Failed to create room", Toast.LENGTH_SHORT).show()
            }
        }
    }

    suspend fun getRooms(): List<Room> {
        return db.collection(COLLECTION_NAME)
            .get()
            .await()
            .documents
            .filter { !it.getString("name").equals("placeholder") }
            .mapNotNull { it.toObject(Room::class.java) }
    }
}