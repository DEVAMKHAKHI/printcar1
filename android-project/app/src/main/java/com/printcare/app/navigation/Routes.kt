package com.printcare.app.navigation

object Routes {
    const val Login = "login"
    const val SignUp = "sign_up"
    const val Dashboard = "dashboard"
    const val CustomerList = "customers"
    const val AddCustomer = "customer_add"
    const val CustomerDetail = "customer/{customerId}"
    const val EditCustomer = "customer_edit/{customerId}"

    fun customerDetail(customerId: String) = "customer/$customerId"
    fun editCustomer(customerId: String) = "customer_edit/$customerId"
}
