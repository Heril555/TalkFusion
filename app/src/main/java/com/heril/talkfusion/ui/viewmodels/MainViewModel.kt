package com.heril.talkfusion.ui.viewmodels

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.heril.talkfusion.ui.Screen

class MainViewModel: ViewModel() {
    private val _currentScreen : MutableState<Screen> = mutableStateOf(Screen.BottomBarScreen.Chats)

    val currentScreen: MutableState<Screen>
        get() = _currentScreen

    fun setCurrentScreen(screen: Screen) {
        _currentScreen.value = screen
    }
}