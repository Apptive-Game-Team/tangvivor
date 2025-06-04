package com.dudoji.tangvivor.matching.service

import com.dudoji.tangvivor.repository.UserRepository
import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.dudoji.tangvivor.BaseDrawerActivity
import com.dudoji.tangvivor.matching.entity.User
import com.dudoji.tangvivor.repository.GameRepository
import com.google.android.gms.nearby.connection.Payload
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.json.JSONObject

class NearbyUserController(val context: Context,
                           onDiscoverChanged: () -> Unit) {
    val nearbyController: NearbyController

    init {
        nearbyController = NearbyController(
            context,
            JsonPayloadCallback { jsonObject ->
                val userId = jsonObject.getString("userId")
                val sessionId = jsonObject.getString("sessionId")
                UserRepository.setEnemy(userId)
                AlertDialog.Builder(context)
                    .setTitle("초대")
                    .setMessage("${userId}님이 초대하였습니다. 방에 참여하시겠습니까?")
                    .setPositiveButton("확인") { dialog, _ ->
                        GameRepository.enterGame(sessionId, context as BaseDrawerActivity, 2)
                        dialog.dismiss()
                    }
                    .setNegativeButton("취소") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            },
            onDiscoverChanged
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