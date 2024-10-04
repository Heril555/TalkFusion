package com.heril.talkfusion.ui

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.heril.talkfusion.ui.components.AuthStateComponent
import com.heril.talkfusion.ui.components.BottomComponent
import com.heril.talkfusion.ui.components.HeadingTextComponent
import com.heril.talkfusion.ui.components.MyTextFieldComponent
import com.heril.talkfusion.ui.components.NormalTextComponent
import com.heril.talkfusion.ui.components.PasswordTextFieldComponent
import com.heril.talkfusion.ui.viewmodels.AuthState
import com.heril.talkfusion.ui.viewmodels.AuthViewModel
import com.heril.talkfusion.ui.viewmodels.FirebaseViewModel
import com.heril.talkfusion.ui.viewmodels.TaskViewModel

@Composable
fun LoginScreen(navController: NavHostController,authViewModel: AuthViewModel,auth: FirebaseAuth, firebaseViewModel: FirebaseViewModel,applicationContext: Context) {
//    val auth: FirebaseAuth = FirebaseAuth.getInstance()
//    val authViewModel = AuthViewModel(applicationContext,auth)
    val authState = authViewModel.authState.observeAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val taskViewModel = TaskViewModel()
//    val context = LocalContext.current

    Surface(
        color = Color.White,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(28.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Column {
                NormalTextComponent(value = "Hey, there")
                HeadingTextComponent(value = "Welcome Back")
            }
            Spacer(modifier = Modifier.height(25.dp))
            Column {
                MyTextFieldComponent(
                    labelValue = "Email",
                    icon = Icons.Outlined.Email,
                    value = email,
                    onValueChange = { email = it }
                )
                Spacer(modifier = Modifier.height(10.dp))
                PasswordTextFieldComponent(
                    labelValue = "Password",
                    icon = Icons.Outlined.Lock,
                    value = password,
                    onValueChange = { password = it }
                )
            }
            BottomComponent(
                textQuery = "Don't have an account? ",
                textClickable = "Register",
                action = "Login",
                navController,
                authViewModel,
                auth,
                email,
                password
            )
        }
    }
    authState.value?.let {
        AuthStateComponent(authState = it)
        if (it is AuthState.Success) {
            Log.d("Login UI: ","$it")
            // Get the current user
            val user = auth.currentUser
            val userId = user?.uid
            val username = user?.displayName
            val profilePictureUrl = user?.photoUrl?.toString()
            val mail = user?.email

            authViewModel.email = mail
            authViewModel.uid = userId
            authViewModel.displayName = username
            authViewModel.profilePictureUrl = profilePictureUrl

            // Store the email in SharedPreferences
//            val sharedPreferences = applicationContext.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
//            sharedPreferences.edit().putString("user_email", mail).apply()
//            sharedPreferences.edit().putString("user_uid", userId).apply()
//            sharedPreferences.edit().putString("user_name", username).apply()
//            sharedPreferences.edit().putString("user_profile_picture", profilePictureUrl).apply()
            Log.d("UI/UX", "User Data: $userId")

            if (authViewModel.getSignedInUser() != null) {
                taskViewModel.isSignedIn = true
                firebaseViewModel.userData = authViewModel.getSignedInUser()
                Log.d("UI/UX", "User Data: ${firebaseViewModel.userData}")
                Log.d("UI/UX", "User Data: ${authViewModel.getSignedInUser()}")
                firebaseViewModel.updateOnlineStatus(true)
                firebaseViewModel.addUserToFirestore(firebaseViewModel.userData!!)
                firebaseViewModel.getToken()
                firebaseViewModel.setupLatestMessageListener()
                taskViewModel.showNavBar = true
                navController.navigate("Main")
            }
        }
    }
}
