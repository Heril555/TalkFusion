package com.heril.talkfusion.ui.components

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.heril.talkfusion.R
import com.heril.talkfusion.ui.viewmodels.AuthState
import com.heril.talkfusion.ui.viewmodels.AuthViewModel
import com.heril.talkfusion.ui.theme.AccentColor
import com.heril.talkfusion.ui.theme.BgColor
import com.heril.talkfusion.ui.theme.GrayColor
import com.heril.talkfusion.ui.theme.Secondary
import com.heril.talkfusion.ui.theme.TextColor
import com.heril.talkfusion.ui.viewmodels.FirebaseViewModel
import com.heril.talkfusion.ui.viewmodels.SignInViewModel
import com.heril.talkfusion.ui.viewmodels.TaskViewModel
import kotlinx.coroutines.launch

@Composable
fun NormalTextComponent(value: String) {
    Text(
        text = value,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 40.dp),
        style = TextStyle(
            fontSize = 24.sp,
            fontWeight = FontWeight.Normal,
            fontStyle = FontStyle.Normal
        ),
        color = TextColor,
        textAlign = TextAlign.Center
    )
}

@Composable
fun HeadingTextComponent(value: String) {
    Text(
        text = value,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(),
        style = TextStyle(
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Normal
        ),
        color = TextColor,
        textAlign = TextAlign.Center
    )
}

@Composable
fun MyTextFieldComponent(
    labelValue: String,
    icon: ImageVector,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        label = { Text(text = labelValue) },
        value = value,
        onValueChange = onValueChange,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AccentColor,
            focusedLabelColor = AccentColor,
            cursorColor = TextColor,
            focusedContainerColor = BgColor,
            unfocusedContainerColor = BgColor,
            focusedLeadingIconColor = AccentColor,
            focusedTextColor = TextColor,
            unfocusedTextColor = TextColor
        ),
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = "profile"
            )
        },
        keyboardOptions = KeyboardOptions.Default
    )
}

@Composable
fun PasswordTextFieldComponent(
    labelValue: String,
    icon: ImageVector,
    value: String,
    onValueChange: (String) -> Unit
) {
    var isPasswordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        label = { Text(text = labelValue) },
        value = value,
        onValueChange = onValueChange,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AccentColor,
            focusedLabelColor = AccentColor,
            cursorColor = TextColor,
            focusedContainerColor = BgColor,
            unfocusedContainerColor = BgColor,
            focusedLeadingIconColor = AccentColor,
            focusedTextColor = TextColor,
            unfocusedTextColor = TextColor
        ),
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = "profile"
            )
        },
        trailingIcon = {
            val iconImage = if (isPasswordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff
            val description = if (isPasswordVisible) "Show Password" else "Hide Password"
            IconButton(onClick = {
                isPasswordVisible = !isPasswordVisible
            }) {
                Icon(imageVector = iconImage, contentDescription = description)
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation()
    )
}

@Composable
fun CheckboxComponent() {
    var isChecked by remember {
        mutableStateOf(false)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(56.dp)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = {
                isChecked = it
            }
        )
        ClickableTextComponent()
    }
}

@Composable
fun ClickableTextComponent() {
    val initialText = "By continuing you accept our "
    val privacyPolicyText = "Privacy Policy"
    val andText = " and "
    val termOfUseText = "Term of Use"

    val annotatedString = buildAnnotatedString {
        withStyle(style = SpanStyle(color = TextColor)) {
            append(initialText)
        }
        withStyle(style = SpanStyle(color = Secondary)) {
            pushStringAnnotation(tag = privacyPolicyText, annotation = privacyPolicyText)
            append(privacyPolicyText)
        }
        withStyle(style = SpanStyle(color = TextColor)) {
            append(andText)
        }
        withStyle(style = SpanStyle(color = Secondary)) {
            pushStringAnnotation(tag = termOfUseText, annotation = termOfUseText)
            append(termOfUseText)
        }
        append(".")
    }

    ClickableText(text = annotatedString, onClick = {
        annotatedString.getStringAnnotations(it, it)
            .firstOrNull()?.also { annotation ->
                Log.d("ClickableTextComponent", "You have Clicked ${annotation.tag}")
            }
    })
}

