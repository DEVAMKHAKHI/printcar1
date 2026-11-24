package com.printcare.app.utils

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ValidationUtilsTest {

    @Test
    fun validateDobRejectsImpossibleDates() {
        assertNotNull(ValidationUtils.validateDob("31/02/2000"))
        assertNotNull(ValidationUtils.validateDob("29/02/2001"))
    }

    @Test
    fun validateDobAcceptsPastLeapDay() {
        assertNull(ValidationUtils.validateDob("29/02/2000"))
    }

    @Test
    fun validateDobRejectsFutureDates() {
        val futureDate = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
        }.time
        val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.US).format(futureDate)

        assertNotNull(ValidationUtils.validateDob(formattedDate))
    }
}
