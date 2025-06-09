package com.dudoji.tangvivor.matching.entity

import android.net.Uri

class User {
    var id: String? = null
    var name: String? = null
    var score: Int = -1

    constructor() { }

    constructor(id: String, name: String, score: Int) {
        this.id = id
        this.name = name
        this.score = score
    }
}