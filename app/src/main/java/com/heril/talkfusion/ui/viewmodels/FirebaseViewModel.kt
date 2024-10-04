package com.heril.talkfusion.ui.viewmodels

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.storage
import com.heril.talkfusion.R
import com.heril.talkfusion.data.MessageData
import com.heril.talkfusion.data.UserData
import com.heril.talkfusion.data.UserPref
import com.heril.talkfusion.data.callNotifAPI
import com.heril.talkfusion.ui.components.allEmojis
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.UUID

class FirebaseViewModel: ViewModel() {

    var userData: UserData? = null
    var chattingWith by mutableStateOf<UserData?>(null)
    var text by mutableStateOf("")
    var mediaUri by mutableStateOf<Uri?>(null)
    var newUser by mutableStateOf("")
    var imageString by mutableStateOf("")
    var mediaViewText by mutableStateOf("")
    var Bio by mutableStateOf("")
    var selectedMessage by mutableStateOf<MessageData?>(null)
    var repliedTo by mutableStateOf<MessageData?>(null)
    var forwarded by mutableStateOf<MessageData?>(null)
    var searchContact by mutableStateOf("")
    var profilePicture by mutableStateOf("")
    var curUserStatus by mutableStateOf(false)
    var sentBy by mutableStateOf("")
    var imageDialogProfilePicture by mutableStateOf("")
    var isLoadingUsers = false
    var isLoadingChat = false
    var searchText by mutableStateOf("")
    var searchIndex by mutableStateOf<Int?>(null)
    var searchListIndex by mutableStateOf<Int?>(null)

    private var firebase: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val _sentRequests = MutableStateFlow<List<UserData>>(emptyList())
    val sentRequests: StateFlow<List<UserData>> = _sentRequests
    private val _receivedRequests = MutableStateFlow<List<UserData>>(emptyList())
    val receivedRequests: StateFlow<List<UserData>> = _receivedRequests
    private val _chatListUsers = MutableStateFlow<List<UserData>>(emptyList())
    val chatListUsers: StateFlow<List<UserData>> = _chatListUsers
    val _chatMessages = MutableStateFlow<List<MessageData>>(emptyList())
    val chatMessages: StateFlow<List<MessageData>> = _chatMessages
    private val _blockedUsers = MutableStateFlow<List<UserData>>(emptyList())
    val blockedUsers : StateFlow<List<UserData>> = _blockedUsers
    private val _searchContacts = MutableStateFlow<List<UserData>>(emptyList())
    val searchContacts : StateFlow<List<UserData>> = _searchContacts
    private var conversationsListener: ListenerRegistration? = null
    private var requestListener: ListenerRegistration?= null
    private val _searchMessages = MutableStateFlow<List<MessageData>>(emptyList())
    val searchMessages : StateFlow<List<MessageData>> = _searchMessages
    private val _repliedToIndex = MutableStateFlow<Int?>(null)
    val repliedToIndex : StateFlow<Int?> = _repliedToIndex
    val listeners = mutableListOf<ListenerRegistration>()

//    fun loadChatListUsers() {
//        if (isLoadingUsers) {
//            Log.d("FirebaseViewModel", "Already loading users, skipping load.")
//            return
//        }
//        isLoadingUsers = true
//        Log.d("FirebaseViewModel", "Started loading chat list users.")
//
//        viewModelScope.launch(Dispatchers.IO) {
//            val chatListUsers = mutableListOf<UserData>()
//            val currentUserDoc = firebase.collection("users").document(userData?.userId.toString())
//            Log.d("FirebaseViewModel", "Current userId: ${userData?.userId}")
//
//            try {
//                // Fetch the current user data
//                val currentUser = currentUserDoc.get().await().toObject(UserData::class.java)
//                Log.d("FirebaseViewModel", "Current user data: $currentUser")
//
//                if (currentUser != null) {
//                    // Fetch blocked users data
//                    fetchBlockedUsersData()
//                    Log.d("FirebaseViewModel", "Blocked users data fetched.")
//
//                    val friendQueries = mutableListOf<Deferred<DocumentSnapshot>>()
//                    val latestMessageQueries = mutableListOf<Deferred<DocumentSnapshot>>()
//
//                    for (userId in currentUser.chatList!!) {
//                        Log.d("FirebaseViewModel", "Fetching data for user ID: $userId")
//                        val friendRef = firebase.collection("users").document(userId)
//                        val latestMessageRef = firebase.collection("latest_messages")
//                            .document(userData?.userId.toString() + "_" + userId)
//
//                        friendQueries.add(async { friendRef.get().await() })
//                        latestMessageQueries.add(async { latestMessageRef.get().await() })
//                    }
//
//                    val friendResults = friendQueries.awaitAll().map { it.toObject(UserData::class.java) }
//                    val latestMessageResults = latestMessageQueries.awaitAll().map { it.toObject(MessageData::class.java) }
//
//                    Log.d("FirebaseViewModel", "Friend results: $friendResults")
//                    Log.d("FirebaseViewModel", "Latest message results: $latestMessageResults")
//
//                    for (i in friendResults.indices) {
//                        friendResults[i]?.latestMessage = latestMessageResults[i]
//                        friendResults[i]?.let { chatListUsers.add(it) }
//                    }
//
//                    _chatListUsers.value = chatListUsers
//                    Log.d("FirebaseViewModel", "Chat list users updated: ${_chatListUsers.value}")
//
//                    userData?.let { addUserToFirestore(it) }
//                } else {
//                    Log.d("FirebaseViewModel", "Current user is null.")
//                }
//            } catch (e: Exception) {
//                Log.e("FirebaseViewModel", "Error loading chat list users", e)
//            } finally {
//                isLoadingUsers = false
//                Log.d("FirebaseViewModel", "Finished loading chat list users.")
//            }
//        }
//    }

