package com.printcare.app.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.printcare.app.model.Customer
import com.printcare.app.ui.components.DetailRow
import com.printcare.app.ui.components.FormField
import com.printcare.app.ui.components.PrimaryAction
import com.printcare.app.ui.components.ScreenCard
import com.printcare.app.ui.components.ScreenTitle
import com.printcare.app.ui.components.SecondaryAction
import com.printcare.app.utils.ValidationUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CustomerListScreen(
    customers: List<Customer>,
    loading: Boolean,
    error: String?,
    onAddCustomer: () -> Unit,
    onOpenCustomer: (String) -> Unit,
    onRetry: () -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    val filtered = remember(query, customers) {
        val needle = query.trim().lowercase(Locale.getDefault())
        if (needle.isEmpty()) customers else customers.filter { customer ->
            customer.fullName.contains(needle, ignoreCase = true) ||
                customer.phoneNumber.contains(needle) ||
                customer.email.contains(needle, ignoreCase = true)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 16.dp)) {
        ScreenTitle(
            title = "Customers",
            subtitle = "${customers.size} registered",
            trailing = {
                IconButton(onClick = onAddCustomer) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Add customer")
                }
            }
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Search name, phone, or email") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )
        Spacer(Modifier.height(12.dp))

        when {
            loading -> Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) { CircularProgressIndicator() }

            error != null -> Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Unable to load customers", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                Text(error, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(12.dp))
                Button(onClick = onRetry) { Text("Retry") }
            }

            filtered.isEmpty() -> Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    if (query.isBlank()) "No customers yet" else "No matching customers",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    if (query.isBlank()) "Add a customer or seed fictional demo records."
                    else "Try another name, phone number, or email.",
                    color = MaterialTheme.colorScheme.secondary
                )
                if (query.isBlank()) {
                    Spacer(Modifier.height(14.dp))
                    Button(onClick = onAddCustomer) { Text("Add first customer") }
                }
            }

            else -> LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered, key = Customer::customerId) { customer ->
                    CustomerListCard(customer = customer, onClick = { onOpenCustomer(customer.customerId) })
                }
            }
        }
    }
}

@Composable
private fun CustomerListCard(customer: Customer, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(customer.fullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(customer.phoneNumber, color = MaterialTheme.colorScheme.primary)
            Text(customer.email, color = MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
fun CustomerDetailScreen(
    customer: Customer?,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            ScreenTitle(
                "Customer details",
                "Complete customer profile",
                modifier = Modifier.weight(1f)
            )
        }

        if (customer == null) {
            Text("Customer not found. It may have been removed.")
            return@Column
        }

        ScreenCard {
            Text(customer.fullName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            DetailRow("Customer ID", customer.customerId)
            DetailRow("Phone number", customer.phoneNumber)
            DetailRow("Date of birth", customer.dateOfBirth)
            DetailRow("Email address", customer.email)
            DetailRow("Full address", customer.address)
            DetailRow("Registered on", customer.createdAt?.toDate()?.let(::formatTimestamp) ?: "Pending sync")
        }

        Button(onClick = onEdit, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
            Icon(Icons.Default.Edit, contentDescription = null)
            Text("  Edit customer")
        }
        OutlinedButton(
            onClick = { showDeleteDialog = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            Text("  Delete customer", color = MaterialTheme.colorScheme.error)
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete this customer?") },
            text = { Text("This permanently removes ${customer?.fullName ?: "this customer"} from Firestore.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDelete()
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun CustomerFormScreen(
    customer: Customer?,
    customerId: String?,
    working: Boolean,
    onSave: (String, String, String, String, String) -> Unit,
    onCancel: () -> Unit
) {
    var name by rememberSaveable(customerId) { mutableStateOf("") }
    var phone by rememberSaveable(customerId) { mutableStateOf("") }
    var dateOfBirth by rememberSaveable(customerId) { mutableStateOf("") }
    var email by rememberSaveable(customerId) { mutableStateOf("") }
    var address by rememberSaveable(customerId) { mutableStateOf("") }
    var loadedCustomerId by rememberSaveable(customerId) { mutableStateOf<String?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var dateError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var addressError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    LaunchedEffect(customerId, customer) {
        if (customerId != null && customer != null && loadedCustomerId != customerId) {
            name = customer.fullName
            phone = customer.phoneNumber
            dateOfBirth = customer.dateOfBirth
            email = customer.email
            address = customer.address
            loadedCustomerId = customerId
        }
    }

    fun openDatePicker() {
        val initial = Calendar.getInstance()
        val parts = dateOfBirth.split("/")
        if (parts.size == 3) {
            runCatching {
                initial.set(parts[2].toInt(), parts[1].toInt() - 1, parts[0].toInt())
            }
        } else {
            initial.add(Calendar.YEAR, -25)
        }
        DatePickerDialog(
            context,
            { _, year, month, day ->
                dateOfBirth = String.format(Locale.US, "%02d/%02d/%04d", day, month + 1, year)
                dateError = null
            },
            initial.get(Calendar.YEAR),
            initial.get(Calendar.MONTH),
            initial.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.maxDate = System.currentTimeMillis()
        }.show()
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onCancel, enabled = !working) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Cancel and go back")
            }
            ScreenTitle(
                if (customerId == null) "New customer" else "Edit customer",
                "Customer details are stored securely in Firestore."
            )
        }

        ScreenCard {
            FormField("Full name", name, { name = it }, error = nameError, enabled = !working)
            FormField(
                "Phone number",
                phone,
                { phone = it.filter(Char::isDigit).take(10) },
                error = phoneError,
                keyboardType = KeyboardType.Phone,
                enabled = !working
            )
            FormField(
                "Date of birth (DD/MM/YYYY)",
                dateOfBirth,
                { },
                error = dateError,
                readOnly = true,
                enabled = !working,
                trailingIcon = {
                    IconButton(onClick = ::openDatePicker, enabled = !working) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Choose date of birth")
                    }
                }
            )
            FormField(
                "Email address",
                email,
                { email = it },
                error = emailError,
                keyboardType = KeyboardType.Email,
                enabled = !working
            )
            FormField(
                "Full address",
                address,
                { address = it },
                error = addressError,
                enabled = !working,
                singleLine = false
            )

            PrimaryAction(
                text = if (customerId == null) "Save customer" else "Save changes",
                enabled = !working,
                working = working,
                onClick = {
                    nameError = ValidationUtils.validateName(name)
                    phoneError = ValidationUtils.validatePhone(phone)
                    dateError = ValidationUtils.validateDateOfBirth(dateOfBirth)
                    emailError = ValidationUtils.validateEmail(email)
                    addressError = ValidationUtils.validateAddress(address)
                    if (nameError == null && phoneError == null && dateError == null &&
                        emailError == null && addressError == null
                    ) {
                        onSave(name, phone, dateOfBirth, email, address)
                    }
                }
            )
            SecondaryAction("Cancel", onCancel)
        }
    }
}

private fun formatTimestamp(date: Date): String =
    SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(date)
