package com.printcare.app.firebase

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseProvider(context: Context) {
    private val app = FirebaseApp.getApps(context.applicationContext)
        .firstOrNull { it.name == "[DEFAULT]" }

    val auth: FirebaseAuth? = app?.let(FirebaseAuth::getInstance)
    val firestore: FirebaseFirestore? = app?.let(FirebaseFirestore::getInstance)
    val isConfigured: Boolean get() = auth != null && firestore != null
}
