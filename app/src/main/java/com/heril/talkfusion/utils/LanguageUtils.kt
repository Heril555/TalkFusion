package com.heril.talkfusion.utils

object LanguageUtils {

    // Function to format language codes correctly
    fun formatLanguageCode(languageCode: String): String {
        return languageCode.lowercase()
    }

    // Function to get a list of supported languages
    fun getSupportedLanguages(): List<String> {
        return listOf(
            "af", "ar", "be", "bg", "bn", "ca", "cs", "cy", "da",
            "de", "el", "en", "eo", "es", "et", "fa", "fi", "fr", "ga", "gl",
            "gu", "he", "hi", "hr", "ht", "hu", "id", "is", "it",
            "ja", "ka", "kn", "ko", "lt", "lv",
            "mk", "mr", "ms", "mt", "nl", "no",
            "pl", "pt", "ro", "ru", "sk", "sl", "sq",
            "sv", "sw", "ta", "te", "th", "tl", "tr", "uk", "ur", "vi", "zh"
        )
    }

    // Function to map language codes to their display names
    fun getLanguageDisplayName(languageCode: String): String {
        val parts = languageCode.split("-")
        val locale = if (parts.size > 1) {
            java.util.Locale(parts[0], parts[1].uppercase())
        } else {
            java.util.Locale(languageCode)
        }
        return locale.displayName
    }
}