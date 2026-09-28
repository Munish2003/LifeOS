package com.personal.lifeos.data.remote

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object DirectAiClient {
    private const val PREFS_NAME = "lifeos_ai_prefs"
    private const val KEY_HF_TOKEN = "hf_token"
    private const val KEY_SERVER_URL = "server_url"

    // Stored securely in app SharedPreferences
    private const val DEFAULT_HF_KEY = ""
    private const val DEFAULT_MODEL = "Qwen/Qwen2.5-7B-Instruct"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    fun getHfKey(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_HF_TOKEN, DEFAULT_HF_KEY) ?: DEFAULT_HF_KEY
    }

    fun saveHfKey(context: Context, key: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_HF_TOKEN, key.trim()).apply()
    }

    fun getServerUrl(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_SERVER_URL, "http://192.168.1.106:8000/") ?: "http://192.168.1.106:8000/"
    }

    fun saveServerUrl(context: Context, url: String) {
        val cleanUrl = if (url.endsWith("/")) url else "$url/"
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SERVER_URL, cleanUrl.trim()).apply()
        ApiClient.baseUrl = cleanUrl.trim()
    }

    /**
     * Calls Hugging Face directly via HTTPS from the phone!
     * Works on Wi-Fi, 4G, 5G anywhere without requiring local PC to be running.
     */
    suspend fun queryHuggingFaceDirect(context: Context, userPrompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = getHfKey(context)
        if (apiKey.isBlank()) {
            return@withContext "Please add your Hugging Face API key in Settings to enable direct cloud AI."
        }

        try {
            val url = "https://api-inference.huggingface.co/models/$DEFAULT_MODEL"
            val promptPayload = JSONObject().apply {
                put("inputs", "<|im_start|>system\nYou are Personal Life OS, an intelligent personal productivity, fitness, diet and routine planning AI companion. Answer concisely, empathetically, and practically in friendly Hinglish or English.<|im_end|>\n<|im_start|>user\n$userPrompt<|im_end|>\n<|im_start|>assistant\n")
                put("parameters", JSONObject().apply {
                    put("max_new_tokens", 250)
                    put("temperature", 0.7)
                    put("return_full_text", false)
                })
            }

            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(promptPayload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val jsonArray = JSONArray(responseBody)
                if (jsonArray.length() > 0) {
                    val gen = jsonArray.getJSONObject(0).optString("generated_text", "")
                    if (gen.isNotBlank()) {
                        return@withContext gen.trim()
                    }
                }
            } else if (response.code == 503) {
                // Model is loading in cloud
                return@withContext "AI Model load ho raha hai cloud me (cold start). Kripya 20-30 second baad dubara send karein."
            }

            // Fallback to secondary endpoint if first model busy
            return@withContext fallbackQuery(apiKey, userPrompt)
        } catch (e: Exception) {
            return@withContext "AI Response: " + generateSmartLocalReply(userPrompt)
        }
    }

    private fun fallbackQuery(apiKey: String, prompt: String): String {
        return try {
            val url = "https://api-inference.huggingface.co/models/meta-llama/Meta-Llama-3-8B-Instruct"
            val json = JSONObject().apply {
                put("inputs", "You are a personal life OS planner. Answer in short:\n$prompt")
                put("parameters", JSONObject().apply { put("max_new_tokens", 180) })
            }
            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $apiKey")
                .post(json.toString().toRequestBody("application/json".toMediaType()))
                .build()
            val res = httpClient.newCall(request).execute()
            if (res.isSuccessful) {
                val arr = JSONArray(res.body?.string() ?: "")
                if (arr.length() > 0) arr.getJSONObject(0).optString("generated_text", "").trim()
                else generateSmartLocalReply(prompt)
            } else {
                generateSmartLocalReply(prompt)
            }
        } catch (e: Exception) {
            generateSmartLocalReply(prompt)
        }
    }

    fun generateSmartLocalReply(prompt: String): String {
        val p = prompt.lowercase()
        return when {
            "python" in p || "study" in p || "padh" in p ->
                "Focus mode activate karein! Aaj ka target achieve karne ke liye 45-60 minute ka dedicated deep-work timer start karein."
            "dsa" in p || "code" in p ->
                "DSA practice ke liye roz kam se kam 2-3 standard problems solve karein. Consistency hi master banayegi!"
            "step" in p || "walk" in p || "chal" in p ->
                "Daily 10,000 steps target se aapka cardiovascular health aur energy boost hota hai. Shaam ko 30 minute brisk walk karein!"
            "food" in p || "diet" in p || "calorie" in p || "roti" in p ->
                "Balanced nutrition ke liye high protein (dal, paneer, eggs) aur controlled carbs maintain karein. Target 1200 kcal budget me rahein."
            "what should i do" in p || "kya karu" in p ->
                "Aapke schedule aur remaining available time ke mutabiq, abhi aapko apne top priority learning task par focus karna chahiye!"
            else ->
                "Aapka target record ho gaya hai. Aap mujhe natural bhasha me food log, study sessions ya daily planning ke liye bol sakte hain!"
        }
    }
}
