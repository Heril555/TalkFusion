package com.heril.talkfusion.ui

import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.heril.talkfusion.ui.viewmodels.AuthViewModel
import com.heril.talkfusion.ui.viewmodels.FirebaseViewModel
import com.heril.talkfusion.ui.viewmodels.TaskViewModel
import com.heril.talkfusion.ui.viewmodels.TranslateViewModel

@Composable
fun Navigation(navController: NavHostController,bottomNavController: NavHostController,pd: PaddingValues,firebaseViewModel: FirebaseViewModel,authViewModel: AuthViewModel){
    NavHost(
        navController = bottomNavController,
        startDestination = Screen.BottomBarScreen.Chats.route,
        modifier = Modifier.padding(pd)
    ) {
        val taskViewModel = TaskViewModel()
        composable(Screen.BottomBarScreen.Chats.route) {
            ChatsScreen(TaskViewModel(), firebaseViewModel,bottomNavController)
        }
        composable(Screen.BottomBarScreen.FriendRequests.route) {
            firebaseViewModel.startRequestListener()
            friendRequests(firebaseViewModel)
        }
        composable(Screen.BottomBarScreen.Account.route) {
            accountScreen(
                userData = authViewModel.getSignedInUser(),
                onSignOut = {
                        firebaseViewModel.updateOnlineStatus(false)
                        authViewModel.signOut()
                        taskViewModel.isSignedIn = false
                        firebaseViewModel.removeToken()
                        navController.navigate("Login")
                },
                navController = bottomNavController,
                firebaseViewModel = firebaseViewModel,
                taskViewModel = taskViewModel
            )
        }
        composable("PersonChat",
            enterTransition = {
                fadeIn() + expandVertically()
            },
            exitTransition = {
                fadeOut() + shrinkVertically()
            }) {
            personChatScreen(firebaseViewModel,taskViewModel,TranslateViewModel(),bottomNavController)
        }
        composable("ForwardScreen") {
            forwardScreen(firebaseViewModel, taskViewModel, bottomNavController)
        }
        composable("AddFriend"){
            addFriend(firebaseViewModel)
        }
        composable("BlockedScreen"){
            blockedScreen(firebaseViewModel)
        }
        composable("EditBioScreen"){
            editBioScreen(userData = authViewModel.getSignedInUser(),
                firebaseViewModel = firebaseViewModel,
                navController = bottomNavController)
        }
    }
}

@Composable
fun MainScreen(navController: NavHostController,firebaseViewModel: FirebaseViewModel,authViewModel: AuthViewModel) {
    val bottomNavController = rememberNavController()
    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = bottomNavController)
        },
        backgroundColor = MaterialTheme.colorScheme.background
    ) {
        Navigation(navController,bottomNavController,it,firebaseViewModel,authViewModel)
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    BottomNavigation(backgroundColor = MaterialTheme.colorScheme.background) {
        val currentRoute = currentRoute(navController)
        screensInBottomBar.forEach { screen ->
            BottomNavigationItem(
                icon = { Icon(painterResource(id = screen.icon), contentDescription = null,tint = MaterialTheme.colorScheme.primary) },
                label = { Text(screen.bTitle,color = MaterialTheme.colorScheme.primary) },
                selected = currentRoute == screen.bRoute,
                onClick = {
                    navController.navigate(screen.bRoute) {
                        navController.graph.startDestinationRoute?.let { route ->
                            popUpTo(route) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
fun currentRoute(navController: NavHostController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}
