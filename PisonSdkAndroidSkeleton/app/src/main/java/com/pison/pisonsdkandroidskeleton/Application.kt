package com.pison.pisonsdkandroidskeleton

import android.app.Application
import android.content.Context
import com.pison.client.PisonSdk
import com.pison.client.bindToServer

class Application : Application() {
    companion object {
        var appContext: Context? = null
        val sdk = PisonSdk.bindToServer("192.168.0.29", 8090)
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
    }
}