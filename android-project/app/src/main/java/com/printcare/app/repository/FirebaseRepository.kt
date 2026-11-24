package com.printcare.app.repository

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.FieldValue
import com.printcare.app.firebase.FirebaseProvider
import com.printcare.app.model.Customer
import com.printcare.app.model.DemoCustomers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await

class FirebaseRepository(private val provider: FirebaseProvider) {
    val isConfigured: Boolean get() = provider.isConfigured

    fun observeAuthState(): Flow<String?> {
        val auth = provider.auth ?: return flowOf(null)
        return callbackFlow {
            val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                trySend(firebaseAuth.currentUser?.email)
            }
            auth.addAuthStateListener(listener)
            awaitClose { auth.removeAuthStateListener(listener) }
        }
    }

    suspend fun signIn(email: String, password: String) {
        requireAuth().signInWithEmailAndPassword(email.trim(), password).await()
    }

    suspend fun createAccount(name: String, email: String, password: String) {
        val result = requireAuth().createUserWithEmailAndPassword(email.trim(), password).await()
        val user = result.user ?: error("Account was created, but the signed-in user could not be loaded.")
        user.updateProfile(
            com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(name.trim())
                .build()
        ).await()
    }

    suspend fun sendPasswordReset(email: String) {
        requireAuth().sendPasswordResetEmail(email.trim()).await()
    }

    fun signOut() {
        requireAuth().signOut()
    }

    fun observeCustomers(): Flow<Result<List<Customer>>> {
        val collection = requireFirestore().collection(COLLECTION_CUSTOMERS)
        return callbackFlow {
            val registration: ListenerRegistration = collection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, exception ->
                    when {
                        exception != null -> trySend(Result.failure(exception))
                        snapshot != null -> trySend(
                            Result.success(snapshot.documents.mapNotNull { Customer.fromDocument(it) })
                        )
                    }
                }
            awaitClose { registration.remove() }
        }
    }

    suspend fun saveCustomer(
        existingCustomerId: String?,
        fullName: String,
        phoneNumber: String,
        dateOfBirth: String,
        email: String,
        address: String
    ): String {
        val collection = requireFirestore().collection(COLLECTION_CUSTOMERS)
        val customerId = existingCustomerId ?: collection.document().id
        val customerRef = collection.document(customerId)
        val values = mapOf(
            "fullName" to fullName.trim(),
            "phoneNumber" to phoneNumber.trim(),
            "dateOfBirth" to dateOfBirth.trim(),
            "email" to email.trim(),
            "address" to address.trim()
        )

        if (existingCustomerId == null) {
            customerRef.set(
                values + mapOf(
                    "customerId" to customerId,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } else {
            val existing = customerRef.get().await()
            check(existing.exists()) { "This customer no longer exists. Refresh the list and try again." }
            val createdAt = existing.getTimestamp("createdAt")
                ?: error("This older customer record has no registration date and cannot be edited.")
            customerRef.set(
                values + mapOf(
                    "customerId" to customerId,
                    "createdAt" to createdAt
                )
            ).await()
        }
        return customerId
    }

    suspend fun deleteCustomer(customerId: String) {
        requireFirestore().collection(COLLECTION_CUSTOMERS)
            .document(customerId)
            .delete()
            .await()
    }

    suspend fun seedDemoCustomers(): Int {
        val collection = requireFirestore().collection(COLLECTION_CUSTOMERS)
        val existingIds = collection.get().await().documents.mapTo(mutableSetOf()) { it.id }
        val missing = DemoCustomers.records.mapIndexedNotNull { index, demo ->
            val id = "demo_customer_${(index + 1).toString().padStart(2, '0')}"
            if (id in existingIds) null else id to demo
        }
        if (missing.isEmpty()) return 0

        val batch = requireFirestore().batch()
        missing.forEach { (id, demo) ->
            batch.set(collection.document(id), demo.toFirestoreMap(id))
        }
        batch.commit().await()
        return missing.size
    }

    private fun requireAuth(): FirebaseAuth = provider.auth
        ?: throw IllegalStateException(FIREBASE_SETUP_MESSAGE)

    private fun requireFirestore() = provider.firestore
        ?: throw IllegalStateException(FIREBASE_SETUP_MESSAGE)

    companion object {
        const val COLLECTION_CUSTOMERS = "customers"
        const val FIREBASE_SETUP_MESSAGE =
            "Firebase is not configured. Follow the setup steps in README.md and add app/google-services.json."
    }
}

fun Throwable.toUserMessage(): String = when (this) {
    is IllegalStateException -> message ?: "The requested operation is unavailable."
    is FirebaseNetworkException -> "Network unavailable. Check your connection and try again."
    is FirebaseAuthUserCollisionException -> "An account already exists for this email. Try signing in."
    is FirebaseAuthInvalidUserException -> "No account was found for this email."
    is FirebaseAuthInvalidCredentialsException -> "Email or password is incorrect."
    is FirebaseFirestoreException -> when (code) {
        FirebaseFirestoreException.Code.PERMISSION_DENIED ->
            "Access denied. Confirm that you are signed in and Firestore rules are deployed."
        FirebaseFirestoreException.Code.UNAVAILABLE ->
            "Firestore is temporarily unavailable. Check your connection and retry."
        else -> localizedMessage ?: "Firestore request failed."
    }
    else -> localizedMessage ?: "Something went wrong. Please try again."
}
