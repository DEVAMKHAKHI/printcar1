package com.printcare.app.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import java.util.Locale

object ValidationUtils {
    private val emailPattern = Regex(
        "^[A-Za-z0-9.!#\$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?(?:\\.[A-Za-z]{2,})+$"
    )
    private val phonePattern = Regex("^[6-9][0-9]{9}$")
    private val namePattern = Regex("^[\\p{L}][\\p{L} .'-]{1,99}$")
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/uuuu", Locale.US)
        .withResolverStyle(ResolverStyle.STRICT)

    fun validateEmail(value: String): String? = when {
        value.trim().isEmpty() -> "Email address is required."
        !emailPattern.matches(value.trim()) -> "Enter a valid email address."
        else -> null
    }

    fun validatePassword(value: String): String? = when {
        value.isEmpty() -> "Password is required."
        value.length < 6 -> "Password must be at least 6 characters."
        else -> null
    }

    fun validateName(value: String): String? = when {
        value.trim().isEmpty() -> "Full name is required."
        value.trim().length < 2 || !namePattern.matches(value.trim()) ->
            "Enter a valid name using letters and spaces."
        else -> null
    }

    fun validatePhone(value: String): String? = when {
        value.trim().isEmpty() -> "Phone number is required."
        !phonePattern.matches(value.trim()) ->
            "Enter a 10-digit Indian mobile number starting with 6–9."
        else -> null
    }

    fun validateDateOfBirth(value: String): String? {
        if (value.trim().isEmpty()) return "Date of birth is required."
        val date = try {
            LocalDate.parse(value.trim(), dateFormatter)
        } catch (_: DateTimeParseException) {
            return "Enter a real date in DD/MM/YYYY format."
        }
        return if (date.isAfter(LocalDate.now())) "Date of birth cannot be in the future." else null
    }

    fun validateAddress(value: String): String? = when {
        value.trim().length < 5 -> "Enter a complete address (at least 5 characters)."
        value.trim().length > 400 -> "Address cannot exceed 400 characters."
        else -> null
    }
}
