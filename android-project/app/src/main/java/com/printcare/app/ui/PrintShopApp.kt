package com.printcare.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.printcare.app.navigation.Routes
import com.printcare.app.ui.screens.AuthScreen
import com.printcare.app.ui.screens.CustomerDetailScreen
import com.printcare.app.ui.screens.CustomerFormScreen
import com.printcare.app.ui.screens.CustomerListScreen
import com.printcare.app.ui.screens.DashboardScreen
import com.printcare.app.viewmodel.MainViewModel

@Composable
fun PrintShopApp(viewModel: MainViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.feedback) {
        state.feedback?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearFeedback()
        }
    }

    LaunchedEffect(state.authReady, state.userEmail) {
        if (!state.authReady) return@LaunchedEffect
        val isAuthRoute = navController.currentDestination?.route in setOf(Routes.Login, Routes.SignUp)
        val destination = if (state.userEmail == null) Routes.Login else Routes.Dashboard
        if ((state.userEmail == null && !isAuthRoute) || (state.userEmail != null && isAuthRoute)) {
            navController.navigate(destination) {
                val previousRoute = if (state.userEmail == null) Routes.Dashboard else Routes.Login
                popUpTo(previousRoute) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { contentPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Login,
            modifier = Modifier.padding(contentPadding)
        ) {
            composable(Routes.Login) {
                AuthScreen(
                    isSignUp = false,
                    firebaseConfigured = state.firebaseConfigured,
                    working = state.working,
                    onSignIn = viewModel::signIn,
                    onCreateAccount = viewModel::createAccount,
                    onForgotPassword = viewModel::sendPasswordReset,
                    onNavigateToSignUp = { navController.navigate(Routes.SignUp) },
                    onNavigateToLogin = {}
                )
            }

            composable(Routes.SignUp) {
                AuthScreen(
                    isSignUp = true,
                    firebaseConfigured = state.firebaseConfigured,
                    working = state.working,
                    onSignIn = viewModel::signIn,
                    onCreateAccount = viewModel::createAccount,
                    onForgotPassword = viewModel::sendPasswordReset,
                    onNavigateToSignUp = {},
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }

            composable(Routes.Dashboard) {
                DashboardScreen(
                    email = state.userEmail,
                    customerCount = state.customers.size,
                    working = state.working,
                    onAddCustomer = { navController.navigate(Routes.AddCustomer) },
                    onViewCustomers = { navController.navigate(Routes.CustomerList) },
                    onSeedDemoData = { viewModel.seedDemoCustomers() },
                    onSignOut = viewModel::signOut
                )
            }

            composable(Routes.CustomerList) {
                CustomerListScreen(
                    customers = state.customers,
                    loading = state.customersLoading,
                    error = state.customersError,
                    onAddCustomer = { navController.navigate(Routes.AddCustomer) },
                    onOpenCustomer = { id -> navController.navigate(Routes.customerDetail(id)) },
                    onRetry = viewModel::retryCustomers
                )
            }

            composable(Routes.AddCustomer) {
                CustomerFormScreen(
                    customer = null,
                    customerId = null,
                    working = state.working,
                    onSave = { name, phone, dob, email, address ->
                        viewModel.saveCustomer(null, name, phone, dob, email, address) { saved ->
                            if (saved) navController.popBackStack()
                        }
                    },
                    onCancel = { navController.popBackStack() }
                )
            }

            composable(
                route = Routes.CustomerDetail,
                arguments = listOf(navArgument("customerId") { type = NavType.StringType })
            ) { entry ->
                val customerId = entry.arguments?.getString("customerId").orEmpty()
                CustomerDetailScreen(
                    customer = viewModel.customerById(customerId),
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(Routes.editCustomer(customerId)) },
                    onDelete = {
                        viewModel.deleteCustomer(customerId) { deleted ->
                            if (deleted) navController.popBackStack()
                        }
                    }
                )
            }

            composable(
                route = Routes.EditCustomer,
                arguments = listOf(navArgument("customerId") { type = NavType.StringType })
            ) { entry ->
                val customerId = entry.arguments?.getString("customerId").orEmpty()
                CustomerFormScreen(
                    customer = viewModel.customerById(customerId),
                    customerId = customerId,
                    working = state.working,
                    onSave = { name, phone, dob, email, address ->
                        viewModel.saveCustomer(customerId, name, phone, dob, email, address) { saved ->
                            if (saved) navController.popBackStack()
                        }
                    },
                    onCancel = { navController.popBackStack() }
                )
            }
        }
    }
}