    fun loadChatListUsers() {
        if (isLoadingUsers) {
            Log.d("FirebaseViewModel", "Already loading users, skipping load.")
            return
        }
        isLoadingUsers = true
        Log.d("FirebaseViewModel", "Started loading chat list users.")

        viewModelScope.launch(Dispatchers.IO) {
            val chatListUsers = mutableListOf<UserData>()
            val currentUserDoc = firebase.collection("users").document(userData?.userId.toString())
            Log.d("FirebaseViewModel", "Current userId: ${userData?.userId}")

            try {
                // Fetch the current user data
                val currentUser = currentUserDoc.get().await().toObject(UserData::class.java)
                Log.d("FirebaseViewModel", "Current user data: $currentUser")

                if (currentUser != null) {
                    // Fetch blocked users data
                    fetchBlockedUsersData()
                    Log.d("FirebaseViewModel", "Blocked users data fetched.")

                    val friendQueries = mutableListOf<Deferred<DocumentSnapshot>>()
                    val latestMessageQueries = mutableListOf<Deferred<DocumentSnapshot>>()

                    for (userId in currentUser.chatList!!) {
                        Log.d("FirebaseViewModel", "Fetching data for user ID: $userId")
                        val friendRef = firebase.collection("users").document(userId)
                        val latestMessageRef = firebase.collection("latest_messages")
                            .document(userData?.userId.toString() + "_" + userId)

                        friendQueries.add(async { friendRef.get().await() })
                        latestMessageQueries.add(async { latestMessageRef.get().await() })
                    }

                    val friendResults = friendQueries.awaitAll().map { it.toObject(UserData::class.java) }
                    val latestMessageResults = latestMessageQueries.awaitAll().mapNotNull { document ->
                        val messageData = document.toObject(MessageData::class.java)
                        val timestamp = document.get("time") as? Timestamp
                        val timeMillis = timestamp?.let { it.seconds * 1000L + it.nanoseconds / 1000000L } ?: 0L
                        messageData?.copy(time = timeMillis)
                    }

                    Log.d("FirebaseViewModel", "Friend results: $friendResults")
                    Log.d("FirebaseViewModel", "Latest message results: $latestMessageResults")

                    for (i in friendResults.indices) {
                        friendResults[i]?.latestMessage = latestMessageResults.getOrNull(i)
                        friendResults[i]?.let { chatListUsers.add(it) }
                    }

                    _chatListUsers.value = chatListUsers
                    Log.d("FirebaseViewModel", "Chat list users updated: ${_chatListUsers.value}")

                    userData?.let { addUserToFirestore(it) }
                } else {
                    Log.d("FirebaseViewModel", "Current user is null.")
                }
            } catch (e: Exception) {
                Log.e("FirebaseViewModel", "Error loading chat list users", e)
            } finally {
                isLoadingUsers = false
                Log.d("FirebaseViewModel", "Finished loading chat list users.")
            }
        }
    }

    fun filterBadWords(message: String, context: Context): String {
        // Get the bad words from the strings.xml file
        val badWords = context.resources.getStringArray(R.array.bad_words)
        // Replace each bad word with asterisks
        var filteredMessage = message
        badWords.forEach { badWord ->
            // Match bad words even if they are attached to other characters
            val regex = Regex("${Regex.escape(badWord)}", RegexOption.IGNORE_CASE)
            filteredMessage = filteredMessage.replace(regex) {
                "*".repeat(it.value.length)
            }
        }
        return filteredMessage
    }

