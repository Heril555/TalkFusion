package com.heril.talkfusion.data

import android.util.Log
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.nl.translate.Translation
import com.heril.talkfusion.utils.LanguageUtils
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.tasks.await

class LanguageRepository {

    private val TAG = "LanguageRepository"

    // Function to detect the language of the input text
    suspend fun detectLanguage(text: String): String {
        val languageIdentifier = LanguageIdentification.getClient()
        val resultDeferred = CompletableDeferred<String>()

        languageIdentifier.identifyLanguage(text)
            .addOnSuccessListener { languageCode ->
                if (languageCode == "und") {
                    Log.i(TAG, "Can't identify language.")
                    resultDeferred.complete("en") // Default to "en" if language is undetermined
                } else {
                    Log.i(TAG, "Language: $languageCode")
                    resultDeferred.complete(languageCode)
                }
            }
            .addOnFailureListener { e ->
                // Handle the error here
                Log.e(TAG, "Language identification failed", e)
                resultDeferred.complete("en") // Default to "en" in case of error
            }

        return resultDeferred.await()
    }

    // Function to translate the input text to the target language
    suspend fun translateText(text: String, targetLanguage: String): String {
        // Ensure that the language codes are in the correct format
        val sourceLanguage = detectLanguage(text)
        val formattedSourceLanguage = LanguageUtils.formatLanguageCode(sourceLanguage)
        val formattedTargetLanguage = LanguageUtils.formatLanguageCode(targetLanguage)

        // If source and target languages are the same, return the original text
        if (formattedSourceLanguage == formattedTargetLanguage) {
            return text
        }

        val translatorOptions = TranslatorOptions.Builder()
            .setSourceLanguage(formattedSourceLanguage)
            .setTargetLanguage(formattedTargetLanguage)
            .build()

        val translator = Translation.getClient(translatorOptions)

        return try {
            translator.downloadModelIfNeeded().await()
            translator.translate(text).await()
        } catch (e: Exception) {
            Log.e(TAG, "Translation failed2", e)
            "Error: ${e.localizedMessage}"
        }finally {
            translator.close()
        }
    }
}
