package com.printcare.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.printcare.app.PrintShopApplication
import com.printcare.app.repository.FirebaseRepository
import com.printcare.app.viewmodel.MainViewModel
import com.printcare.app.viewmodel.MainViewModelFactory

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels {
        val provider = (application as PrintShopApplication).firebaseProvider
        MainViewModelFactory(FirebaseRepository(provider))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PrintShopTheme {
                PrintShopApp(viewModel)
            }
        }
    }
}
