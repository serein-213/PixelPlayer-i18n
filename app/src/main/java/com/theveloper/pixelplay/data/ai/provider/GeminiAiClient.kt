package com.theveloper.pixelplay.data.ai.provider

import com.google.genai.Client
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Gemini AI provider implementation
 */
class GeminiAiClient(private val apiKey: String) : AiClient {
    
    companion object {
        private const val DEFAULT_GEMINI_MODEL = "gemini-2.5-flash"
    }
    
    private val client: Client by lazy {
        Client.builder().apiKey(apiKey).build()
    }
    
    override suspend fun generateContent(model: String, prompt: String): String {
        return withContext(Dispatchers.IO) {
            val response = client.models.generateContent(model, prompt, null)
            response.text() ?: throw Exception("Gemini returned an empty response")
        }
    }
    
    override suspend fun getAvailableModels(apiKey: String): List<String> {
        // Use HTTP API instead of SDK as the SDK doesn't expose model listing
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models?key=$apiKey"
                val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
                
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                
                val responseCode = connection.responseCode
                if (responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    parseModelsFromResponse(response)
                } else {
                    getDefaultModels()
                }
            } catch (e: Exception) {
                getDefaultModels()
            }
        }
    }
    
    override suspend fun validateApiKey(apiKey: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val tempClient = Client.builder().apiKey(apiKey).build()
                // Try a simple generation to validate the key
                val response = tempClient.models.generateContent(DEFAULT_GEMINI_MODEL, "test", null)
                response.text() != null
            } catch (e: Exception) {
                false
            }
        }
    }
    
    override fun getDefaultModel(): String = DEFAULT_GEMINI_MODEL
    
    private fun parseModelsFromResponse(jsonResponse: String): List<String> {
        try {
            val models = mutableListOf<String>()
            val modelPattern = """"name":\s*"(models/[^"]+)"""".toRegex()
            val matches = modelPattern.findAll(jsonResponse)
            
            for (match in matches) {
                val fullName = match.groupValues[1]
                val modelName = fullName.removePrefix("models/")
                
                // Filter for generative models
                if (modelName.startsWith("gemini", ignoreCase = true) &&
                    !modelName.contains("embedding", ignoreCase = true)) {
                    models.add(modelName)
                }
            }
            
            return if (models.isNotEmpty()) models else getDefaultModels()
        } catch (e: Exception) {
            return getDefaultModels()
        }
    }
    
    private fun getDefaultModels(): List<String> {
        return listOf(
            "gemini-2.5-flash",
            "gemini-2.5-pro",
            "gemini-1.5-flash",
            "gemini-1.5-pro"
        )
    }
}
