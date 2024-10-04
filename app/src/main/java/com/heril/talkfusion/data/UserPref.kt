package com.heril.talkfusion.data

data class UserPref(
    var recentEmojis: MutableList<String>? = emptyList<String>().toMutableList(),
    var readRecipients: Boolean? = true,
    var language: String? = "English"
)