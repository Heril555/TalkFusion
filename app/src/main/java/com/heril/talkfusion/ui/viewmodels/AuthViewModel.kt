package com.heril.talkfusion.ui.viewmodels

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.heril.talkfusion.data.UserData

class AuthViewModel(context: Context, private val auth: FirebaseAuth) : ViewModel() {
//    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

//    var sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
//    val email = sharedPreferences.getString("user_email", null)
//    val uid = sharedPreferences.getString("user_uid", null)
//    val displayName = sharedPreferences.getString("user_name", null)
//    val profilePictureUrl = sharedPreferences.getString("user_profile_picture", null)

    var email by mutableStateOf<String?>("")
    var uid by mutableStateOf<String?>("")
    var displayName by mutableStateOf<String?>("")
    var profilePictureUrl by mutableStateOf<String?>("")

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    init {
        checkAuthStatus()
    }

    fun checkAuthStatus() {
        if (auth.currentUser != null) {
            _authState.value = AuthState.Authenticated
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun successfulSignWithGoogle(){
        _authState.value = AuthState.Success
    }

    fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email and password cannot be empty")
            return
        }
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Success
                    Log.d("AuthViewModel","AuthState.Success")
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Unknown Error")
                }
            }
    }

    fun signup(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email and password cannot be empty")
            return
        }
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Success
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Unknown Error")
                }
            }
    }

    fun getSignedInUser(): UserData {
        Log.d("AuthViewModel","UID: $uid")
        return UserData(
            userId = uid,
            username = displayName,
            profilePictureUrl = profilePictureUrl,
            mail = email
        )
    }

    fun signOut() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }
}

sealed class AuthState {
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}