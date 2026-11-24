package com.printcare.app.utils

import android.util.Patterns
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Pure validation helper functions for authentication and customer registration forms.
 */
object ValidationUtils {

    private val INDIAN_PHONE_REGEX = Regex("^[6-9][0-9]{9}$")
    private val NAME_REGEX = Regex("^[A-Za-z][A-Za-z .'-]{1,99}$")
    private val DOB_REGEX = Regex("^(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[0-2])/(19|20)[0-9]{2}$")

    fun validateEmail(email: String): String? {
        val trimmed = email.trim()
        return when {
            trimmed.isEmpty() -> "Email address is required"
            !Patterns.EMAIL_ADDRESS.matcher(trimmed).matches() -> "Enter a valid email address"
            else -> null
        }
    }

    fun validatePassword(password: String): String? {
        return when {
            password.isEmpty() -> "Password is required"
            password.length < 6 -> "Password must be at least 6 characters"
            else -> null
        }
    }

    fun validateConfirmPassword(password: String, confirmPassword: String): String? {
        return when {
            confirmPassword.isEmpty() -> "Please confirm your password"
            password != confirmPassword -> "Passwords do not match"
            else -> null
        }
    }

    fun validateStaffName(name: String): String? {
        val trimmed = name.trim()
        return when {
            trimmed.isEmpty() -> "Full name is required"
            trimmed.length < 2 -> "Name must be at least 2 characters"
            else -> null
        }
    }

    fun validateCustomerName(name: String): String? {
        val trimmed = name.trim()
        return when {
            trimmed.isEmpty() -> "Customer full name is required"
            trimmed.length < 2 -> "Name must be at least 2 characters"
            !NAME_REGEX.matches(trimmed) -> "Enter a valid name using letters and spaces"
            else -> null
        }
    }

    fun validateIndianPhone(phone: String): String? {
        val trimmed = phone.trim()
        return when {
            trimmed.isEmpty() -> "Phone number is required"
            !INDIAN_PHONE_REGEX.matches(trimmed) ->
                "Enter a valid 10-digit Indian mobile number starting with 6–9"
            else -> null
        }
    }

    fun validateDob(dob: String): String? {
        val trimmed = dob.trim()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US).apply {
            isLenient = false
        }
        val parsePosition = ParsePosition(0)
        val date = dateFormat.parse(trimmed, parsePosition)
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        return when {
            trimmed.isEmpty() -> "Date of birth is required"
            !DOB_REGEX.matches(trimmed) ||
                date == null ||
                parsePosition.index != trimmed.length ||
                date.after(today) -> "Select a valid date (DD/MM/YYYY)"
            else -> null
        }
    }

    fun validateAddress(address: String): String? {
        val trimmed = address.trim()
        return when {
            trimmed.isEmpty() -> "Address is required"
            trimmed.length < 5 -> "Please enter a complete address (min 5 characters)"
            trimmed.length > 400 -> "Address cannot exceed 400 characters"
            else -> null
        }
    }
}
