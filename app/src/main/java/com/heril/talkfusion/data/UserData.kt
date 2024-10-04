package com.heril.talkfusion.data

data class UserData(
    val userId: String? = "",
    val username: String? = "",
    val profilePictureUrl: String? = "",
    val mail: String? = "",
    val chatList: List<String>? = emptyList(),
//    val favorites: List<String>? = emptyList(),
    var blocked: List<String>? = emptyList(),
    val friendRequestSent: List<String>? = emptyList(),
    val friendRequestReceived: List<String>? = emptyList(),
    val notifications : List<Map<String,Any>>? = emptyList(),
    var bio: String? = "",
    var latestMessage: MessageData? = null,
    var token : String? = "",
//    var status: String? = "",
//    var statusExpiry: Long? = 0,
    var online: Boolean? = false,
    var typing: String? = "",
    var userPref: UserPref? = UserPref(),
    var currentEmotion: String? = "",
    var emojiCountMap: MutableMap<String, Int>? = mutableMapOf()
)