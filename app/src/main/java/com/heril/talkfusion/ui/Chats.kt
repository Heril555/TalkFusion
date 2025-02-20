package com.heril.talkfusion.ui

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.heril.talkfusion.ui.components.PersonCard
import com.heril.talkfusion.ui.viewmodels.FirebaseViewModel
import com.heril.talkfusion.ui.viewmodels.TaskViewModel
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp
import com.heril.talkfusion.R
import com.heril.talkfusion.ui.components.CustomDialog
import com.heril.talkfusion.ui.components.ImageDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatsScreen(
    taskViewModel: TaskViewModel, firebaseViewModel: FirebaseViewModel, navController: NavController
) {
    firebaseViewModel.loadChatListUsers()
    val chatListUsers = firebaseViewModel.chatListUsers.collectAsState()
    val blockedUsers = firebaseViewModel.blockedUsers.collectAsState()
    val filteredUsers = firebaseViewModel.searchContacts.collectAsState()

    Log.d("Chat UI:", "ChatListUsers: ${chatListUsers.value}")

    BackHandler {}

    if (taskViewModel.showDialog) {
        CustomDialog(
            taskViewModel = taskViewModel, firebaseViewModel = firebaseViewModel
        )
    }

    if (taskViewModel.showImageDialog) {
        ImageDialog(
            taskViewModel = taskViewModel,
            firebaseViewModel = firebaseViewModel,
            navController = navController
        )
    }

    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current

    Scaffold(modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding(),
        topBar = {
            TopAppBar(title = {
                Text(
                    text = "Chats",
                    fontSize = 35.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("AddFriend")
                },
                modifier = Modifier.padding(end = 26.dp, bottom = 54.dp),
                containerColor = colorResource(id = R.color.blue),
                contentColor = colorResource(id = R.color.black)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add"
                )
            }
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                OutlinedTextField(value = firebaseViewModel.searchContact,
                    onValueChange = { newValue ->
                        firebaseViewModel.searchContact = newValue
                        firebaseViewModel.filterContacts(
                            firebaseViewModel.chatListUsers.value, firebaseViewModel.searchContact
                        )
                    },
                    label = { Text("Search") },
                    placeholder = { Text(text = "Search By Name or Email") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Rounded.Search, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Search,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(30.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.primary.copy(0.2f),
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.primary.copy(0.2f),
                    )
                )

                Spacer(modifier = Modifier.size(20.dp))

                Text(
                    text = "Message",
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.Start)
                )

                val items = remember(chatListUsers.value, filteredUsers.value) {
                    if (filteredUsers.value.isEmpty()) {
                        chatListUsers.value.filter { userData -> !blockedUsers.value.any { it.userId == userData.userId } }
//                            .sortedByDescending { it.latestMessage?.time }
                    } else {
                        filteredUsers.value
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 70.dp)
                ) {
                    items(items = items, key = { it.userId.toString() }) { userData ->
                        val dismissState =
                            rememberSwipeToDismissBoxState(initialValue = SwipeToDismissBoxValue.Settled,
                                positionalThreshold = { swipeActivationFloat -> swipeActivationFloat / 3 })
                        LaunchedEffect(userData) {
                            dismissState.reset()
                        }
                        SwipeToDismissBox(modifier = Modifier.animateItemPlacement(),
                            state = dismissState,
                            backgroundContent = {
                                val color by animateColorAsState(
                                    when (dismissState.targetValue) {
                                        SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                                        SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primaryContainer
                                        SwipeToDismissBoxValue.Settled -> Color.Transparent
                                    }, label = ""
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(color),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        AnimatedVisibility(visible = dismissState.targetValue == SwipeToDismissBoxValue.EndToStart,
                                            enter = slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth }),
                                            exit = slideOutHorizontally(targetOffsetX = { fullWidth -> fullWidth })) {
                                            Row(
                                                modifier = Modifier.fillMaxSize(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Column {
                                                    Text(text = "Delete or Block User ?")
                                                }
                                                IconButton(onClick = { scope.launch { dismissState.reset() } }) {
                                                    Icon(
                                                        Icons.Rounded.Refresh,
                                                        contentDescription = "Refresh"
                                                    )
                                                }
                                                IconButton(onClick = {
                                                    firebaseViewModel.deleteFriend(userData.userId.toString())
                                                    Toast.makeText(
                                                        ctx,
                                                        "Contact and Chats Deleted",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }) {
                                                    Icon(
                                                        Icons.Rounded.Delete,
                                                        contentDescription = "Delete"
                                                    )
                                                }
                                                IconButton(onClick = {
                                                    firebaseViewModel.blockUser(userData.userId.toString())
                                                    Toast.makeText(
                                                        ctx,
                                                        "Blocked User, you can manage blocked users in Settings",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }) {
                                                    Icon(
                                                        painter = painterResource(id = R.drawable.blockicon),
                                                        contentDescription = "Delete",
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }
                                            }
                                        }
                                        AnimatedVisibility(visible = dismissState.targetValue == SwipeToDismissBoxValue.StartToEnd) {
                                            Row(
                                                modifier = Modifier.fillMaxSize(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Text(text = "Add Contact to Favorites ?")
                                                IconButton(onClick = { scope.launch { dismissState.reset() } }) {
                                                    Icon(
                                                        Icons.Rounded.Refresh,
                                                        contentDescription = "Refresh"
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            content = {
                                PersonCard(
                                    userData = userData,
                                    firebaseViewModel = firebaseViewModel,
                                    navController = navController,
                                    taskViewModel = taskViewModel
                                )
                            })
                    }
                }
            }
        })
}