    fun fetchBlockedUsersData() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentUserDoc = firebase.collection("users").document(userData?.userId.toString())
            val blockedUsersIds = currentUserDoc.get().await().toObject(UserData::class.java)?.blocked ?: emptyList()
            if (blockedUsersIds.isNotEmpty()) {
                val blockedUsersQuery = firebase.collection("users").whereIn(FieldPath.documentId(), blockedUsersIds)
                val blockedUsersSnapshot = blockedUsersQuery.get().await()
                val blockedUsersData = blockedUsersSnapshot.documents.mapNotNull { it.toObject(UserData::class.java) }
                _blockedUsers.value = blockedUsersData
            } else {
                _blockedUsers.value = emptyList()
            }
        }
    }

    fun setupLatestMessageListener() {
        Log.d("FirebaseViewModel", "Started setupLatestMessageListener")
        viewModelScope.launch(Dispatchers.IO) {
            delay(5000)
            chatListUsers.value.forEach { user ->
                val listener1 = firebase.collection("conversations").document(userData?.userId.toString())
                    .collection(user.userId.toString())
                    .orderBy("time", Query.Direction.DESCENDING)
                    .limit(1)
                    .addSnapshotListener { snapshots, error ->
                        if (error != null) {
                            return@addSnapshotListener
                        }
                        loadChatListUsers()
                    }
                listeners.add(listener1)
                val listener2 = firebase.collection("users").document(user.userId!!)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            return@addSnapshotListener
                        }
                        loadChatListUsers()
                    }
                listeners.add(listener2)
            }
        }
        Log.d("FirebaseViewModel", "Finished setupLatestMessageListener")
    }

    fun addUserToFirestore(user: UserData) {
        viewModelScope.launch (Dispatchers.IO){
            val userQuery = firebase.collection("users").document(user.userId.toString()).get().await()

            if (!userQuery.exists()) {
                firebase.collection("users").document(user.userId.toString())
                    .set(user)
                    .await()
                firebase.collection("users").document(user.userId.toString())
                    .update("chatList", emptyList<String>())
                    .await()
            }else{
                val currentUser = userQuery.toObject(UserData::class.java)
                userData?.bio = currentUser?.bio.toString()
                profilePicture = currentUser?.profilePictureUrl.toString()
                Bio = currentUser?.bio.toString()
                userData?.blocked = currentUser?.blocked
            }
        }
    }

    fun addUserToChatList(userMail: String, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val userQuery = firebase.collection("users").whereEqualTo("mail", userMail).get().await()
            if (!userQuery.isEmpty) {
                val otherUser = userQuery.documents.first().toObject(UserData::class.java)
                if (otherUser?.userId != userData?.userId) {
                    firebase.collection("users").document(userData?.userId.toString())
                        .update("chatList", FieldValue.arrayUnion(otherUser?.userId.toString()))
                        .await()
                    firebase.collection("users").document(otherUser?.userId.toString())
                        .update("chatList", FieldValue.arrayUnion(userData?.userId.toString())).addOnSuccessListener {
                            Toast.makeText(
                                context,
                                "User Added to Friend List",
                                Toast.LENGTH_SHORT
                            ).show()
                            loadChatListUsers()
                        }
                        .await()
                }
            }else{
                viewModelScope.launch (Dispatchers.Main){
                    Toast.makeText(
                        context,
                        "Given User has not Registered on the App!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    fun SendMessage(otherUserId: String, message: String, repliedTo: MessageData?, context: Context) {
        viewModelScope.launch {
            val filteredMessage = filterBadWords(message, context) // Pass context to filter bad words

            if (forwarded != null) {
                sendMessage(otherUserId, message = filteredMessage, repliedTo = repliedTo, forwarded = forwarded?.isForwarded)
                forwarded = null
            } else {
                sendMessage(otherUserId, message = filteredMessage, repliedTo = repliedTo)
            }
        }
    }

//    fun sendMessage(
//        otherUserId: String,
//        message: String,
//        repliedTo: MessageData? = null,
//        forwarded: Boolean? = false,
//    ) {
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                // Create the message data without a timestamp for now
//                val messageData = MessageData(
//                    message = message,
//                    senderID = userData?.userId.toString(),
//                    time = FieldValue.serverTimestamp(), // Placeholder for server timestamp
//                    repliedTo = repliedTo,
//                    isForwarded = forwarded
//                )
//
//                // Add message to sender's conversation and retrieve the timestamp after the write
//                val senderDocumentRef = firebase.collection("conversations")
//                    .document(userData?.userId.toString())
//                    .collection(otherUserId)
//                    .add(messageData)
//                    .await()
//
//                // Get the actual server-generated timestamp
//                val senderMessageSnapshot = senderDocumentRef.get().await()
//                val timestamp = senderMessageSnapshot.getTimestamp("time")
//
//                timestamp?.let {
//                    // Create a new MessageData object with the correct timestamp
//                    val messageDataWithTimestamp = messageData.copy(time = timestamp)
//
//                    // Add message to recipient's conversation with the same timestamp
//                    firebase.collection("conversations")
//                        .document(otherUserId)
//                        .collection(userData?.userId.toString())
//                        .add(messageDataWithTimestamp)
//                        .await()
//
//                    // Update latest messages for sender and recipient
//                    val latestMessageSenderRef = firebase.collection("latest_messages")
//                        .document(userData?.userId.toString() + "_" + otherUserId)
//                    latestMessageSenderRef.set(messageDataWithTimestamp)
//
//                    val latestMessageRecipientRef = firebase.collection("latest_messages")
//                        .document(otherUserId + "_" + userData?.userId.toString())
//                    latestMessageRecipientRef.set(messageDataWithTimestamp)
//
//                    // Send notification
//                    sendNotif(message)
//                }
//            } catch (e: Exception) {
//                Log.e("FirebaseViewModel", "Error sending message: ${e.message}")
//            }
//        }
//    }
    fun sendMessage(
        otherUserId: String,
        message: String,
        repliedTo: MessageData? = null,
        forwarded: Boolean? = false
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Create message data
                val messageData = MessageData(
                    message = message,
                    senderID = userData?.userId.toString(),
                    time = FieldValue.serverTimestamp(),
                    repliedTo = repliedTo,
                    isForwarded = forwarded
                )

                // Send the message (same as before)
                val senderDocumentRef = firebase.collection("conversations")
                    .document(userData?.userId.toString())
                    .collection(otherUserId)
                    .add(messageData).await()

                // Fetch and update timestamp
                val senderMessageSnapshot = senderDocumentRef.get().await()
                val timestamp = senderMessageSnapshot.getTimestamp("time")

                timestamp?.let {
                    // Create a new MessageData object with the correct timestamp
                    val messageDataWithTimestamp = messageData.copy(time = timestamp)

                    // Add message to recipient's conversation with the same timestamp
                    firebase.collection("conversations")
                        .document(otherUserId)
                        .collection(userData?.userId.toString())
                        .add(messageDataWithTimestamp)
                        .await()

                    // Update latest messages for sender and recipient
                    val latestMessageSenderRef = firebase.collection("latest_messages")
                        .document(userData?.userId.toString() + "_" + otherUserId)
                    latestMessageSenderRef.set(messageDataWithTimestamp)

                    val latestMessageRecipientRef = firebase.collection("latest_messages")
                        .document(otherUserId + "_" + userData?.userId.toString())
                    latestMessageRecipientRef.set(messageDataWithTimestamp)

                    // Send notification
                    sendNotif(message)

                    // Now handle emojis in the message
                    val emojis = extractEmojisFromText(message)
                    if (emojis.isNotEmpty()) {
                        updateEmojiCountAndEmotion(emojis)
                    }
                }
            } catch (e: Exception) {
                Log.e("FirebaseViewModel", "Error sending message: ${e.message}")
            }
        }
    }

    fun extractEmojisFromText(text: String): List<String> {
        return allEmojis.filter { text.contains(it) }
    }

    suspend fun updateEmojiCountAndEmotion(newEmojis: List<String>, oldEmojis: List<String> = emptyList()) {
        val userId = userData?.userId ?: return
        val userDocRef = firebase.collection("users").document(userId)

        // Fetch the current emojiCountMap from Firestore
        val userSnapshot = userDocRef.get().await()
        val emojiCountMap = userSnapshot.get("emojiCountMap") as? MutableMap<String, Int> ?: mutableMapOf()

        Log.d("FirebaseViewModel", "Emojis received: $newEmojis , $oldEmojis")

        // Update the emoji counts
        for (emoji in oldEmojis) {
            emojiCountMap[emoji] = emojiCountMap.getOrDefault(emoji, 1) - 1
        }

        for (emoji in newEmojis) {
            emojiCountMap[emoji] = emojiCountMap.getOrDefault(emoji, 0) + 1
        }

        Log.d("FirebaseViewModel", "Updated emojiCountMap: $emojiCountMap")

        // Save the updated emojiCountMap to Firestore
        userDocRef.update("emojiCountMap", emojiCountMap).await()

        // Find the most used emoji
        val mostUsedEmoji = emojiCountMap.maxByOrNull { it.value }?.key ?: return

        // Update the currentEmotion if it's different
        val currentEmotion = userSnapshot.getString("currentEmotion")
        if (currentEmotion != mostUsedEmoji) {
            userDocRef.update("currentEmotion", mostUsedEmoji).await()
        }
    }


    //    fun getMessagesWithUser() {
//        try {
//            if (isLoadingChat) {
//                return
//            }
//            isLoadingChat = true
//            Log.d("FirebaseViewModel", "Started loading messages with user: $chattingWith")
//            viewModelScope.launch(Dispatchers.IO) {
//                val messages =
//                    firebase.collection("conversations").document(userData?.userId.toString())
//                        .collection(chattingWith?.userId.toString())
//                        .orderBy("time", Query.Direction.DESCENDING)
//                        .get()
//                        .await()
//                _chatMessages.value = messages.toObjects(MessageData::class.java)
//                if (userData?.userPref?.readRecipients == true) {
//                    updateReadStatus()
//                }
//                isLoadingChat = false
//            }
//        }catch (e: Exception){
//            Log.d("FirebaseViewModel", "Error getting messages with user: ${e.message}")
//        }
//    }
fun getMessagesWithUser() {
    try {
        if (isLoadingChat) {
            return
        }
        isLoadingChat = true
        Log.d("FirebaseViewModel", "Started loading messages with user: $chattingWith")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val messagesSnapshot = firebase.collection("conversations")
                    .document(userData?.userId.toString())
                    .collection(chattingWith?.userId.toString())
                    .orderBy("time", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val messages = messagesSnapshot.documents.mapNotNull { document ->
                    val messageData = document.toObject(MessageData::class.java)
                    val timestamp = document.get("time") as? Timestamp
                    val timeMillis = timestamp?.let { it.seconds * 1000L + it.nanoseconds / 1000000L } ?: 0L
                    messageData?.copy(time = timeMillis)
                }

                withContext(Dispatchers.Main) {
                    _chatMessages.value = messages
                    if (userData?.userPref?.readRecipients == true) {
                        updateReadStatus()
                    }
                    isLoadingChat = false
                }
            } catch (e: Exception) {
                Log.d("FirebaseViewModel", "Error getting messages with user: ${e.message}")
                withContext(Dispatchers.Main) {
                    isLoadingChat = false
                }
            }
        }
    } catch (e: Exception) {
        Log.d("FirebaseViewModel", "Error in getMessagesWithUser: ${e.message}")
    }
}

    fun updateReadStatus() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentUserRef = firebase.collection("conversations")
                .document( chattingWith?.userId.toString())
                .collection(userData?.userId.toString())

            val messagesQuery = currentUserRef
                .whereEqualTo("read", false)
                .get()
                .await()

            for (document in messagesQuery.documents) {
                val messageId = document.id
                val message = document.toObject(MessageData::class.java)
                message?.let {
                    it.read = true
                    currentUserRef.document(messageId).set(it, SetOptions.merge())
                }
            }
        }
    }


    fun startMessageListener() {
        viewModelScope.launch (Dispatchers.IO){
            conversationsListener = firebase.collection("conversations")
                .document(userData?.userId.toString())
                .collection(chattingWith?.userId.toString())
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        return@addSnapshotListener
                    }
                    getMessagesWithUser()
                }
        }
    }

    fun stopConversationsListener() {
        conversationsListener?.remove()
    }

    fun addBio() {
        viewModelScope.launch(Dispatchers.IO) {
            val userDocumentRef = firebase.collection("users").document(userData?.userId.toString())
            userDocumentRef.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val currentUser = documentSnapshot.toObject(UserData::class.java)
                    currentUser?.bio = Bio
                    userData?.bio
                    userDocumentRef.set(currentUser!!)
                }
            }
        }
    }

    fun deleteFriend(friendUserId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _chatListUsers.value = _chatListUsers.value.filter { it.userId != friendUserId }
            firebase.collection("users").document(userData?.userId.toString())
                .update("chatList", FieldValue.arrayRemove(friendUserId))
                .await()
            firebase.collection("users").document(friendUserId)
                .update("chatList", FieldValue.arrayRemove(userData?.userId.toString()))
                .await()
            loadChatListUsers()
            firebase.collection("conversations").document(userData?.userId.toString())
                .collection(friendUserId)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        document.reference.delete()
                    }
                }
                .await()
            firebase.collection("conversations").document(friendUserId)
                .collection(userData?.userId.toString())
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        document.reference.delete()
                    }
                }
                .await()

            firebase.collection("latest_messages").document(userData?.userId.toString() + "_" + friendUserId)
                .delete()
                .await()
            firebase.collection("latest_messages").document(friendUserId + "_" + userData?.userId.toString())
                .delete()
                .await()
        }
    }

