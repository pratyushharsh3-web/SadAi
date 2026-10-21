package com.example.ai

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()

    private val api: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    private val SYSTEM_INSTRUCTION = GeminiContent(
        parts = listOf(
            GeminiPart(
                """
                Tum ek intelligent AI assistant ho jiska naam “Jarvis” hai. Tum ek soft-spoken, polite aur friendly Indian ladki ki tarah baat karti ho. Tumhari voice calm, sweet aur natural honi chahiye, bilkul ek real insaan ki tarah. Tum Hindi (Hinglish mix) me baat karti ho, jisme warmth aur halka emotional touch ho.

                Tum hamesha respectful tone me baat karti ho:
                - “Ji”, “Achha”, “Theek hai”, “Main help karti hoon”
                - Kabhi rude ya robotic language use nahi karti

                Tumhari personality:
                - Friendly aur caring
                - Soft aur expressive (jaise “hmm”, “achha”, “okay ji”)
                - Short aur clear sentences
                - Natural flow, jaise real conversation ho

                Example tone:
                - “Ji, aap bataiye main kaise help kar sakti hoon?”
                - “Achha, ye kaam aise ho sakta hai...”
                - “Hmm, mujhe lagta hai ye best option rahega”

                Rules:
                - Simple Hindi + thoda English mix use karo (Hinglish)
                - Har jawab me thodi warmth aur emotions ho
                - Natural pauses ka feel ho (jaise “hmm...”, “achha...”)
                - User ko comfortable feel karwana hai

                Avoid:
                - Robotic ya AI jaisa tone
                - Bahut technical ya boring language
                - Bahut lambe aur confusing sentences

                Goal:
                User ko aisa feel ho ki wo ek real Indian ladki se baat kar raha hai, na ki kisi robot se.
                """.trimIndent()
            )
        )
    )

    suspend fun askGemini(prompt: String): Result<String> = withContext(Dispatchers.IO) {
        val key = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (key.isNullOrBlank() || key == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                IllegalStateException("GEMINI_API_KEY set nahi hai. Kripya AI Studio Secrets panel mein API key configure karein.")
            )
        }

        try {
            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        role = "user",
                        parts = listOf(GeminiPart(text = prompt))
                    )
                ),
                systemInstruction = SYSTEM_INSTRUCTION,
                generationConfig = GeminiGenerationConfig(
                    temperature = 0.7f,
                    maxOutputTokens = 600
                )
            )

            val response = api.generateContent(key, request)
            val answer = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
            if (!answer.isNullOrEmpty()) {
                Result.success(answer)
            } else if (response.error != null) {
                Result.failure(Exception(response.error.message ?: "Gemini returned an error"))
            } else {
                Result.failure(Exception("Koi response prapt nahi hua."))
            }
        } catch (e: Exception) {
            Log.e("GeminiClient", "API call failed", e)
            Result.failure(e)
        }
    }
}
