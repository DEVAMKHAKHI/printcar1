package com.printcare.app

import android.app.Application
import com.printcare.app.firebase.FirebaseProvider

class PrintShopApplication : Application() {
    lateinit var firebaseProvider: FirebaseProvider
        private set

    override fun onCreate() {
        super.onCreate()
        firebaseProvider = FirebaseProvider(this)
    }
}
