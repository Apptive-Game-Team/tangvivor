package com.dudoji.tangvivor.matching.service

import com.dudoji.tangvivor.repository.UserRepository
import android.content.Context
import com.dudoji.tangvivor.matching.entity.User
import com.google.android.gms.nearby.connection.Payload
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.json.JSONObject

class NearbyUserController(context: Context) {
    val nearbyController: NearbyController

    init {
        nearbyController = NearbyController(
            context,
            JsonPayloadCallback { jsonObject ->
                val userId = jsonObject.getString("userId")
                val sessionId = jsonObject.getString("sessionId")
                UserRepository.setEnemy(userId)
            }
        )
    }

    fun invite(user: User, sessionId: String) {
        val userId = user.id ?: throw IllegalArgumentException("User ID cannot be null")
        val jsonObject: JSONObject = JSONObject()
        jsonObject.put("inviter", UserRepository.me?.id ?: "")
        jsonObject.put("sessionId", sessionId)
        nearbyController.connectToEndpoint(userId)
        val payload: Payload = Payload.fromBytes(
            jsonObject.toString().toByteArray()
        )
        nearbyController.sendPayload(userId, payload)
        nearbyController.disconnectFromEndpoint(userId)
    }

    suspend fun getNearbyUsers(): List<User> {
        val flow = nearbyController.discoveredEndpointIds.asFlow()
        val result: List<User> = flow.map { value ->
            UserRepository.getUser(value)
        }.toList()

        return result
    }
}