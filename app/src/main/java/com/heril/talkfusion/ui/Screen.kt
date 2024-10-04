package com.heril.talkfusion.ui

import androidx.annotation.DrawableRes
import com.heril.talkfusion.R

sealed class Screen(val title: String,val route:String) {

    sealed class BottomBarScreen(val bTitle: String,val bRoute: String,@DrawableRes val icon: Int)
        :Screen(bTitle,bRoute){
        object Chats : BottomBarScreen(
            "Chats",
            "Chats",
            R.drawable.ic_chats
        )
        object FriendRequests : BottomBarScreen(
            "Friend Requests",
            "FriendRequests",
            R.drawable.ic_friends
        )
        object Account : BottomBarScreen(
            "Account",
            "Account",
            R.drawable.accounticon
        )
    }
}

val screensInBottomBar = listOf(
    Screen.BottomBarScreen.Chats,
    Screen.BottomBarScreen.FriendRequests,
    Screen.BottomBarScreen.Account
)