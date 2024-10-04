package com.heril.talkfusion.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.heril.talkfusion.R
import com.heril.talkfusion.data.MessageData
import com.heril.talkfusion.ui.viewmodels.FirebaseViewModel
import com.heril.talkfusion.ui.viewmodels.TaskViewModel


@OptIn(ExperimentalFoundationApi::class, ExperimentalGlideComposeApi::class)
@Composable
fun chatCard(
    messageData: MessageData,
    index: Int,
    firebaseViewModel: FirebaseViewModel,
    taskViewModel: TaskViewModel
){
    val chatList by firebaseViewModel.chatMessages.collectAsState()
    val roundedCornerRadius = 30
    val fontSize = 16
    val ctx = LocalContext.current

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        AnimatedVisibility(visible = firebaseViewModel.selectedMessage?.time.toString() == messageData.time.toString()) {
            Card(
                modifier = Modifier
                    .padding(5.dp)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                )
            ) {
                val emojiList = mutableListOf("\uD83D\uDC4D","\uD83D\uDC4E","❤\uFE0F","\uD83D\uDE2E","\uD83D\uDE2D","\uD83D\uDE02")
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement =
                    if(firebaseViewModel.userData?.userId == messageData.senderID){
                        Arrangement.End
                    }else{
                        Arrangement.Start
                    },
                ) {
                    emojiList.forEach{
                        Box(
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.secondary.copy(0.5f),
                                    shape = CircleShape
                                )
                                .combinedClickable(
                                    onClick = {
                                        if (messageData.curUserReaction == it) {
                                            messageData.curUserReaction = null
                                            firebaseViewModel.editMessage(
                                                firebaseViewModel.chattingWith?.userId.toString(),
                                                messageData.time!!,
                                                messageData.message.toString(),
                                                ctx,
                                                null
                                            )
                                        } else {
                                            messageData.curUserReaction = it
                                            firebaseViewModel.editMessage(
                                                firebaseViewModel.chattingWith?.userId.toString(),
                                                messageData.time!!,
                                                messageData.message.toString(),
                                                ctx,
                                                messageData.curUserReaction
                                            )
                                        }
                                        firebaseViewModel.selectedMessage = null
                                        taskViewModel.chatOptions = false
                                    }
                                )
                        ){
                            Text(
                                text = it,
                                modifier = Modifier
                                    .padding(10.dp),
                                fontSize = 20.sp
                            )
                        }
                        Spacer(modifier = Modifier.size(5.dp))
                    }
                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.secondary.copy(0.5f),
                                shape = CircleShape
                            )
                            .clickable {
                                taskViewModel.allEmojis = true
                            }
                    ){
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(10.dp)
                                .size(26.dp)
                        )
                    }
                }
            }
        }
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
                .background(
                    color =
                    if (firebaseViewModel.selectedMessage?.time.toString() == messageData.time.toString() || firebaseViewModel.repliedToIndex.collectAsState().value == index || firebaseViewModel.searchIndex == index) {
                        MaterialTheme.colorScheme.secondary.copy(0.5f)
                    } else {
                        Color.Transparent
                    }
                )
                .combinedClickable(
                    onLongClick = {
                        if (firebaseViewModel.chattingWith?.blocked?.contains(firebaseViewModel.userData?.userId.toString()) == false) {
                            firebaseViewModel.selectedMessage = messageData

                            taskViewModel.chatOptions = true
                        }
                    },
                    onClick = {
                        if (firebaseViewModel.chattingWith?.blocked?.contains(firebaseViewModel.userData?.userId.toString()) == false) {
                            taskViewModel.chatOptions = false
                            firebaseViewModel.selectedMessage = null
                            firebaseViewModel.repliedTo = null
                        }
                    },
                    onDoubleClick = {
                        firebaseViewModel.repliedTo = messageData
                    }
                )
        ){
            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start =
                        if (messageData.senderID == firebaseViewModel.userData?.userId) {
                            35.dp
                        } else {
                            0.dp
                        },
                        end =
                        if (messageData.senderID == firebaseViewModel.userData?.userId) {
                            0.dp
                        } else {
                            35.dp
                        },
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement =
                if(messageData.senderID==firebaseViewModel.userData?.userId){
                    Arrangement.End
                }else{
                    Arrangement.Start
                },
            ){
                Card(
                    modifier = Modifier
                        .padding(horizontal = 5.dp),
                    shape =
                        RoundedCornerShape(
                            topStart =
                            if(messageData.senderID!=firebaseViewModel.userData?.userId){
                                if(index!=0){
                                    if(chatList[index-1].senderID!=firebaseViewModel.userData?.userId){
                                        5.dp
                                    }else{
                                        roundedCornerRadius.dp
                                    }
                                }else{
                                    roundedCornerRadius.dp
                                }
                            }else{
                                roundedCornerRadius.dp
                            },
                            topEnd =
                            if(messageData.senderID==firebaseViewModel.userData?.userId){
                                if(index!=0){
                                    if(chatList[index-1].senderID==firebaseViewModel.userData?.userId){
                                        5.dp
                                    }else{
                                        roundedCornerRadius.dp
                                    }
                                }else{
                                    roundedCornerRadius.dp
                                }
                            }else{
                                roundedCornerRadius.dp
                            },
                            bottomStart =
                            if(messageData.senderID!=firebaseViewModel.userData?.userId){
                                if(index!=chatList.size-1){
                                    if(chatList[index+1].senderID!=firebaseViewModel.userData?.userId){
                                        5.dp
                                    }else{
                                        roundedCornerRadius.dp
                                    }
                                }else{
                                    roundedCornerRadius.dp
                                }
                            }else{
                                roundedCornerRadius.dp
                            },
                            bottomEnd =
                            if(messageData.senderID==firebaseViewModel.userData?.userId){
                                if(index!=chatList.size-1){
                                    if(chatList[index+1].senderID==firebaseViewModel.userData?.userId){
                                        5.dp
                                    }else{
                                        roundedCornerRadius.dp
                                    }
                                }else{
                                    roundedCornerRadius.dp
                                }
                            }else{
                                roundedCornerRadius.dp
                            }
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor =
                        if(messageData.senderID==firebaseViewModel.userData?.userId){
                                MaterialTheme.colorScheme.primaryContainer
                        }else{
                                MaterialTheme.colorScheme.surface
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier,
                        horizontalAlignment =
                        if(messageData.senderID==firebaseViewModel.userData?.userId)
                            Alignment.End
                        else
                            Alignment.Start,
                    ){
                        if(messageData.repliedTo!=null){
                            val user =
                                if(messageData.repliedTo?.senderID==firebaseViewModel.userData?.userId)
                                    "You"
                                else{
                                    firebaseViewModel.chattingWith?.username
                                }
                            Card(
                                modifier = Modifier
                                    .padding(
                                        top = 3.dp,
                                        bottom = 3.dp,
                                        start = 3.dp,
                                        end = 3.dp
                                    )
                                    .clickable {
                                        messageData.repliedTo?.time?.let {
                                            firebaseViewModel.findRepliedToIndex(it)
                                        }
                                    },
                                shape = RoundedCornerShape(roundedCornerRadius.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor =
                                    if(messageData.senderID==firebaseViewModel.userData?.userId){
                                            MaterialTheme.colorScheme.surface
                                    }
                                    else{
                                            MaterialTheme.colorScheme.secondaryContainer
                                    }
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                ) {
                                    Box {
                                        Text(
                                            text = messageData.message.toString() + "   ",
                                            fontSize = fontSize.sp,
                                            color =
                                            if(messageData.senderID==firebaseViewModel.userData?.userId){
                                                    MaterialTheme.colorScheme.surface
                                            }
                                            else {
                                                    MaterialTheme.colorScheme.secondaryContainer
                                            },
                                            maxLines = 1
                                        )
                                        Text(
                                            text = user.toString(),
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            fontSize = fontSize.sp
                                        )
                                    }
                                    Row (
                                        verticalAlignment = Alignment.CenterVertically
                                    ){
                                        Column(
                                            modifier = Modifier,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            if(messageData.repliedTo?.message?.isNotEmpty()==true){
                                                Text(
                                                    text =
                                                        messageData.repliedTo?.message.toString(),
                                                    color = Color.White,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    fontSize = fontSize.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if(messageData.message?.isNotEmpty() == true){
                            Text(
                                text = messageData.message.toString(),
                                modifier = Modifier
                                    .padding(
                                        start = 15.dp,
                                        top = if(messageData.repliedTo!=null) 0.dp else 5.dp,
                                        end = 15.dp
                                    ),
                                fontSize = fontSize.sp,
                                color = Color.White,
                            )
                        }
                        Row (
                            modifier = Modifier
                                .padding(
                                    start = 10.dp,
                                    end = 10.dp,
                                    bottom =
                                    if(messageData.curUserReaction!=null || messageData.otherUserReaction!=null)
                                        2.dp
                                    else
                                        0.dp,
                                    top = if((messageData.curUserReaction!=null || messageData.otherUserReaction!=null) && messageData.message?.isEmpty()==true)
                                        2.dp
                                    else
                                        0.dp
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            Text(
                                text = taskViewModel.getTime(messageData.time).toString(),
                                color = Color.Gray,
                                modifier = Modifier.padding(start = 5.dp, end = 5.dp),
                                fontSize = 12.sp,
                            )
                            if(messageData.isForwarded==true){
                                Text(
                                    text = "Forwarded",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.size(5.dp))
                            }
                            if(!(messageData.otherUserReaction.isNullOrBlank())) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = MaterialTheme.colorScheme.secondary.copy(0.5f),
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .padding(1.dp)
                                ){
                                    Row (
                                        verticalAlignment = Alignment.CenterVertically
                                    ){
                                        AsyncImage(
                                            model = firebaseViewModel.chattingWith?.profilePictureUrl,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .clip(shape = CircleShape)
                                                .size(25.dp)
                                        )
                                        Text(
                                            text = if(!messageData.otherUserReaction.isNullOrBlank()) messageData.otherUserReaction.toString() else "",
                                            modifier = Modifier
                                                .padding(
                                                    horizontal = 5.dp,
                                                    vertical = 3.dp
                                                )
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.size(5.dp))
                            }
                            if(!(messageData.curUserReaction.isNullOrBlank())) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = MaterialTheme.colorScheme.secondary.copy(0.5f),
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .padding(1.dp)
                                        .clickable {
                                            firebaseViewModel.editMessage(
                                                firebaseViewModel.chattingWith?.userId.toString(),
                                                messageData.time!!,
                                                messageData.message.toString(),
                                                ctx,
                                                null
                                            )
                                        }
                                ){
                                    Row (
                                        verticalAlignment = Alignment.CenterVertically
                                    ){
                                        AsyncImage(
                                            model = firebaseViewModel.profilePicture,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .clip(shape = CircleShape)
                                                .size(25.dp)
                                        )
                                        Text(
                                            text = if(!messageData.curUserReaction.isNullOrBlank()) messageData.curUserReaction.toString() else "",
                                            modifier = Modifier
                                                .padding(
                                                    horizontal = 5.dp,
                                                    vertical = 3.dp
                                                ),
                                        )
                                    }
                                }
                            }
                            if(messageData.senderID==firebaseViewModel.userData?.userId){
                                Icon(
                                    painter = painterResource(id = R.drawable.sentnotifiericon),
                                    contentDescription = null,
                                    tint =
                                    if(messageData.read == true && firebaseViewModel.userData?.userPref?.readRecipients==true){
                                        Color.White
                                    }else{
                                        Color.Gray
                                    },
                                    modifier = Modifier
                                        .size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}