//    fun deleteMessage(otherUserId: String, messageData: MessageData) {
//        Log.d("FirebaseViewModel", "Deleting message: $messageData")
//        viewModelScope.launch(Dispatchers.IO) {
//            val senderMessageRef = firebase.collection("conversations")
//                .document(userData?.userId.toString())
//                .collection(otherUserId)
//                .whereEqualTo("message", messageData.message)
//                .whereEqualTo("time", messageData.time)
//                .get()
//                .await()
//
//            val receiverMessageRef = firebase.collection("conversations")
//                .document(otherUserId)
//                .collection(userData?.userId.toString())
//                .whereEqualTo("message", messageData.message)
//                .whereEqualTo("time", messageData.time)
//                .get()
//                .await()
//
//            for (document in senderMessageRef.documents) {
//                document.reference.delete()
//            }
//            for (document in receiverMessageRef.documents) {
//                document.reference.delete()
//            }
//
//            val latestMessageSenderRef = firebase.collection("conversations")
//                .document(userData?.userId.toString())
//                .collection(otherUserId)
//                .orderBy("time", Query.Direction.DESCENDING)
//                .limit(1)
//                .get()
//                .await()
//
//            val latestMessageRecipientRef = firebase.collection("conversations")
//                .document(otherUserId)
//                .collection(userData?.userId.toString())
//                .orderBy("time", Query.Direction.DESCENDING)
//                .limit(1)
//                .get()
//                .await()
//
//            val latestMessageSender = latestMessageSenderRef.documents.firstOrNull()?.toObject(MessageData::class.java)
//            latestMessageSender?.let {
//                firebase.collection("latest_messages")
//                    .document(userData?.userId.toString() + "_" + otherUserId)
//                    .set(it)
//            }
//
//            val latestMessageRecipient = latestMessageRecipientRef.documents.firstOrNull()?.toObject(MessageData::class.java)
//            latestMessageRecipient?.let {
//                firebase.collection("latest_messages")
//                    .document(otherUserId + "_" + userData?.userId.toString())
//                    .set(it)
//            }
//        }
//    }

    fun deleteMessage(otherUserId: String, messageData: MessageData) {
        Log.d("FirebaseViewModel", "Deleting message: ${messageData.time!!::class.java.simpleName}")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val taskViewModel=TaskViewModel()
                // Convert messageData.time from Long to Timestamp to match Firestore's data type
                val messageTime = taskViewModel.getTimeStamp(messageData.time)
                Log.d("FirebaseViewModel", "Message time: ${messageTime::class.java.simpleName}")
                // Query the messages for both sender and receiver to find the message to delete
                val senderMessageRef = firebase.collection("conversations")
                    .document(userData?.userId.toString())
                    .collection(otherUserId)
                    .whereEqualTo("message", messageData.message)
                    .whereEqualTo("time", messageTime)
                    .get()
                    .await()

                val receiverMessageRef = firebase.collection("conversations")
                    .document(otherUserId)
                    .collection(userData?.userId.toString())
                    .whereEqualTo("message", messageData.message)
                    .whereEqualTo("time", messageTime)
                    .get()
                    .await()

                // Delete messages for both sender and receiver
                for (document in senderMessageRef.documents) {
                    document.reference.delete()
                }
                for (document in receiverMessageRef.documents) {
                    document.reference.delete()
                }

                // Update the latest messages after deletion
                val latestMessageSenderRef = firebase.collection("conversations")
                    .document(userData?.userId.toString())
                    .collection(otherUserId)
                    .orderBy("time", Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .await()

                val latestMessageRecipientRef = firebase.collection("conversations")
                    .document(otherUserId)
                    .collection(userData?.userId.toString())
                    .orderBy("time", Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .await()

                val latestMessageSender = latestMessageSenderRef.documents.firstOrNull()?.toObject(MessageData::class.java)
                latestMessageSender?.let {
                    firebase.collection("latest_messages")
                        .document(userData?.userId.toString() + "_" + otherUserId)
                        .set(it)
                } ?: run {
                    // If no message is left, clear the latest message
                    firebase.collection("latest_messages")
                        .document(userData?.userId.toString() + "_" + otherUserId)
                        .delete()
                }

                val latestMessageRecipient = latestMessageRecipientRef.documents.firstOrNull()?.toObject(MessageData::class.java)
                latestMessageRecipient?.let {
                    firebase.collection("latest_messages")
                        .document(otherUserId + "_" + userData?.userId.toString())
                        .set(it)
                } ?: run {
                    // If no message is left, clear the latest message
                    firebase.collection("latest_messages")
                        .document(otherUserId + "_" + userData?.userId.toString())
                        .delete()
                }

            } catch (e: Exception) {
                Log.e("FirebaseViewModel", "Error deleting message: ${e.message}")
            }
        }
    }

    fun filterContacts(
        contactList : List<UserData>,
        toSearch : String
    ){
        viewModelScope.launch {
            val filteredList = contactList.filter {
                it.username!!.contains(toSearch,true) || it.mail!!.contains(toSearch, true)
            }
            _searchContacts.emit(filteredList)
        }
    }


    // FCM

    fun getToken(){
        viewModelScope.launch(Dispatchers.IO) {
            FirebaseMessaging.getInstance().token.addOnSuccessListener {
                val token = it.toString()
                userData?.token = token
                val userDocumentRef = firebase.collection("users").document(userData?.userId.toString())
                userDocumentRef.update("token", token)
            }
        }
    }

    fun removeToken() {
        viewModelScope.launch (Dispatchers.IO){
            userData?.let { user ->
                val userDocumentRef = firebase.collection("users").document(user.userId.toString())
                userDocumentRef.update("token", FieldValue.delete())
            }
        }
    }


    private fun sendNotif(message: String){
        viewModelScope.launch(Dispatchers.IO) {
            val jsonObject = JSONObject()
            val notificationObject = JSONObject()
            val dataObject = JSONObject()
            notificationObject.put("title",userData?.username.toString())
            notificationObject.put("body",message)
            dataObject.put("userId",userData?.userId.toString())
            jsonObject.put("notification", notificationObject)
            jsonObject.put("data", dataObject)
            jsonObject.put("to", chattingWith?.token)
            callNotifAPI(jsonObject)
        }
    }

    fun editMessage(otherUserId: String, messageTimestamp: Any, newMessage: String, context: Context, reaction: String? = null, starred: Boolean? = false) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Filter the new message for bad words
                val filteredMessage = filterBadWords(newMessage, context)

                // Convert messageTimestamp to Firestore Timestamp if it's not already
                val taskViewModel = TaskViewModel()
                val firestoreTimestamp = when (messageTimestamp) {
                    is Long -> taskViewModel.getTimeStamp(messageTimestamp)
                    is Timestamp -> messageTimestamp
                    else -> null
                }

                firestoreTimestamp?.let { timestamp ->
                    // Handle emojis in the edited message
                    val oldMessage = getMessageByTimestamp(otherUserId, timestamp)

                    // Current user message reference
                    val currentUserRef = firebase.collection("conversations")
                        .document(userData?.userId.toString())
                        .collection(otherUserId)

                    // Recipient message reference
                    val recipientUserRef = firebase.collection("conversations")
                        .document(otherUserId)
                        .collection(userData?.userId.toString())

                    // Query for the current user's message
                    val curUserQuerySnapshot = currentUserRef
                        .whereEqualTo("time", timestamp)
                        .get()
                        .await()

                    // Query for the recipient's message
                    val otherUserQuerySnapshot = recipientUserRef
                        .whereEqualTo("time", timestamp)
                        .get()
                        .await()

                    // Update current user's message
                    for (document in curUserQuerySnapshot.documents) {
                        val messageId = document.id
                        currentUserRef.document(messageId).update("message", filteredMessage)
                        currentUserRef.document(messageId).update("curUserReaction", reaction)
                        currentUserRef.document(messageId).update("starred", starred)
                        Log.d("FirebaseViewModel", "Updated sender's message with ID: $messageId")
                    }

                    // Update recipient's message
                    if (otherUserQuerySnapshot.isEmpty) {
                        Log.e("FirebaseViewModel", "No matching document for recipient with timestamp: $timestamp")
                    } else {
                        for (document in otherUserQuerySnapshot.documents) {
                            val messageId = document.id
                            recipientUserRef.document(messageId).update("message", filteredMessage)
                            recipientUserRef.document(messageId).update("otherUserReaction", reaction)
                            recipientUserRef.document(messageId).update("starred", starred)
                            Log.d("FirebaseViewModel", "Updated recipient's message with ID: $messageId")
                        }
                    }

                    // Update the latest message for the sender
                    val latestMessageSenderRef = firebase.collection("latest_messages")
                        .document(userData?.userId.toString() + "_" + otherUserId)

                    val latestMessageSenderQuery = currentUserRef
                        .orderBy("time", Query.Direction.DESCENDING)
                        .limit(1)
                        .get()
                        .await()

                    val latestMessageSender = latestMessageSenderQuery.documents.firstOrNull()?.toObject(MessageData::class.java)
                    latestMessageSender?.let {
                        latestMessageSenderRef.set(it)
                    }

                    // Update the latest message for the recipient
                    val latestMessageRecipientRef = firebase.collection("latest_messages")
                        .document(otherUserId + "_" + userData?.userId.toString())

                    val latestMessageRecipientQuery = recipientUserRef
                        .orderBy("time", Query.Direction.DESCENDING)
                        .limit(1)
                        .get()
                        .await()

                    val latestMessageRecipient = latestMessageRecipientQuery.documents.firstOrNull()?.toObject(MessageData::class.java)
                    latestMessageRecipient?.let {
                        latestMessageRecipientRef.set(it)
                    }

                    if(oldMessage!!.senderID==userData?.userId) {
                        // Handle emojis
                        // Extract emojis from the old message
                        val oldEmojis = extractEmojisFromText(oldMessage.message.orEmpty()) + (oldMessage.curUserReaction
                            ?: "")

                        // For the current user's message content
                        val currentUserMessageEmojis = extractEmojisFromText(filteredMessage)
                        // For the recipient's message, only count curUserReaction for reactions
                        val recipientReactionEmojis = reaction?.let { listOf(it) } ?: emptyList()

                        // Update emoji count and emotion based on the current user's message emojis and reaction
                        val newEmojis = currentUserMessageEmojis + recipientReactionEmojis
                        Log.d("FirebaseViewModel", "Before updating emoji in if condition: $newEmojis , $oldEmojis")
                        // Call function to update the emoji count and emotion
                        updateEmojiCountAndEmotion(newEmojis.filter { it.isNotEmpty() }, oldEmojis.filter { it.isNotEmpty() })
                        Log.d("FirebaseViewModel", "Updated emoji in if condition: $newEmojis , $oldEmojis")
                    }
                    else{
                        val oldEmoji=oldMessage.curUserReaction?.let { listOf(it) } ?: emptyList()
                        // For the recipient's message, only count curUserReaction for reactions
                        val recipientReactionEmojis = reaction?.let { listOf(it) } ?: emptyList()
                        val newEmojis = recipientReactionEmojis
                        Log.d("FirebaseViewModel", "Before updating emoji in else condition: $newEmojis , $oldEmoji")
                        updateEmojiCountAndEmotion(newEmojis.filter { it.isNotEmpty() },oldEmoji.filter { it.isNotEmpty()})
                        Log.d("FirebaseViewModel", "Updated emoji in else condition: $newEmojis , $oldEmoji")
                    }
                } ?: run {
                    Log.e("FirebaseViewModel", "Invalid timestamp format")
                }

            } catch (e: Exception) {
                Log.e("FirebaseViewModel", "Error editing message: ${e.message}")
            }
        }
    }

    suspend fun getMessageByTimestamp(otherUserId: String, timestamp: Timestamp): MessageData? {
        return try {
            val userId = userData?.userId ?: return null

            // Reference to the sender's conversation collection
            val conversationRef = firebase.collection("conversations")
                .document(userId)
                .collection(otherUserId)

            // Query the message with the matching timestamp
            val querySnapshot = conversationRef
                .whereEqualTo("time", timestamp)
                .limit(1) // Since timestamps should be unique, limit to 1 result
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                // Convert the first document into MessageData object
                querySnapshot.documents[0].toObject(MessageData::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("FirebaseViewModel", "Error retrieving message by timestamp: ${e.message}")
            null
        }
    }

    fun updateProfilePic() {
        viewModelScope.launch{
            if (mediaUri != null && userData?.userId!!.isNotEmpty()) {
                val storageRef = Firebase.storage.reference.child("profilePics/${userData?.userId}/${UUID.randomUUID()}")
                val allProfilePics = Firebase.storage.reference.child("profilePics/${userData?.userId}")
                allProfilePics.listAll()
                    .addOnSuccessListener { listResult ->
                        listResult.items.forEach { item ->
                            item.delete()
                        }
                    }
                val uploadTask = storageRef.putFile(mediaUri!!)
                uploadTask.continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    storageRef.downloadUrl
                }.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result
                        val userDocumentRef = firebase.collection("users").document(userData?.userId.toString())
                        userDocumentRef.update("profilePictureUrl", downloadUri.toString())
                        profilePicture = downloadUri.toString()
                    }
                }
            }
            mediaUri = null
        }

    }

    fun blockUser(userIdToBlock: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _blockedUsers.value += _chatListUsers.value.first { it.userId == userIdToBlock }
            _chatListUsers.value = _chatListUsers.value.filter { it.userId != userIdToBlock }
            val currentUserDoc = firebase.collection("users").document(userData?.userId.toString())
            currentUserDoc.update("blocked", FieldValue.arrayUnion(userIdToBlock))
        }
    }

    fun unblockUser(userIdToUnblock: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentUserDoc = firebase.collection("users").document(userData?.userId.toString())
            currentUserDoc.update("blocked", FieldValue.arrayRemove(userIdToUnblock))
                .addOnSuccessListener {
                    _blockedUsers.value = _blockedUsers.value.filter { it.userId != userIdToUnblock }
                }
        }
    }

    fun sendFriendRequest(userIdToSendFriendRequest: String){
        viewModelScope.launch(Dispatchers.IO) {
            val currentUserDoc = firebase.collection("users").document(userData?.userId.toString())
            currentUserDoc.update("friendRequestSent", FieldValue.arrayUnion(userIdToSendFriendRequest))
                .addOnSuccessListener {
                    val otherUserDoc = firebase.collection("users").document(userIdToSendFriendRequest)
                    otherUserDoc.update("friendRequestReceived", FieldValue.arrayUnion(userData?.userId.toString()))
//                    fetchAllFriendRequests()
                }
        }
    }

    fun cancelFriendRequest(userIdToCancelFriendRequest: String){
        viewModelScope.launch(Dispatchers.IO) {
            val otherUserDoc = firebase.collection("users").document(userIdToCancelFriendRequest)
            otherUserDoc.update("friendRequestReceived", FieldValue.arrayRemove(userData?.userId.toString()))
                .addOnSuccessListener {
                    val currentUserDoc = firebase.collection("users").document(userData?.userId.toString())
                    currentUserDoc.update("friendRequestSent", FieldValue.arrayRemove(userIdToCancelFriendRequest))
//                    fetchAllFriendRequests()
                }
        }
    }

    fun rejectFriendRequest(userIdToRejectFriendRequest: String){
        viewModelScope.launch(Dispatchers.IO) {
            val otherUserDoc = firebase.collection("users").document(userIdToRejectFriendRequest)
            otherUserDoc.update("friendRequestSent", FieldValue.arrayRemove(userData?.userId.toString()))
                .addOnSuccessListener {
                    val currentUserDoc = firebase.collection("users").document(userData?.userId.toString())
                    currentUserDoc.update("friendRequestReceived", FieldValue.arrayRemove(userIdToRejectFriendRequest))
//                    fetchAllFriendRequests()
                }
        }
    }

    fun acceptFriendRequest(userIdToAcceptFriendRequest: String){
        viewModelScope.launch(Dispatchers.IO) {
            val currentUserDoc = firebase.collection("users").document(userData?.userId.toString())
            currentUserDoc.update("chatList", FieldValue.arrayUnion(userIdToAcceptFriendRequest))
            val otherUserDoc = firebase.collection("users").document(userIdToAcceptFriendRequest)
            otherUserDoc.update("chatList", FieldValue.arrayUnion(userData?.userId.toString()))
                .addOnSuccessListener {
                    currentUserDoc.update("friendRequestReceived", FieldValue.arrayRemove(userIdToAcceptFriendRequest))
                        .addOnSuccessListener {
                            otherUserDoc.update("friendRequestSent", FieldValue.arrayRemove(userData?.userId.toString()))
//                            fetchAllFriendRequests()
                        }
                }
        }
    }

    fun fetchAllFriendRequests(){
        fetchSentFriendRequests()
        fetchReceivedFriendRequests()
    }

    fun fetchSentFriendRequests() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentUserDoc = firebase.collection("users").document(userData?.userId.toString())
                currentUserDoc.get().addOnSuccessListener { documentSnapshot ->
                    val sentRequestIds = documentSnapshot["friendRequestSent"] as? List<String> ?: emptyList()

                    if (sentRequestIds.isNotEmpty()) {
                        // Fetch user details of sent requests
                        firebase.collection("users")
                            .whereIn("userId", sentRequestIds)
                            .get()
                            .addOnSuccessListener { querySnapshot ->
                                val users = querySnapshot.toObjects(UserData::class.java)
                                _sentRequests.value = users // Update the StateFlow with the retrieved users
                            }
                            .addOnFailureListener { e ->
                                Log.e("FirebaseViewModel", "Error fetching users for sent requests: ${e.message}")
                            }
                    } else {
                        // Handle case where there are no sent requests
                        _sentRequests.value = emptyList() // Return an empty list
                    }
                }
            } catch (e: Exception) {
                Log.e("FirebaseViewModel", "Error fetching sent requests: ${e.message}")
            }
        }
    }

    fun fetchReceivedFriendRequests() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentUserDoc = firebase.collection("users").document(userData?.userId.toString())
                currentUserDoc.get().addOnSuccessListener { documentSnapshot ->
                    val receivedRequestIds = documentSnapshot["friendRequestReceived"] as? List<String> ?: emptyList()

                    if (receivedRequestIds.isNotEmpty()) {
                        // Fetch user details of received requests
                        firebase.collection("users")
                            .whereIn("userId", receivedRequestIds)
                            .get()
                            .addOnSuccessListener { querySnapshot ->
                                val users = querySnapshot.toObjects(UserData::class.java)
                                _receivedRequests.value = users // Update the StateFlow with the retrieved users
                            }
                            .addOnFailureListener { e ->
                                Log.e("FirebaseViewModel", "Error fetching users for received requests: ${e.message}")
                            }
                    } else {
                        // Handle case where there are no received requests
                        _receivedRequests.value = emptyList() // Return an empty list
                    }
                }
            } catch (e: Exception) {
                Log.e("FirebaseViewModel", "Error fetching received requests: ${e.message}")
            }
        }
    }

    fun startRequestListener() {
        viewModelScope.launch (Dispatchers.IO){
            requestListener = firebase.collection("users")
                .document(userData?.userId.toString())
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        return@addSnapshotListener
                    }
                    fetchAllFriendRequests()
                }
        }
    }

    fun stopRequestListener() {
        requestListener?.remove()
    }

    // Real-time search function
    fun searchUserByEmail(emailPrefix: String, onResult: (List<UserData>?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                firebase.collection("users")
                    .orderBy("mail")
                    .startAt(emailPrefix)
                    .endAt(emailPrefix + "\uf8ff")  // "\uf8ff" is the highest possible Unicode character
                    .limit(10)  // Optional: Limit the number of results
                    .get()
                    .addOnSuccessListener { documents ->
                        val users = documents.toObjects(UserData::class.java)
                        onResult(users)  // Return the list of found users
                    }
                    .addOnFailureListener {
                        onResult(null)  // Handle any errors
                    }
            } catch (e: Exception) {
                Log.e("FirebaseViewModel", "Error searching for users: ${e.message}")
                onResult(null)  // Handle exceptions
            }
        }
    }

    fun updateOnlineStatus(status: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val userDocumentRef = firebase.collection("users").document(userData?.userId.toString())
            userDocumentRef.update("online", status)
        }
    }

    fun updateTypingStatus(typing :Boolean? = true) {
        viewModelScope.launch (Dispatchers.IO){
            val userDocumentRef = firebase.collection("users").document(userData?.userId.toString())
            if(typing==false){
                userDocumentRef.update("typing", "")
            }
            else if(text.isBlank()){
                userDocumentRef.update("typing", "")
            }else{
                userDocumentRef.update("typing", chattingWith?.userId)
            }
        }
    }

    fun updateEmojiPref(emoji: String){
        viewModelScope.launch(Dispatchers.IO) {
            userData?.let { user ->
                if (user.userPref == null) {
                    user.userPref = UserPref()
                }
                user.userPref?.let { userPref ->
                    if (userPref.recentEmojis == null) {
                        userPref.recentEmojis = mutableListOf()
                    }
                    while (userPref.recentEmojis!!.size >= 6) {
                        userPref.recentEmojis!!.removeAt(0)
                    }
                    userPref.recentEmojis!!.add(emoji)
                }
                val userDocumentRef = firebase.collection("users").document(user.userId.toString())
                userDocumentRef.update("userPref", user.userPref).await()
            }
        }
    }

    fun findRepliedToIndex(time: Any){
        viewModelScope.launch (Dispatchers.Default){
            val ind = chatMessages.value.indexOfFirst { it.time == time }
            if(ind!=-1){
                _repliedToIndex.value = ind
                delay(1000)
                _repliedToIndex.value = null
            }
        }
    }

    fun updateChatPreferences(){
        viewModelScope.launch (Dispatchers.IO) {
            val userDocumentRef = firebase.collection("users").document(userData?.userId.toString())
            userDocumentRef.update("userPref", userData?.userPref).await()
        }
    }

    fun updateSearchMessageList(list: List<MessageData>, message: String){
        viewModelScope.launch (Dispatchers.Default){
            val messageList = chatMessages.value.filter {
                it.message!!.contains(message,true)
            }
            _searchMessages.emit(messageList)
            if(messageList.isNotEmpty()){
                searchListIndex = searchMessages.value.size-1
            }
        }
    }

    fun updateSearchMessageIndex(){
        viewModelScope.launch (Dispatchers.Default){
            if(searchMessages.value.isNotEmpty()){
                searchIndex = chatMessages.value.indexOfFirst { it.time == searchListIndex?.let { it1 ->
                    searchMessages.value[it1].time
                } }
                if(searchIndex == -1){
                    searchIndex = null
                }
            }
        }
    }
}