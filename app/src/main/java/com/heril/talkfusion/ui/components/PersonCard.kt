package com.heril.talkfusion.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.heril.talkfusion.data.UserData
import com.heril.talkfusion.ui.viewmodels.FirebaseViewModel
import com.heril.talkfusion.ui.viewmodels.TaskViewModel

@Composable
fun PersonCard(
    userData: UserData,
    firebaseViewModel: FirebaseViewModel,
    navController: NavController,
    taskViewModel: TaskViewModel
){
    val ctx = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        onClick = {
            firebaseViewModel._chatMessages.value = emptyList()
            firebaseViewModel.chattingWith = userData
            firebaseViewModel.startMessageListener()
            Log.d("PersonCard","Chatting with : $userData")
            navController.navigate("PersonChat")
            if(taskViewModel.isForwarding){
                firebaseViewModel.SendMessage(
                    firebaseViewModel.chattingWith?.userId.toString(),
                    firebaseViewModel.forwarded?.message.toString(),
                    null,
                    context = ctx,
                )
                taskViewModel.isForwarding = false
            }
        }
    ) {
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp)
            ,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Box {
                AsyncImage(
                    model = userData.profilePictureUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .clip(RoundedCornerShape(25.dp))
                        .size(55.dp)
                )
                if(userData.online == true){
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color.Green,
                                shape = CircleShape
                            )
                            .size(10.dp)
                            .align(Alignment.TopEnd)
                    )
                }
                if (!userData.currentEmotion.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(24.dp) // Adjust size for the emoji
                            .align(Alignment.BottomStart) // Adjust alignment if needed
                    ) {
                        Text(
                            text = userData.currentEmotion.orEmpty(), // Display the emoji
                            fontSize = 18.sp, // Adjust font size to suit your design
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp)
            ) {
                Text(
                    text = userData.username.toString(),
                    fontSize = 22.sp,
                    maxLines = 1,
                    fontWeight = FontWeight.Bold,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                )
                if(userData.blocked?.contains(firebaseViewModel.userData?.userId.toString()) == false){
                    Row (
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ){
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if(userData.latestMessage?.message != null){
                                Text(
                                    text = userData.latestMessage?.message.toString(),
                                    color = Color.Gray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                        if(userData.latestMessage?.time!=null){
                            Text(
//                                text = userData.latestMessage?.time.toString(),
                                text = taskViewModel.getTime(userData.latestMessage?.time).toString(),
                                color = Color.Gray,
                            )
                        }
                    }
                }
            }
        }
    }
}