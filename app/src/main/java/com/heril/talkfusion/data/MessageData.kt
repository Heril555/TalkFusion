package com.heril.talkfusion.data

data class MessageData(
    val message : String? = "",
    val senderID : String? = "",
    val time : Any?=null,
    var curUserReaction : String? = "",
    var otherUserReaction : String? = "",
    var read: Boolean? = false,
    var repliedTo: MessageData? = null,
    var isForwarded: Boolean? = false
)