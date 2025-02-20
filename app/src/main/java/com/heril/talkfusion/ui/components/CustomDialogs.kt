package com.heril.talkfusion.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.heril.talkfusion.R
import com.heril.talkfusion.ui.viewmodels.FirebaseViewModel
import com.heril.talkfusion.ui.viewmodels.TaskViewModel

@Composable
fun CustomDialog(
    taskViewModel: TaskViewModel,
    firebaseViewModel: FirebaseViewModel
){
    val ctx = LocalContext.current
    Dialog(
        onDismissRequest = {
            taskViewModel.showDialog = false
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ){
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .aspectRatio(1.4f),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(vertical = 25.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = "Add a New User",
                    fontSize = 30.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.size(10.dp))
                OutlinedTextField(
                    value = firebaseViewModel.newUser,
                    onValueChange = {firebaseViewModel.newUser = it},
                    label = {
                        Text(
                            text = "Enter the Mail ID",
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    singleLine = true,
                    maxLines = 1,
                    shape = RoundedCornerShape(20.dp),
                    placeholder = {
                        Text(
                            text = "Mail ID",
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                )
                Spacer(modifier = Modifier.size(15.dp))
                Button(
                    onClick = {
                        firebaseViewModel.addUserToChatList(firebaseViewModel.newUser,ctx)
                        firebaseViewModel.newUser = ""
                        taskViewModel.showDialog = false
                    }
                ){
                    Text(
                        text = "Add",
                        fontSize = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
fun DeleteMessageDialog(
    taskViewModel: TaskViewModel,
    firebaseViewModel: FirebaseViewModel,
    navController: NavController
){
    val ctx = LocalContext.current
    Dialog(
        onDismissRequest = {
            taskViewModel.showDeleteMsgDialog = false
            firebaseViewModel.selectedMessage = null
            taskViewModel.chatOptions = false
        }
    ){
        AlertDialog(
            onDismissRequest = {taskViewModel.showDeleteMsgDialog = false},
            title = { Text(text = "Delete Message?") },
            text = { Text(text = "This Operation is Irreversible") },
            dismissButton = {
                Button(
                    onClick = {
                        taskViewModel.showDeleteMsgDialog = false
                        firebaseViewModel.selectedMessage = null
                        taskViewModel.chatOptions = false
                    },
                ) {
                    Text(text = "Cancel")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        firebaseViewModel.deleteMessage(
                            otherUserId = firebaseViewModel.chattingWith?.userId.toString(),
                            messageData = firebaseViewModel.selectedMessage!!
                        )
                        taskViewModel.showDeleteMsgDialog = false
                        taskViewModel.chatOptions = false
                        firebaseViewModel.selectedMessage = null
                    },
                ) {
                    Text(text = "Delete")
                }
            }
        )
    }
}

@Composable
fun ImageDialog(
    taskViewModel : TaskViewModel,
    firebaseViewModel: FirebaseViewModel,
    navController: NavController
){
    val ctx = LocalContext.current
    Dialog(
        onDismissRequest = {
            taskViewModel.showImageDialog = false
            firebaseViewModel.sentBy = ""
            firebaseViewModel.mediaViewText = ""
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ){
        ElevatedCard(
            colors = CardDefaults.cardColors(
                containerColor = Color.Black
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        taskViewModel.showImageDialog = false
                        firebaseViewModel.sentBy = ""
                        firebaseViewModel.mediaViewText = ""
                    }
                ){
                    Icon(
                        painter = painterResource(id = R.drawable.backicon),
                        contentDescription = null,
                        modifier = Modifier.size(25.dp),
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.size(10.dp))
                Row (
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    AsyncImage(
                        model = firebaseViewModel.imageDialogProfilePicture,
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(shape = CircleShape)
                    )
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(
                        text = if(firebaseViewModel.chattingWith?.userId == firebaseViewModel.userData?.userId || firebaseViewModel.sentBy == firebaseViewModel.userData?.userId){
                            "You"
                        }else {
                            firebaseViewModel.chattingWith?.username.toString()
                        },
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(1f),
                        maxLines =
                        if(taskViewModel.expandedPersonInfo){
                            2
                        }else{
                            1
                        },
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.size(10.dp))
            Column (
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = if (firebaseViewModel.mediaViewText.isNotBlank()) 10.dp else 30.dp),
                verticalArrangement = Arrangement.SpaceAround,
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                var scale by remember {
                    mutableStateOf(1f)
                }
                var offset by remember {
                    mutableStateOf(Offset.Zero)
                }
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    val state =
                        rememberTransformableState { zoomChange, panChange, rotationChange ->
                            scale = (scale * zoomChange).coerceIn(1f, 5f)
                            val extraWidth = (scale - 1) * constraints.maxWidth
                            val extraHeight = (scale - 1) * constraints.maxHeight
                            val maxX = extraWidth / 2
                            val maxY = extraHeight / 2
                            offset = Offset(
                                x = (offset.x + scale * panChange.x).coerceIn(-maxX, maxX),
                                y = (offset.y + scale * panChange.y).coerceIn(-maxY, maxY),
                            )
                        }
                    SubcomposeAsyncImage(
                        model = firebaseViewModel.imageString,
                        contentDescription = null,
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                translationX = offset.x
                                translationY = offset.y
                            }
                            .transformable(state),
                        loading = {
                            Row (
                                modifier =  Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ){
                                CircularProgressIndicator()
                            }
                        }
                    )
                }
                AnimatedVisibility(visible = firebaseViewModel.mediaViewText.isNotBlank() && scale == 1f){
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Text(
                            text = firebaseViewModel.mediaViewText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(15.dp)
                                .weight(1f),
                            maxLines = 2,
                            textAlign = TextAlign.Center,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SetProfilePictureDialog(
    firebaseViewModel: FirebaseViewModel,
    taskViewModel: TaskViewModel
) {
    val ctx = LocalContext.current
    Dialog(
        onDismissRequest = {
            taskViewModel.showSetProfilePictureDialog = false
            firebaseViewModel.mediaUri = null
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        ElevatedCard(
            colors = CardDefaults.cardColors(
                containerColor = Color.Black
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        bottom = 30.dp,
                        top = 30.dp
                    ),
                verticalArrangement = Arrangement.SpaceAround
            ) {
                Text(
                    text = "Update Profile Picture?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp
                )
                Spacer(modifier = Modifier.size(10.dp))
                AsyncImage(
                    model = firebaseViewModel.mediaUri,
                    contentDescription = null,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.size(10.dp))
                Row {
                    ElevatedButton(onClick = {
                        taskViewModel.showSetProfilePictureDialog = false
                        firebaseViewModel.mediaUri = null
                    }) {
                        Text(
                            text = "Cancel",
                            fontSize = 20.sp
                        )
                    }
                    Spacer(modifier = Modifier.size(15.dp))
                    ElevatedButton(onClick = {
                        firebaseViewModel.updateProfilePic()
                        Toast.makeText(
                            ctx,
                            "Profile Picture will be Updated",
                            Toast.LENGTH_SHORT
                        ).show()
                        taskViewModel.showSetProfilePictureDialog = false
                        firebaseViewModel.mediaUri = null
                    }) {
                        Text(
                            text = "Set",
                            fontSize = 20.sp
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EmojiDialog(
    taskViewModel: TaskViewModel,
    firebaseViewModel: FirebaseViewModel
){
    val ctx = LocalContext.current
    Dialog(onDismissRequest = {
        taskViewModel.allEmojis = false
        firebaseViewModel.selectedMessage = null
        taskViewModel.chatOptions = false
    }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(5.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if(firebaseViewModel.userData?.userPref?.recentEmojis?.isNotEmpty() == true) {
                    Column {
                        Text(
                            text = "Recently Used",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Row (
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ){
                            firebaseViewModel.userData?.userPref?.recentEmojis?.take(6)?.forEach {
                                Text(
                                    text = it,
                                    fontSize = 25.sp,
                                    modifier = Modifier
                                        .padding(10.dp)
                                        .clickable {
                                            if (firebaseViewModel.selectedMessage?.curUserReaction == it) {
                                                firebaseViewModel.editMessage(
                                                    firebaseViewModel.chattingWith?.userId.toString(),
                                                    firebaseViewModel.selectedMessage?.time!!,
                                                    firebaseViewModel.selectedMessage?.message.toString(),
                                                    ctx,
                                                    null
                                                )
                                            } else {
                                                firebaseViewModel.editMessage(
                                                    firebaseViewModel.chattingWith?.userId.toString(),
                                                    firebaseViewModel.selectedMessage?.time!!,
                                                    firebaseViewModel.selectedMessage?.message.toString(),
                                                    ctx,
                                                    it
                                                )
                                            }
                                            firebaseViewModel.selectedMessage = null
                                            taskViewModel.chatOptions = false
                                            taskViewModel.allEmojis = false
                                        }
                                )
                            }
                        }
                    }
                }
                Text(
                    text = "All Emojis",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Left
                )
                Spacer(modifier = Modifier.size(5.dp))
                LazyVerticalGrid(columns = GridCells.Fixed(6)) {
                    items(allEmojis){
                        Text(
                            text = it,
                            modifier = Modifier
                                .padding(10.dp)
                                .combinedClickable(
                                    onClick = {
                                        if (firebaseViewModel.selectedMessage?.curUserReaction == it) {
                                            firebaseViewModel.editMessage(
                                                firebaseViewModel.chattingWith?.userId.toString(),
                                                firebaseViewModel.selectedMessage?.time!!,
                                                firebaseViewModel.selectedMessage?.message.toString(),
                                                ctx,
                                                null
                                            )
                                        } else {
                                            firebaseViewModel.updateEmojiPref(it)
                                            firebaseViewModel.editMessage(
                                                firebaseViewModel.chattingWith?.userId.toString(),
                                                firebaseViewModel.selectedMessage?.time!!,
                                                firebaseViewModel.selectedMessage?.message.toString(),
                                                ctx,
                                                it
                                            )
                                        }
                                        firebaseViewModel.selectedMessage = null
                                        taskViewModel.chatOptions = false
                                        taskViewModel.allEmojis = false
                                    }
                                ),
                            fontSize = 25.sp
                        )
                    }
                }
            }
        }
    }
}