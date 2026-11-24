package com.printcare.app.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot

data class Customer(
    val customerId: String,
    val fullName: String,
    val phoneNumber: String,
    val dateOfBirth: String,
    val email: String,
    val address: String,
    val createdAt: Timestamp?
) {
    companion object {
        fun fromDocument(document: DocumentSnapshot): Customer? {
            val fullName = document.getString("fullName")
                ?: document.getString("name")
                ?: return null
            return Customer(
                customerId = document.id,
                fullName = fullName,
                phoneNumber = document.getString("phoneNumber")
                    ?: document.getString("phone").orEmpty(),
                dateOfBirth = document.getString("dateOfBirth")
                    ?: document.getString("dob").orEmpty(),
                email = document.getString("email").orEmpty(),
                address = document.getString("address").orEmpty(),
                createdAt = document.getTimestamp("createdAt")
            )
        }
    }
}
