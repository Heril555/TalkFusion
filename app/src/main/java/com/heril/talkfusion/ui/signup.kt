package com.heril.talkfusion.ui

import android.content.Context
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
import com.heril.talkfusion.ui.components.CheckboxComponent
import com.heril.talkfusion.ui.components.HeadingTextComponent
import com.heril.talkfusion.ui.components.MyTextFieldComponent
import com.heril.talkfusion.ui.components.NormalTextComponent
import com.heril.talkfusion.ui.components.PasswordTextFieldComponent
import com.heril.talkfusion.ui.viewmodels.AuthState
import com.heril.talkfusion.ui.viewmodels.AuthViewModel

@Composable
fun SignupScreen(navController: NavHostController, applicationContext: Context) {
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val authViewModel = AuthViewModel(applicationContext,auth)
    val authState = authViewModel.authState.observeAsState()
//    var firstName by remember { mutableStateOf("") }
//    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

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
            NormalTextComponent(value = "Hello there,")
            HeadingTextComponent(value = "Create an Account")
            Spacer(modifier = Modifier.height(25.dp))

            Column {
//                MyTextFieldComponent(
//                    labelValue = "First Name",
//                    icon = Icons.Outlined.Person,
//                    value = firstName,
//                    onValueChange = { firstName = it }
//                )
//                Spacer(modifier = Modifier.height(10.dp))
//                MyTextFieldComponent(
//                    labelValue = "Last Name",
//                    icon = Icons.Outlined.Person,
//                    value = lastName,
//                    onValueChange = { lastName = it }
//                )
//                Spacer(modifier = Modifier.height(10.dp))
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
                CheckboxComponent()
                BottomComponent(
                    textQuery = "Already have an account? ",
                    textClickable = "Login",
                    action = "Register",
                    navController,
                    authViewModel,
                    auth,
                    email,
                    password
                )
            }
        }
    }
    authState.value?.let {
        AuthStateComponent(authState = it)
        if(it is AuthState.Success){
            navController.navigate("Login")
        }
    }
}
