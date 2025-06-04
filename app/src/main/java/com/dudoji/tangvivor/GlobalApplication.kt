package com.dudoji.tangvivor

import android.app.Application
import com.google.android.gms.games.PlayGamesSdk

class GlobalApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        PlayGamesSdk.initialize(this);
    }
}