package com.printcare.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.printcare.app.model.Customer
import com.printcare.app.repository.FirebaseRepository
import com.printcare.app.repository.toUserMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainUiState(
    val firebaseConfigured: Boolean = false,
    val authReady: Boolean = false,
    val userEmail: String? = null,
    val customers: List<Customer> = emptyList(),
    val customersLoading: Boolean = false,
    val customersError: String? = null,
    val working: Boolean = false,
    val feedback: String? = null
)

class MainViewModel(private val repository: FirebaseRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(
        MainUiState(firebaseConfigured = repository.isConfigured)
    )
    val state: StateFlow<MainUiState> = mutableState.asStateFlow()
    private var customerSubscription: Job? = null
    private var operationInProgress = false

    init {
        viewModelScope.launch {
            repository.observeAuthState().collect { email ->
                mutableState.update {
                    it.copy(
                        authReady = true,
                        userEmail = email,
                        customers = if (email == null) emptyList() else it.customers,
                        customersLoading = email != null && it.customers.isEmpty(),
                        customersError = null
                    )
                }
                if (email == null) stopCustomerSubscription() else startCustomerSubscription()
            }
        }
    }

    fun signIn(email: String, password: String) = perform("Signed in successfully.") {
        repository.signIn(email, password)
    }

    fun createAccount(name: String, email: String, password: String) =
        perform("Account created successfully.") {
            repository.createAccount(name, email, password)
        }

    fun sendPasswordReset(email: String) =
        perform("Password reset email sent. Check your inbox.") {
            repository.sendPasswordReset(email)
        }

    fun signOut() = perform("Signed out.") {
        repository.signOut()
    }

    fun saveCustomer(
        customerId: String?,
        fullName: String,
        phoneNumber: String,
        dateOfBirth: String,
        email: String,
        address: String,
        onComplete: (Boolean) -> Unit
    ) {
        perform(
            successMessage = if (customerId == null) "Customer saved." else "Customer updated.",
            onComplete = onComplete
        ) {
            repository.saveCustomer(customerId, fullName, phoneNumber, dateOfBirth, email, address)
        }
    }

    fun deleteCustomer(customerId: String, onComplete: (Boolean) -> Unit) =
        perform("Customer deleted.", onComplete) {
            repository.deleteCustomer(customerId)
        }

    fun seedDemoCustomers(onComplete: (Boolean) -> Unit = {}) =
        perform(
            successMessage = "Demo customer seed complete.",
            onComplete = onComplete
        ) {
            val added = repository.seedDemoCustomers()
            if (added == 0) "All 10 demo customers are already present."
            else "Added $added fictional demo customers."
        }

    fun customerById(customerId: String): Customer? =
        state.value.customers.firstOrNull { it.customerId == customerId }

    fun retryCustomers() {
        if (state.value.userEmail != null) startCustomerSubscription()
    }

    fun clearFeedback() {
        mutableState.update { it.copy(feedback = null) }
    }

    private fun perform(
        successMessage: String,
        onComplete: (Boolean) -> Unit = {},
        operation: suspend () -> Any?
    ) {
        if (operationInProgress) return
        operationInProgress = true
        viewModelScope.launch {
            mutableState.update { it.copy(working = true, feedback = null) }
            var succeeded = false
            try {
                val result = operation()
                mutableState.update {
                    it.copy(feedback = (result as? String) ?: successMessage)
                }
                succeeded = true
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                mutableState.update { it.copy(feedback = error.toUserMessage()) }
            } finally {
                mutableState.update { it.copy(working = false) }
                operationInProgress = false
            }
            onComplete(succeeded)
        }
    }

    private fun startCustomerSubscription() {
        customerSubscription?.cancel()
        mutableState.update { it.copy(customersLoading = true, customersError = null) }
        customerSubscription = viewModelScope.launch {
            repository.observeCustomers().collect { result ->
                result.onSuccess { customers ->
                    mutableState.update {
                        it.copy(
                            customers = customers,
                            customersLoading = false,
                            customersError = null
                        )
                    }
                }.onFailure { error ->
                    mutableState.update {
                        it.copy(
                            customersLoading = false,
                            customersError = error.toUserMessage(),
                            feedback = error.toUserMessage()
                        )
                    }
                }
            }
        }
    }

    private fun stopCustomerSubscription() {
        customerSubscription?.cancel()
        customerSubscription = null
        mutableState.update {
            it.copy(customers = emptyList(), customersLoading = false, customersError = null)
        }
    }
}

class MainViewModelFactory(
    private val repository: FirebaseRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(MainViewModel::class.java)) {
            "Unsupported ViewModel type: ${modelClass.name}"
        }
        return MainViewModel(repository) as T
    }
}
