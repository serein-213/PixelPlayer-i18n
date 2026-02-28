package com.theveloper.pixelplay.data.ai.provider

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Factory for creating AI client instances based on provider type
 */
@Singleton
class AiClientFactory @Inject constructor() {
    
    /**
     * Create an AI client for the specified provider
     * @param provider The AI provider type
     * @param apiKey The API key for the provider
     * @return AiClient instance
     */
    fun createClient(provider: AiProvider, apiKey: String): AiClient {
        if (apiKey.isBlank()) {
            throw IllegalArgumentException("API Key cannot be blank for ${provider.displayName}")
        }
        
        return when (provider) {
            AiProvider.GEMINI -> GeminiAiClient(apiKey)
            AiProvider.DEEPSEEK -> DeepSeekAiClient(apiKey)
        }
    }
}
