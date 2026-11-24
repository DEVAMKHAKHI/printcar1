package com.printcare.app.model

data class DemoCustomer(
    val fullName: String,
    val phoneNumber: String,
    val dateOfBirth: String,
    val email: String,
    val address: String
) {
    fun toFirestoreMap(customerId: String): Map<String, Any> = mapOf(
        "customerId" to customerId,
        "fullName" to fullName,
        "phoneNumber" to phoneNumber,
        "dateOfBirth" to dateOfBirth,
        "email" to email,
        "address" to address,
        "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
    )
}

object DemoCustomers {
    val records = listOf(
        DemoCustomer("Aarav Mehta", "9000000001", "14/03/1992", "aarav.mehta@example.invalid", "12 Sample Market, Jaipur, Rajasthan"),
        DemoCustomer("Diya Sharma", "9000000002", "08/11/1988", "diya.sharma@example.invalid", "24 Fiction Road, Pune, Maharashtra"),
        DemoCustomer("Kabir Rao", "9000000003", "21/06/1995", "kabir.rao@example.invalid", "8 Demo Lane, Mysuru, Karnataka"),
        DemoCustomer("Anaya Patel", "9000000004", "02/01/1990", "anaya.patel@example.invalid", "41 Placeholder Street, Surat, Gujarat"),
        DemoCustomer("Ishaan Nair", "9000000005", "17/09/1985", "ishaan.nair@example.invalid", "6 Example Avenue, Kochi, Kerala"),
        DemoCustomer("Sara Khan", "9000000006", "29/02/1996", "sara.khan@example.invalid", "15 Sample Circle, Bhopal, Madhya Pradesh"),
        DemoCustomer("Arjun Das", "9000000007", "30/04/1993", "arjun.das@example.invalid", "19 Fiction Park, Kolkata, West Bengal"),
        DemoCustomer("Meera Iyer", "9000000008", "12/12/1989", "meera.iyer@example.invalid", "3 Demo Gardens, Chennai, Tamil Nadu"),
        DemoCustomer("Vivaan Joshi", "9000000009", "05/07/1991", "vivaan.joshi@example.invalid", "27 Placeholder Road, Indore, Madhya Pradesh"),
        DemoCustomer("Tara Singh", "9000000010", "23/10/1994", "tara.singh@example.invalid", "10 Example Square, Dehradun, Uttarakhand")
    )
}
