package com.heril.talkfusion

import android.app.Application
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.firebase.FirebaseApp
import com.heril.talkfusion.ui.viewmodels.FirebaseViewModel
import com.heril.talkfusion.utils.FirebaseViewModelProvider

class AppLifecycle : Application(), LifecycleObserver {

//    private lateinit var firebaseViewModel: FirebaseViewModel
    private val firebaseViewModel: FirebaseViewModel by lazy {
        FirebaseViewModelProvider.instance
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            Log.e("AppLifecycle", "Firebase initialization error", e)
        }

        // Initialize ViewModel
//        firebaseViewModel = FirebaseViewModel()

        // Register lifecycle observer
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        // App moved to the foreground
        firebaseViewModel.updateOnlineStatus(true)
        Log.d("AppLifecycle", "App moved to the foreground")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        // App moved to the background
        firebaseViewModel.updateOnlineStatus(false)
        Log.d("AppLifecycle", "App moved to the background")
    }
}
