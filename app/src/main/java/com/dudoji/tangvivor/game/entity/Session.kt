package com.dudoji.tangvivor.game.entity

import com.dudoji.tangvivor.DEFAULT_HP


data class Session(var user1X: Float, var user2X: Float, var user1Point: Float, var user2Point: Float, var user1Hp: Int = DEFAULT_HP , var user2Hp: Int = DEFAULT_HP, ) {
    constructor() : this(0.5f, 0.5f, 0.5f, 0.5f)

    constructor(master: Master) : this(-1f, -1f,-1f,-1f, -1, -1) {
//        when (master) {
//            Master.User1 -> {
//                user1X = 0.5f
//                user2X = -1f
//                user1Point = 0.5f
//                user2Point = -1f
//                user1Hp = -1
//                user2Hp = -1
//            }
//            Master.User2 -> {
//                user1X = -1f
//                user2X = 0.5f
//                user1Point = -1f
//                user2Point = 0.5f
//                user1Hp = -1
//                user2Hp = -1
//            }
//        }

    }

    fun toMap(session: Session) : Map<String, Any> {
        val map = mutableMapOf<String, Any>()

        if (user1X != -1f) map["user1X"] = user1X
        if (user2X != -1f) map["user2X"] = user2X
        if (user1Point != -1f) map["user1Point"] = user1Point
        if (user2Point != -1f) map["user2Point"] = user2Point
        if (user1Hp != -1) map["user1Hp"] = user1Hp + session.user1Hp
        if (user2Hp != -1) map["user2Hp"] = user2Hp + session.user2Hp

        return map
    }

    fun toSetHp() {
        user1Hp = -1
        user2Hp = -1
    }
}
