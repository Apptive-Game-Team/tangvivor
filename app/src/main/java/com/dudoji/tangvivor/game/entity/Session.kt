package com.dudoji.tangvivor.game.entity

import com.dudoji.tangvivor.DEFAULT_HP


data class Session(var user1X: Float, var user2X: Float, var user1Point: Float, var user2Point: Float, var user1Hp: Int = DEFAULT_HP , var user2Hp: Int = DEFAULT_HP, var tang1: Boolean = false, var tang2: Boolean = false) {
    constructor() : this(0.5f, 0.5f, 0.5f, 0.5f, )

    constructor(master: Master) : this(-1f, -1f,-1f,-1f, -1, -1)

    fun toMap(session: Session) : Map<String, Any> {
        val map = mutableMapOf<String, Any>()

        if (user1X != -1f) map["user1X"] = user1X
        if (user2X != -1f) map["user2X"] = user2X
        if (user1Point != -1f) map["user1Point"] = user1Point
        if (user2Point != -1f) map["user2Point"] = user2Point
        if (user1Hp != -1) map["user1Hp"] = user1Hp + session.user1Hp
        if (user2Hp != -1) map["user2Hp"] = user2Hp + session.user2Hp
        map["tang1"] = tang1
        map["tang2"] = tang2

        return map
    }

    fun setTang(me: Int) {
        if (me == 1) tang1 = true
        else if (me == 2) tang2 = true
    }

    fun getTang(me: Int): Boolean {
        return if (me != 1) tang1 else tang2
    }

    fun initialize() {
        user1Hp = -1
        user2Hp = -1
        tang1 = false
        tang2 = false
    }
}
