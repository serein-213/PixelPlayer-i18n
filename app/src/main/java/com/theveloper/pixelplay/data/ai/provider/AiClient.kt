package com.theveloper.pixelplay.data.ai.provider

/**
 * Abstract interface for AI providers
 * Defines common operations for text generation and metadata completion
 */
interface AiClient {
    
    /**
     * Generate text content based on a prompt
     * @param model The model identifier to use
     * @param prompt The input prompt
     * @return Generated text response
     * @throws Exception if generation fails
     */
    suspend fun generateContent(model: String, prompt: String): String
    
    /**
     * Get list of available models for this provider
     * @param apiKey The API key to use
     * @return List of available model names
     */
    suspend fun getAvailableModels(apiKey: String): List<String>
    
    /**
     * Validate the API key
     * @param apiKey The API key to validate
     * @return true if valid, false otherwise
     */
    suspend fun validateApiKey(apiKey: String): Boolean
    
    /**
     * Get the default model for this provider
     * @return Default model identifier
     */
    fun getDefaultModel(): String
}
