package com.heril.talkfusion.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heril.talkfusion.data.UserData
import com.heril.talkfusion.ui.viewmodels.FirebaseViewModel
import kotlinx.coroutines.delay

@Composable
fun addFriend(viewModel: FirebaseViewModel) {
    val colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
    MaterialTheme(colorScheme = colorScheme) {
        AddFriendContent(viewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFriendContent(viewModel: FirebaseViewModel) {
    var email by remember { mutableStateOf("") }
    var foundUsers by remember { mutableStateOf<List<UserData>?>(null) }
    var isSearching by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }

    val currentUserId = viewModel.userData?.userId.orEmpty()
    // Collect chatListUsers from the StateFlow
    val chatListUsers by viewModel.chatListUsers.collectAsState(emptyList())

    // Extract the user IDs from the chat list
    val chatListUserIds = chatListUsers.map { it.userId }

    LaunchedEffect(email) {
        if (email.isNotEmpty()) {
            isSearching = true
            delay(500) // Debounce
            viewModel.searchUserByEmail(email) { users ->
                // Filter out the current user and users already in the chatList
                if (users != null) {
                    foundUsers = users.filter { user ->
                        user.userId != currentUserId && !chatListUserIds.contains(user.userId)
                    }
                }
                isSearching = false
            }
        } else {
            foundUsers = null
            isSearching = false
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Add a Friend",
                fontSize = 35.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Search by email") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(
                visible = isSearching,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(36.dp),
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            AnimatedVisibility(
                visible = !isSearching && foundUsers != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                foundUsers?.let { users ->
                    if (users.isEmpty()) {
                        Text(
                            "No users found",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(8.dp)
                        )
                    } else {
                        LazyColumn {
                            items(users) { user ->
                                UserCard(user, viewModel) {
                                    showSuccessMessage = true
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = showSuccessMessage,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    "Friend request sent successfully!",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

@Composable
fun UserCard(user: UserData, viewModel: FirebaseViewModel, onRequestSent: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    user.username ?: "",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    user.mail ?: "",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            Button(
                onClick = {
                    viewModel.sendFriendRequest(user.userId ?: "")
                    onRequestSent()
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text("Add")
            }
        }
    }
}
