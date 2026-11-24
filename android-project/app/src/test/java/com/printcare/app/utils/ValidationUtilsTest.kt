package com.printcare.app.utils

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class ValidationUtilsTest {
    @Test
    fun validatesEmailAndPhone() {
        assertNull(ValidationUtils.validateEmail("demo@example.invalid"))
        assertNotNull(ValidationUtils.validateEmail("not-an-email"))
        assertNull(ValidationUtils.validatePhone("9876543210"))
        assertNotNull(ValidationUtils.validatePhone("123"))
    }

    @Test
    fun validatesRealPastBirthDates() {
        assertNull(ValidationUtils.validateDateOfBirth("29/02/2000"))
        assertNotNull(ValidationUtils.validateDateOfBirth("31/02/2000"))
        assertNotNull(ValidationUtils.validateDateOfBirth("29/02/2001"))
    }

    @Test
    fun rejectsFutureBirthDates() {
        val tomorrow = LocalDate.now().plusDays(1)
            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.US))
        assertNotNull(ValidationUtils.validateDateOfBirth(tomorrow))
    }

    @Test
    fun validatesCustomerFields() {
        assertNull(ValidationUtils.validateName("Aarav Mehta"))
        assertNotNull(ValidationUtils.validateName("1"))
        assertNull(ValidationUtils.validateAddress("12 Sample Market"))
        assertNotNull(ValidationUtils.validateAddress("No"))
    }
}
