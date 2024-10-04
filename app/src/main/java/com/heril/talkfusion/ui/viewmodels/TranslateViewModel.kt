package com.heril.talkfusion.ui.viewmodels

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heril.talkfusion.data.LanguageRepository
import com.heril.talkfusion.data.MessageData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TranslateViewModel() : ViewModel() {

    private val repository = LanguageRepository()

    var inputText by mutableStateOf("")
    var translatedText by mutableStateOf("")
    var selectedLanguage by mutableStateOf("en")
    var isLoading by mutableStateOf(false)
    var isTranslating by mutableStateOf<Boolean?>(null)
    var errorMessage by mutableStateOf("")

    var translatedChatList by mutableStateOf<List<MessageData>>(emptyList())

    fun onTextChanged(newText: String) {
        inputText = newText
    }

    fun onLanguageChanged(languageCode: String) {
        selectedLanguage = languageCode
    }

//    fun translateChatMessages(chatMessagesState: State<List<MessageData>>) {
//        isLoading = true
//        val chatMessages = chatMessagesState.value
//
//        // Create a mutable list to hold the translated messages
//        val translatedMessages = mutableListOf<MessageData>()
//
//        for (message in chatMessages) {
//            val messageText = message.message ?: "" // Safe call with default value
//            if (messageText.isNotEmpty()) {
//                inputText = messageText
//                if(translate()){
//                    val translatedMessage = message.copy(message = translatedText)
//                    translatedMessages.add(translatedMessage)
//                }
//
//                // Wait until translation is complete
////                while (!isLoading) {
//                    // You may want to handle cancellation or timeout here
////                    delay(3000) // Adjust the delay as needed
//                    // Add the translated message to the list
////                    val translatedMessage = message.copy(message = translatedText)
////                    translatedMessages.add(translatedMessage)
////                }
//
//                // Add the translated message to the list
////                val translatedMessage = message.copy(message = translatedText)
////                translatedMessages.add(translatedMessage)
//            } else {
//                // Add the original message if messageText is empty or null
//                translatedMessages.add(message)
//            }
//        }
//
//        // Update the translatedChatList with the translated messages
//        translatedChatList = translatedMessages
//        isLoading = false
//    }
suspend fun translateChatMessages(chatMessagesState: State<List<MessageData>>) {
    Log.d("TranslateViewModel", "BeforeList: $translatedChatList")
    isTranslating = true
    val chatMessages = chatMessagesState.value

    val translatedMessages = mutableListOf<MessageData>()

    for (message in chatMessages) {
        val messageText = message.message ?: "" // Safe call with default value
        if (messageText.isNotEmpty()) {
            inputText = messageText
            translate() // Directly call the suspend function

            // Add the translated message to the list
            val translatedMessage = message.copy(message = translatedText)
            translatedMessages.add(translatedMessage)
        } else {
            translatedMessages.add(message)
        }
    }

    translatedChatList = translatedMessages
    isTranslating = false
    Log.d("TranslateViewModel", "After List: $translatedChatList")
}

    suspend fun translate() {
        if (inputText.isNotBlank()) {
            try {
                isLoading = true
                errorMessage = ""
                translatedText = repository.translateText(inputText, selectedLanguage)
                Log.d("TranslateViewModel", "Translated text: $translatedText")
            } catch (e: Exception) {
                errorMessage = "Error: ${e.localizedMessage}"
                Log.e("TranslateViewModel", "Translation failed", e)
            } finally {
                isLoading = false
            }
        }
    }
}