@Composable
fun AuthStateComponent(authState: AuthState) {
    when (authState) {
        is AuthState.Authenticated -> {
            Text(
                text = "Authenticated",
                modifier = Modifier.fillMaxSize(),
                color = Color.Green,
                fontSize = 24.sp,
                textAlign = TextAlign.Center
            )
        }
        is AuthState.Unauthenticated -> {
            Text(
                text = "Unauthenticated",
                modifier = Modifier.fillMaxSize(),
                color = Color.Red,
                fontSize = 24.sp,
                textAlign = TextAlign.Center
            )
        }
        is AuthState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(50.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        is AuthState.Success -> {
            Text(
                text = "Success",
                modifier = Modifier.fillMaxSize(),
                color = Color.Green,
                fontSize = 24.sp,
                textAlign = TextAlign.Center
            )
        }
        is AuthState.Error -> {
            Text(
                text = authState.message,
                modifier = Modifier.fillMaxSize(),
                color = Color.Red,
                fontSize = 24.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun BottomComponent(
    textQuery: String,
    textClickable: String,
    action: String,
    navController: NavHostController,
    authViewModel: AuthViewModel,
    auth: FirebaseAuth,
    email: String,
    password: String
) {
//    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current
    val applicationContext = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()
    val credentialManager = CredentialManager.create(context)
    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(context.getString(R.string.web_client_id))
        .build()
    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    if(action == "Login"){
                        authViewModel.login(email, password)
                    } else {
                        authViewModel.signup(email, password)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(Color.Transparent),
                enabled = authState.value !is AuthState.Loading
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.horizontalGradient(listOf(Secondary, AccentColor)),
                            shape = RoundedCornerShape(50.dp)
                        )
                        .fillMaxWidth()
                        .heightIn(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = action, color = Color.White, fontSize = 20.sp)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    thickness = 1.dp,
                    color = GrayColor
                )
                Text(
                    text = "Or",
                    modifier = Modifier.padding(10.dp),
                    fontSize = 20.sp
                )
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    thickness = 1.dp,
                    color = GrayColor
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        //Implement Google Sign In and Sign up here (with credentials manager)
                            scope.launch {
                                try{
                                    val result = credentialManager.getCredential(
                                        context = context,
                                        request = request
                                    )
                                    val credential : Credential = result.credential
                                    val googleIdTokenCredential = GoogleIdTokenCredential
                                        .createFrom(credential.data)
                                    val googleIdToken = googleIdTokenCredential.idToken
                                    val firebaseCredential =
                                        GoogleAuthProvider.getCredential(googleIdToken, null)
                                    auth.signInWithCredential(firebaseCredential)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                authViewModel.successfulSignWithGoogle()
                                                // Get the current user
//                                                val user = auth.currentUser
//                                                val userId = user?.uid
//                                                val username = user?.displayName
//                                                val profilePictureUrl = user?.photoUrl?.toString()
//                                                val mail = user?.email
//                                                Log.d("UI/UX", "User Data: $userId")
//                                                // Add the user to Firestore
////                                                firebaseViewModel.addUserToFirestore(authViewModel.getSignedInUser())
//
//                                                // Store the email in SharedPreferences
//                                                val sharedPreferences = applicationContext.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
//                                                sharedPreferences.edit().putString("user_email", mail).apply()
//                                                sharedPreferences.edit().putString("user_uid",userId ).apply()
//                                                sharedPreferences.edit().putString("user_name",username ).apply()
//                                                sharedPreferences.edit().putString("user_profile_picture",profilePictureUrl ).apply()
//
//                                                if(authViewModel.getSignedInUser() != null) {
//                                                    taskViewModel.isSignedIn = true
//                                                    firebaseViewModel.userData = authViewModel.getSignedInUser()
//                                                    Log.d("UI/UX", "User Data: ${firebaseViewModel.userData}")
//                                                    Log.d("UI/UX", "User Data: ${authViewModel.getSignedInUser()}")
//                                                    firebaseViewModel.updateOnlineStatus(true)
//                                                    firebaseViewModel.addUserToFirestore(firebaseViewModel.userData!!)
//                                                    firebaseViewModel.getToken()
//                                                    firebaseViewModel.setupLatestMessageListener()
//                                                    taskViewModel.showNavBar = true
//                                                    navController.popBackStack()
//                                                    navController.navigate("Main")
//                                                }else {
//                                                    // Navigate to the main screen
//                                                    navController.popBackStack()
//                                                    navController.navigate("Main")
//                                                }
                                            }else{
                                                // Handle unsuccessful sign-in
                                                Toast.makeText(context, "Sign-In Failed", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                }catch (e: Exception){
                                    Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                                    e.printStackTrace()
                                }
                            }
                         },
                    colors = ButtonDefaults.buttonColors(Color.Transparent),
                    modifier = Modifier
                        .padding(4.dp)
                        .border(
                            width = 2.dp,
                            color = Color(android.graphics.Color.parseColor("#d2d2d2")),
                            shape = RoundedCornerShape(20.dp)
                        )
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.google_svg),
                        contentDescription = "Google Logo",
                        modifier = Modifier
                            .size(30.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(15.dp))
            AccountQueryComponent(textQuery, textClickable, navController)
        }
    }
}

@Composable
fun AccountQueryComponent(
    textQuery: String,
    textClickable: String,
    navController: NavHostController
) {
    val annonatedString = buildAnnotatedString {
        withStyle(style = SpanStyle(color = TextColor, fontSize = 15.sp)) {
            append(textQuery)
        }
        withStyle(style = SpanStyle(color = Secondary, fontSize = 15.sp)) {
            pushStringAnnotation(tag = textClickable, annotation = textClickable)
            append(textClickable)
        }
    }

    ClickableText(text = annonatedString, onClick = {
        annonatedString.getStringAnnotations(it, it)
            .firstOrNull()?.also { annonation ->
                if (annonation.item == "Login") {
                    navController.navigate("Login")
                } else if (annonation.item == "Register") {
                    navController.navigate("Signup")
                }
            }
    })
}