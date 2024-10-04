package com.heril.talkfusion.ui.viewmodels

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.heril.talkfusion.data.MessageData
import com.heril.talkfusion.data.MessageGroup
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TaskViewModel : ViewModel(){

    var selected by mutableIntStateOf(0)
    var isSignedIn by mutableStateOf(false)
    var expandedPersonInfo by mutableStateOf(false)
    var morePersonChatOptions by mutableStateOf(false)
    var showNavBar by mutableStateOf(false)
    var showDialog by mutableStateOf(false)
    var showDeleteMsgDialog by mutableStateOf(false)
    var showImageDialog by mutableStateOf(false)
    var showSetProfilePictureDialog by mutableStateOf(false)
    var chatOptions by mutableStateOf(false)
    var isEditing by mutableStateOf(false)
    var allEmojis by mutableStateOf(false)
    var isForwarding by mutableStateOf(false)
    var searchMessages by mutableStateOf(false)

//    fun getTime(mills: Long): String {
//        val calendar = Calendar.getInstance()
//        calendar.timeInMillis = mills
//
//        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
//        return dateFormat.format(calendar.time)
//    }
fun getTime(time: Any?): String {
    val calendar = Calendar.getInstance()

    // Handle different types of time inputs
    val timeMillis: Long = when (time) {
        is Long -> time
        is Timestamp -> time.seconds * 1000 + time.nanoseconds / 1000000
        is String -> time.toLongOrNull() ?: 0L
        else -> 0L
    }

    calendar.timeInMillis = timeMillis

//    val dateFormat = SimpleDateFormat("MMM d, yyyy 'at' hh:mm a", Locale.getDefault())
    val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return dateFormat.format(calendar.time)
}
    fun getTimeStamp(time: Any?): Timestamp {
        return when (time) {
            is Long -> Timestamp(Date(time))  // Convert Long to Date, then to Timestamp
            is Timestamp -> time  // Already a Timestamp, return as is
            is String -> {
                val timeMillis = time.toLongOrNull() ?: 0L
                Timestamp(Date(timeMillis))
            }
            else -> Timestamp(Date(0L))  // Default case, use epoch time
        }
    }

    fun formatDate(time: Any?): String {
        val calendar = Calendar.getInstance()

        // Handle different types of time inputs
        val timeMillis: Long = when (time) {
            is Long -> time
            is Timestamp -> time.seconds * 1000 + time.nanoseconds / 1000000
            is String -> time.toLongOrNull() ?: 0L
            else -> 0L
        }

        calendar.timeInMillis = timeMillis

        val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }
    fun groupMessagesByDate(messages: List<MessageData>): List<MessageGroup> {
        val groupedMessages = mutableMapOf<String, MutableList<MessageData>>()
        messages.forEach { message ->
            val date = formatDate(message.time) // Format the date from timestamp
            if (date !in groupedMessages) {
                groupedMessages[date] = mutableListOf()
            }
            groupedMessages[date]?.add(message)
        }
        return groupedMessages.map { (date, messages) ->
            MessageGroup(date, messages)
        }
    }

    fun copyToClipboard(context: Context, text: String) {
        viewModelScope.launch {
            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("text", text)
            clipboardManager.setPrimaryClip(clipData)
        }
    }
}