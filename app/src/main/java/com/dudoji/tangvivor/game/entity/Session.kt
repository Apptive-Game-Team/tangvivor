package com.dudoji.tangvivor.game.entity

import com.dudoji.tangvivor.DEFAULT_HP


data class Session(var user1X: Float, var user2X: Float, var user1Point: Float, var user2Point: Float, var user1Hp: Int = DEFAULT_HP , var user2Hp: Int = DEFAULT_HP, ) {
    constructor() : this(0.5f, 0.5f, 0.5f, 0.5f)

    fun toMap(): Map<String, Any> {
        return mapOf(
            "user1X" to user1X,
            "user2X" to user2X,
            "user1Point" to user1Point,
            "user2Point" to user2Point,
            "user1Hp" to user1Hp,
            "user2Hp" to user2Hp
        )
    }
}
