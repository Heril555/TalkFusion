package com.heril.talkfusion.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.heril.talkfusion.ui.viewmodels.AuthViewModel
import com.heril.talkfusion.ui.viewmodels.FirebaseViewModel
import com.heril.talkfusion.ui.viewmodels.SignInViewModel
import com.heril.talkfusion.ui.viewmodels.TaskViewModel

@Composable
fun Navigation(firebaseViewModel: FirebaseViewModel){
    val navController = rememberNavController()
    val applicationContext = LocalContext.current.applicationContext
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val authViewModel = AuthViewModel(applicationContext,auth)
    NavHost(navController = navController, startDestination = "Login") {
        composable("Login") {
            LoginScreen(navController,authViewModel,auth, firebaseViewModel,applicationContext)
        }
        composable("Signup") {
            SignupScreen(navController, applicationContext)
        }
        composable("Main") {
            MainScreen(navController,firebaseViewModel,authViewModel)
        }
    }
}