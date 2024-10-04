package com.heril.talkfusion.ui

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.heril.talkfusion.R
import com.heril.talkfusion.ui.components.DeleteMessageDialog
import com.heril.talkfusion.ui.components.EmojiDialog
import com.heril.talkfusion.ui.components.ImageDialog
import com.heril.talkfusion.ui.components.chatCard
import com.heril.talkfusion.ui.viewmodels.FirebaseViewModel
import com.heril.talkfusion.ui.viewmodels.TaskViewModel
import com.heril.talkfusion.ui.viewmodels.TranslateViewModel
import com.heril.talkfusion.utils.LanguageUtils
import kotlinx.coroutines.launch


@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun personChatScreen(
    firebaseViewModel: FirebaseViewModel,
    taskViewModel: TaskViewModel,
    translateViewModel: TranslateViewModel,
    navController: NavController
){
    Log.d("PersonChatScreen", "Before FirebaseViewModel.getMessagesWithUser")
    firebaseViewModel.getMessagesWithUser()
    Log.d("PersonChatScreen", "After FirebaseViewModel.getMessagesWithUser")
    val chatList = firebaseViewModel.chatMessages.collectAsState()
    val userList by firebaseViewModel.chatListUsers.collectAsState()
    val searchMessageList by firebaseViewModel.searchMessages.collectAsState()
    Log.d("PersonChatScreen", "Chat Messages : $chatList")

    val supportedLanguages = LanguageUtils.getSupportedLanguages()
    var expanded by remember { mutableStateOf(false) }
    var translationEnabled by remember { mutableStateOf(false) }

    val fontSize = 16

    val ctx = LocalContext.current
    val corLaunch = rememberCoroutineScope()
    val listState = rememberLazyListState()

//    val translatedChatList = remember { mutableStateListOf<MessageData>() }
    // Launch effect to process translations
    LaunchedEffect(chatList, translateViewModel.selectedLanguage) {
        if (translationEnabled) {
            translateViewModel.translateChatMessages(chatList)
        }
    }

    DisposableEffect(Unit){
        taskViewModel.showNavBar = false
        onDispose {
            taskViewModel.showNavBar = true
            taskViewModel.expandedPersonInfo = false
            firebaseViewModel.text = ""
            firebaseViewModel.stopConversationsListener()
            firebaseViewModel.updateTypingStatus(false)
            firebaseViewModel.repliedTo = null
            firebaseViewModel.searchText = ""
            firebaseViewModel.searchListIndex = null
            firebaseViewModel.searchIndex = null
            taskViewModel.searchMessages = false
        }
    }

    if(taskViewModel.showDeleteMsgDialog){
        DeleteMessageDialog(
            taskViewModel = taskViewModel,
            firebaseViewModel = firebaseViewModel,
            navController = navController
        )
    }

    if(taskViewModel.showImageDialog){
        ImageDialog(
            taskViewModel = taskViewModel,
            firebaseViewModel = firebaseViewModel,
            navController = navController
        )
    }
    if(taskViewModel.allEmojis){
        EmojiDialog(
            taskViewModel = taskViewModel,
            firebaseViewModel = firebaseViewModel
        )
    }
    Column(
        modifier =
        if(taskViewModel.searchMessages) Modifier
            .fillMaxSize()
            .imePadding() else Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black
            ),
            shape = RectangleShape,
            onClick = {
                if(!taskViewModel.searchMessages){
                    taskViewModel.expandedPersonInfo = !taskViewModel.expandedPersonInfo
                }
            }
        ) {
            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(
                        top = 5.dp,
                        bottom = 10.dp,
                        start = 10.dp,
                        end = 10.dp
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                if(taskViewModel.searchMessages) {
                    Row (
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Row (
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ){
                            IconButton(
                                onClick = {
                                    taskViewModel.searchMessages = false
                                    firebaseViewModel.searchText = ""
                                    firebaseViewModel.searchListIndex = null
                                    firebaseViewModel.searchIndex = null
                                }
                            ){
                                Icon(
                                    painter = painterResource(id = R.drawable.backicon),
                                    contentDescription = null,
                                    modifier = Modifier.size(25.dp)
                                )
                            }
                            TextField(
                                value = firebaseViewModel.searchText,
                                onValueChange = {
                                    firebaseViewModel.searchText = it
                                    firebaseViewModel.updateSearchMessageList(
                                        chatList.value,
                                        firebaseViewModel.searchText
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                singleLine = true,
                                maxLines = 1,
                                label = {
                                    Text(text = "Search Messages")
                                },
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Search
                                ),
                                keyboardActions = KeyboardActions(
                                    onSearch = {
                                        firebaseViewModel.updateSearchMessageIndex()
                                        if(firebaseViewModel.searchIndex!=null){
                                            corLaunch.launch {
                                                listState.animateScrollToItem(chatList.value.size-1 - firebaseViewModel.searchIndex!!)
                                            }
                                        }
                                    }
                                )
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(35.dp)
                                    .clickable {
                                        if (firebaseViewModel.searchListIndex != null && firebaseViewModel.searchListIndex != searchMessageList.size - 1) {
                                            firebaseViewModel.searchListIndex =
                                                firebaseViewModel.searchListIndex!! + 1
                                            firebaseViewModel.updateSearchMessageIndex()
                                            if (firebaseViewModel.searchIndex != null) {
                                                corLaunch.launch {
                                                    listState.animateScrollToItem(chatList.value.size - 1 - firebaseViewModel.searchIndex!!)
                                                }
                                            }
                                        }
                                    },
                                tint =
                                if(firebaseViewModel.searchListIndex == null || firebaseViewModel.searchListIndex == searchMessageList.size-1){
                                    Color.Gray
                                }else
                                    LocalContentColor.current
                            )
                            Spacer(modifier = Modifier.size(5.dp))
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowUp,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(35.dp)
                                    .clickable {
                                        if (firebaseViewModel.searchListIndex != null && firebaseViewModel.searchListIndex != 0) {
                                            firebaseViewModel.searchListIndex =
                                                firebaseViewModel.searchListIndex!! - 1
                                            firebaseViewModel.updateSearchMessageIndex()
                                            if (firebaseViewModel.searchIndex != null) {
                                                corLaunch.launch {
                                                    listState.animateScrollToItem(chatList.value.size - 1 - firebaseViewModel.searchIndex!!)
                                                }
                                            }
                                        }
                                    },
                                tint =
                                if(firebaseViewModel.searchListIndex == null || firebaseViewModel.searchListIndex == 0){
                                    Color.Gray
                                }else
                                    LocalContentColor.current
                            )
                        }
                    }
                }
                else{
                    IconButton(
                        onClick = {
                            navController.navigateUp()
                        }
                    ){
                        Icon(
                            painter = painterResource(id = R.drawable.backicon),
                            contentDescription = null,
                            modifier = Modifier.size(25.dp)
                        )
                    }
                    Row (
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        AsyncImage(
                            model = firebaseViewModel.chattingWith?.profilePictureUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(shape = CircleShape)
                        )
                        Spacer(modifier = Modifier.size(10.dp))
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = firebaseViewModel.chattingWith?.username.toString(),
                                fontSize = (fontSize!!+6).sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            AnimatedVisibility(visible = userList.first { it.userId == firebaseViewModel.chattingWith?.userId }.online==true) {
                                Text(
                                    text = "Online",
                                    fontSize = (fontSize -2).sp
                                )
                            }
                        }
                    }
                    IconButton(onClick = {
                        taskViewModel.searchMessages = !taskViewModel.searchMessages
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null,
                            modifier = Modifier.size(25.dp)
                        )
                    }
                    IconButton(onClick = {
                        if(!taskViewModel.searchMessages){
                            taskViewModel.morePersonChatOptions = !taskViewModel.morePersonChatOptions
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = null,
                            modifier = Modifier.size(25.dp)
                        )
                    }
                }
            }
            AnimatedVisibility(
                visible = taskViewModel.expandedPersonInfo,
            ){
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Details",
                        fontSize = (fontSize!!+6).sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.size(5.dp))
                    Text(
                        text = firebaseViewModel.chattingWith?.mail.toString(),
                        fontSize = (fontSize+3).sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Mail",
                        fontSize = fontSize.sp,
                        color = Color.Gray
                    )
                    if(firebaseViewModel.chattingWith?.bio!!.isNotEmpty()){
                        Spacer(modifier = Modifier.size(5.dp))
                        Text(
                            text = firebaseViewModel.chattingWith?.bio.toString(),
                            fontSize = 15.sp,
                            maxLines = 5,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Bio",
                            fontSize = fontSize.sp,
                            color = Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.size(15.dp))
                }
            }
            AnimatedVisibility(
                visible = taskViewModel.morePersonChatOptions,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Translate to :",
                        fontSize = (fontSize!! + 6).sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.size(5.dp))
                    // Row to align Dropdown and IconButton horizontally
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Exposed Dropdown Menu
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded },
                            modifier = Modifier.weight(1f) // Take available space
                        ) {
                            TextField(
                                value = LanguageUtils.getLanguageDisplayName(translateViewModel.selectedLanguage),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Select Language") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                },
                                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                                modifier = Modifier.menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                supportedLanguages.forEach { code ->
                                    DropdownMenuItem(
                                        text = { Text(LanguageUtils.getLanguageDisplayName(code)) },
                                        onClick = {
                                            translateViewModel.onLanguageChanged(code)
                                            translationEnabled = true
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp)) // Space between dropdown and icon
                        // IconButton
                        AnimatedVisibility(visible = translationEnabled) {
                            IconButton(
                                onClick = { translationEnabled = false }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Clear,
                                    contentDescription = "Disable translation",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.size(15.dp))
                }
            }
            AnimatedVisibility(taskViewModel.chatOptions) {
                Row (
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ){
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(0.9f),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Row (
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 10.dp,
                            end = 10.dp,
                            bottom = 10.dp,
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ){
                    IconButton(
                        onClick = {
                            taskViewModel.chatOptions = false
                            firebaseViewModel.selectedMessage = null
                            taskViewModel.isEditing = false
                            firebaseViewModel.text = ""
                            firebaseViewModel.mediaUri = null
                        }
                    ){
                        Icon(
                            painter = painterResource(id = R.drawable.backicon),
                            contentDescription = null,
                            modifier = Modifier
                                .size(25.dp)
                                .rotate(90f)
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        AnimatedVisibility(firebaseViewModel.selectedMessage?.senderID == firebaseViewModel.userData?.userId){
                            IconButton(
                                onClick = {
                                    firebaseViewModel.text = firebaseViewModel.selectedMessage?.message.toString()
                                    taskViewModel.isEditing = true
                                }
                            ){
                                Icon(
                                    painter = painterResource(id = R.drawable.editicon),
                                    contentDescription = null,
                                    modifier = Modifier.size(25.dp)
                                )
                            }
                        }
                        IconButton(
                            onClick = {
                                if(firebaseViewModel.selectedMessage?.message.toString().isNotEmpty()){
                                    taskViewModel.copyToClipboard(ctx,firebaseViewModel.selectedMessage?.message.toString())
                                    firebaseViewModel.selectedMessage = null
                                    taskViewModel.chatOptions = false
                                }
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.copyicon),
                                contentDescription = null,
                                modifier = Modifier.size(25.dp)
                            )
                        }
                        IconButton(
                            onClick = {
                                taskViewModel.showDeleteMsgDialog = true
                            }
                        ){
                            Icon(
                                painter = painterResource(id = R.drawable.deleteimageicon),
                                contentDescription = null,
                                modifier = Modifier.size(25.dp)
                            )
                        }
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
        ){
            Image(
                painter = painterResource(id = R.drawable.i0),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary.copy(0.5f), blendMode = BlendMode.Overlay),
                alpha = 1f
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                val index = firebaseViewModel.repliedToIndex.collectAsState()
                LaunchedEffect(Unit) {
                    listState.animateScrollToItem(0)
                }
                LaunchedEffect(chatList.value.size) {
                    listState.animateScrollToItem(0)
                }
                LaunchedEffect(index.value) {
                    if(index.value!=null){
                        listState.animateScrollToItem(chatList.value.size-1- index.value!!)
                    }
                }
                var currentIndex by remember { mutableIntStateOf(0) }
                LaunchedEffect(listState) {
                    snapshotFlow { listState.firstVisibleItemIndex }
                        .collect { newIndex ->
                            currentIndex = newIndex
                        }
                }
                Box (
                    modifier = Modifier.weight(1f)
                ){
                    val sortedList by remember(translateViewModel.translatedChatList, chatList.value) {
                        derivedStateOf {
                            if (translationEnabled) {
                                Log.d("ComposeUI", "Translation is enabled")
                                if (translateViewModel.translatedChatList.isNotEmpty()) {
                                    Log.d("ComposeUI", "Using translatedChatList")
                                    translateViewModel.translatedChatList
                                } else {
                                    Log.d("ComposeUI", "Waiting...")
                                    translateViewModel.translatedChatList
                                }
                            } else {
                                Log.d("ComposeUI", "Translation is not enabled")
                                chatList.value
                            }
                        }
                    }
//                    LazyColumn(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .height(50.dp),
//                        reverseLayout = true,
//                        state = listState
//                    ) {
//                        itemsIndexed(
//                            items = sortedList,
//                            key = { index, item -> "${item.time}" }
//                        ) { index, message ->
//                            val dismissState = rememberSwipeToDismissBoxState(
//                                initialValue = SwipeToDismissBoxValue.Settled,
//                                positionalThreshold = { swipeActivationFloat -> swipeActivationFloat / 10 },
//                                confirmValueChange = {
//                                    when (it) {
//                                        SwipeToDismissBoxValue.StartToEnd -> {
//                                            firebaseViewModel.repliedTo = message
//                                            false
//                                        }
//                                        SwipeToDismissBoxValue.EndToStart -> {
//                                            taskViewModel.isForwarding = true
//                                            firebaseViewModel.forwarded = message
//                                            firebaseViewModel.forwarded?.isForwarded = true
//                                            navController.navigate("ForwardScreen")
//                                            false
//                                        }
//                                        else -> false
//                                    }
//                                }
//                            )
//
//                            SwipeToDismissBox(
//                                state = dismissState,
//                                backgroundContent = {
//                                    AnimatedVisibility(
//                                        visible = dismissState.targetValue == SwipeToDismissBoxValue.StartToEnd
//                                    ) {
//                                        Row(
//                                            modifier = Modifier.fillMaxHeight(),
//                                            verticalAlignment = Alignment.CenterVertically,
//                                        ) {
//                                            Spacer(modifier = Modifier.size(10.dp))
//                                            Icon(
//                                                painter = painterResource(id = R.drawable.reply),
//                                                contentDescription = null,
//                                                tint = Color.White,
//                                                modifier = Modifier.size(50.dp)
//                                            )
//                                            Spacer(modifier = Modifier.size(10.dp))
//                                            Text(
//                                                text = "Reply",
//                                                fontWeight = FontWeight.Bold
//                                            )
//                                        }
//                                    }
//                                    AnimatedVisibility(
//                                        visible = dismissState.targetValue == SwipeToDismissBoxValue.EndToStart,
//                                        enter = slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth }),
//                                        exit = slideOutHorizontally(targetOffsetX = { fullWidth -> fullWidth })
//                                    ) {
//                                        Row(
//                                            modifier = Modifier.fillMaxSize(),
//                                            verticalAlignment = Alignment.CenterVertically,
//                                            horizontalArrangement = Arrangement.End
//                                        ) {
//                                            Text(
//                                                text = "Forward",
//                                                fontWeight = FontWeight.Bold
//                                            )
//                                            Spacer(modifier = Modifier.size(10.dp))
//                                            Icon(
//                                                painter = painterResource(id = R.drawable.forward),
//                                                contentDescription = null,
//                                                tint = Color.White,
//                                                modifier = Modifier.size(50.dp)
//                                            )
//                                            Spacer(modifier = Modifier.size(20.dp))
//                                        }
//                                    }
//                                },
//                                content = {
//                                    chatCard(
//                                        message,
//                                        chatList.value.size - 1 - index,
//                                        firebaseViewModel,
//                                        taskViewModel
//                                    )
//                                },
//                            )
//                        }
//                    }

                    val groupedMessages by remember(sortedList) { derivedStateOf { taskViewModel.groupMessagesByDate(sortedList) } }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .height(50.dp),
                        reverseLayout = true,
                        state = listState
                    ) {
                        groupedMessages.forEach { group ->
                            // Messages for this group
                            itemsIndexed(group.messages) { index, message ->
                                val dismissState = rememberSwipeToDismissBoxState(
                                    initialValue = SwipeToDismissBoxValue.Settled,
                                    positionalThreshold = { swipeActivationFloat -> swipeActivationFloat / 10 },
                                    confirmValueChange = {
                                        when (it) {
                                            SwipeToDismissBoxValue.StartToEnd -> {
                                                firebaseViewModel.repliedTo = message
                                                false
                                            }
                                            SwipeToDismissBoxValue.EndToStart -> {
                                                taskViewModel.isForwarding = true
                                                firebaseViewModel.forwarded = message
                                                firebaseViewModel.forwarded?.isForwarded = true
                                                navController.navigate("ForwardScreen")
                                                false
                                            }
                                            else -> false
                                        }
                                    }
                                )

                                SwipeToDismissBox(
                                    state = dismissState,
                                    backgroundContent = {
                                        AnimatedVisibility(
                                            visible = dismissState.targetValue == SwipeToDismissBoxValue.StartToEnd
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxHeight(),
                                                verticalAlignment = Alignment.CenterVertically,
                                            ) {
                                                Spacer(modifier = Modifier.size(10.dp))
                                                Icon(
                                                    painter = painterResource(id = R.drawable.reply),
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(50.dp)
                                                )
                                                Spacer(modifier = Modifier.size(10.dp))
                                                Text(
                                                    text = "Reply",
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        AnimatedVisibility(
                                            visible = dismissState.targetValue == SwipeToDismissBoxValue.EndToStart,
                                            enter = slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth }),
                                            exit = slideOutHorizontally(targetOffsetX = { fullWidth -> fullWidth })
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxSize(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.End
                                            ) {
                                                Text(
                                                    text = "Forward",
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.size(10.dp))
                                                Icon(
                                                    painter = painterResource(id = R.drawable.forward),
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(50.dp)
                                                )
                                                Spacer(modifier = Modifier.size(20.dp))
                                            }
                                        }
                                    },
                                    content = {
                                        chatCard(
                                            message,
                                            group.messages.size - 1 - index,
                                            firebaseViewModel,
                                            taskViewModel
                                        )
                                    },
                                )
                            }

                            // Date header for this group
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {
                                    Text(
                                        text = group.date,
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .background(
                                                color = Color.Gray.copy(alpha = 0.7f),
                                                shape = RoundedCornerShape(16.dp)
                                            )
                                            .padding(vertical = 4.dp, horizontal = 12.dp),
                                        style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }
                    Row (
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.Bottom
                    ){
                        AnimatedVisibility(visible = currentIndex >= 3 && !taskViewModel.searchMessages) {
                            FloatingActionButton(
                                onClick = {
                                    corLaunch.launch {
                                        listState.animateScrollToItem(0)
                                    }
                                },
                                modifier = Modifier.padding(10.dp),
                                containerColor =
                                    MaterialTheme.colorScheme.surface
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.KeyboardArrowDown,
                                    contentDescription = null
                                )
                            }
                        }
                    }
                }
                AnimatedVisibility(visible =userList.first { it.userId == firebaseViewModel.chattingWith?.userId }.typing == firebaseViewModel.userData?.userId) {
                    Row (
                        modifier = Modifier.fillMaxWidth(0.9f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ){
                        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.typinganimation))
                        LottieAnimation(
                            composition = composition,
                            iterations = LottieConstants.IterateForever,
                            modifier = Modifier.size(60.dp)
                        )
                        Spacer(modifier = Modifier.size(10.dp))
                        Text(text = "Typing...", fontWeight = FontWeight.Bold)
                    }
                }
                AnimatedVisibility(visible = firebaseViewModel.mediaUri!=null){
                    ElevatedCard(
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .weight(1f),
                        shape = RoundedCornerShape(20.dp)
                    ){
                        Column(
                            modifier = Modifier.padding(3.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ){
                                Text(
                                    text =
                                    if(taskViewModel.isEditing){
                                        "Edit Message"
                                    }
                                    else{
                                        ""
                                    },
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if(taskViewModel.isEditing){
                                    IconButton(onClick = {
                                        firebaseViewModel.mediaUri = null
                                        firebaseViewModel.text = ""
                                        firebaseViewModel.selectedMessage = null
                                        taskViewModel.isEditing = false
                                        taskViewModel.chatOptions = false
                                    }) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.crossicon),
                                            contentDescription =  null,
                                            modifier = Modifier.size(15.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }else{
                                    IconButton(onClick = {
                                        firebaseViewModel.mediaUri = null
                                    }) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.deleteimageicon),
                                            contentDescription =  null,
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                                AsyncImage(
                                    model = firebaseViewModel.mediaUri,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxHeight(0.9f)
                                        .fillMaxWidth(0.9f)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(
                                            color = Color.Black,
                                            shape = RoundedCornerShape(20.dp)
                                        ),
                                    contentScale = ContentScale.Crop
                                )
                        }
                    }
                }
                AnimatedVisibility(visible = firebaseViewModel.repliedTo!=null) {
                    val user =
                        if(firebaseViewModel.repliedTo?.senderID == firebaseViewModel.userData?.userId)
                            "Yourself"
                        else{
                            firebaseViewModel.chattingWith?.username
                        }
                    ElevatedCard {
                        Column(
                            modifier = Modifier
                                .padding(10.dp)
                                .fillMaxWidth(0.9f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row (
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ){
                                Row (
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ){
                                    Icon(
                                        painter = painterResource(id = R.drawable.reply),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(30.dp)
                                    )
                                    Text(
                                        text = "Replying to $user",
                                        textAlign = TextAlign.Start,
                                        overflow = TextOverflow.Ellipsis,
                                        maxLines = 1,
                                    )
                                }
                                IconButton(
                                    onClick = {firebaseViewModel.repliedTo=null},
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Clear,
                                        contentDescription = null
                                    )
                                }
                            }
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp)
                                    .animateContentSize()
                            ) {
                                Row (
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ){
                                    AnimatedVisibility(visible = firebaseViewModel.repliedTo?.message != "") {
                                        Column(
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = if(firebaseViewModel.repliedTo?.message.isNullOrEmpty()) "" else firebaseViewModel.repliedTo?.message.toString(),
                                                modifier = Modifier.padding(
                                                    start = 10.dp,
                                                    end = 10.dp,
                                                    bottom = 10.dp,
                                                    top = 10.dp
                                                ),
                                                color = Color.White,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                                fontSize = fontSize!!.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if(firebaseViewModel.chattingWith?.blocked?.contains(firebaseViewModel.userData?.userId.toString()) == true){
                    Text(
                        text = "You cannot Message this Person Anymore",
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        fontWeight = FontWeight.Bold
                    )
                }else{
                    AnimatedVisibility(visible = !taskViewModel.searchMessages) {
                        Row (
                            modifier = Modifier.fillMaxWidth(0.9f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ){
                            OutlinedTextField(
                                value = firebaseViewModel.text,
                                onValueChange = {
                                        newText -> firebaseViewModel.text = newText
                                    firebaseViewModel.updateTypingStatus()
                                },
                                shape = RoundedCornerShape(30.dp),
                                label = {
                                    Text(
                                        text = "Message",
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                maxLines = 4,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = Color.Black,
                                    focusedContainerColor = Color.Black,
                                    unfocusedBorderColor = Color.Transparent,
                                )
                            )
                            AnimatedVisibility(visible = firebaseViewModel.mediaUri!=null || firebaseViewModel.text.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        firebaseViewModel.updateTypingStatus(false)
                                        if(taskViewModel.isEditing){
                                            firebaseViewModel.editMessage(
                                                firebaseViewModel.chattingWith?.userId.toString(),
                                                firebaseViewModel.selectedMessage?.time!!,
                                                firebaseViewModel.text,
                                                ctx,
                                                if(firebaseViewModel.selectedMessage!!.curUserReaction==null){
                                                    null
                                                }else{
                                                    firebaseViewModel.selectedMessage!!.curUserReaction
                                                }
                                            )
                                        }else{
                                            firebaseViewModel.SendMessage(
                                                firebaseViewModel.chattingWith?.userId.toString(),
                                                firebaseViewModel.text,
                                                repliedTo = firebaseViewModel.repliedTo,
                                                context = ctx
                                            )
                                            if(translationEnabled) {
                                                translationEnabled = false
                                                translationEnabled = true
                                                Log.d("SendMessage", "Recompose")
                                            }
                                        }
                                        firebaseViewModel.text = ""
                                        firebaseViewModel.mediaUri = null
                                        taskViewModel.isEditing = false
                                        taskViewModel.chatOptions = false
                                        firebaseViewModel.selectedMessage = null
                                        firebaseViewModel.repliedTo = null
                                    }
                                ){
                                    Icon(
                                        painter = painterResource(id = R.drawable.sendicon),
                                        contentDescription = null,
                                        modifier = Modifier.size(30.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}