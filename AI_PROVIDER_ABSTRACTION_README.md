# AI 提供商抽象层实现

## 概述
本次更新为 PixelPlayer 实现了一个 AI 提供商抽象层，允许用户在设置中切换不同的 AI 提供商（Google Gemini 和 DeepSeek）。

## 功能特性

### 1. AI 提供商抽象层
创建了统一的接口层，支持多个 AI 提供商：

#### 核心组件：
- **AiProvider** (`data/ai/provider/AiProvider.kt`) - 枚举类型定义可用的 AI 提供商
- **AiClient** (`data/ai/provider/AiClient.kt`) - 抽象接口定义 AI 客户端的通用操作
- **GeminiAiClient** (`data/ai/provider/GeminiAiClient.kt`) - Google Gemini 提供商实现
- **DeepSeekAiClient** (`data/ai/provider/DeepSeekAiClient.kt`) - DeepSeek 提供商实现
- **AiClientFactory** (`data/ai/provider/AiClientFactory.kt`) - 工厂类用于创建 AI 客户端实例

### 2. 统一的 API
所有 AI 提供商实现相同的接口：
```kotlin
interface AiClient {
    suspend fun generateContent(model: String, prompt: String): String
    suspend fun getAvailableModels(apiKey: String): List<String>
    suspend fun validateApiKey(apiKey: String): Boolean
    fun getDefaultModel(): String
}
```

### 3. 更新的 AI 功能组件
- **AiPlaylistGenerator** - 更新为使用抽象层而非直接调用 Gemini SDK
- **AiMetadataGenerator** - 更新为使用抽象层

### 4. 设置界面增强

#### 新增设置项：
1. **AI 提供商选择器**
   - 位置：设置 > AI 集成 > AI Provider
   - 选项：Google Gemini / DeepSeek

2. **动态 API 密钥输入**
   - 根据选择的提供商显示相应的 API 密钥输入框
   - Gemini API Key - 用于 Google Gemini
   - DeepSeek API Key - 用于 DeepSeek

3. **智能模型选择**
   - 根据当前选择的提供商加载对应的可用模型
   - 支持模型列表自动更新

4. **条件显示系统提示词**
   - 系统提示词设置仅在使用 Gemini 时显示
   - DeepSeek 使用标准 OpenAI 兼容 API

## 数据持久化

### UserPreferencesRepository 新增字段：
```kotlin
// AI Provider Settings
val AI_PROVIDER = stringPreferencesKey("ai_provider")
val DEEPSEEK_API_KEY = stringPreferencesKey("deepseek_api_key")
val DEEPSEEK_MODEL = stringPreferencesKey("deepseek_model")
```

### StateFlows：
- `aiProvider: StateFlow<String>` - 当前选择的 AI 提供商
- `deepseekApiKey: StateFlow<String>` - DeepSeek API 密钥
- `deepseekModel: StateFlow<String>` - 选择的 DeepSeek 模型

## ViewModel 更新

### SettingsViewModel 新增方法：
- `onAiProviderChange(provider: String)` - 切换 AI 提供商
- `onDeepseekApiKeyChange(apiKey: String)` - 更新 DeepSeek API 密钥
- `onDeepseekModelChange(model: String)` - 更新 DeepSeek 模型选择

## 国际化支持

### 英文字符串 (values/strings.xml)：
```xml
<string name="settings_ai_provider_title">AI Provider</string>
<string name="settings_ai_provider_subtitle">Choose between Google Gemini or DeepSeek.</string>
<string name="settings_deepseek_api_key_title">DeepSeek API Key</string>
<string name="settings_deepseek_api_key_subtitle">Required for DeepSeek AI features.</string>
```

### 中文字符串 (values-zh/strings.xml)：
```xml
<string name="settings_ai_provider_title">AI 提供商</string>
<string name="settings_ai_provider_subtitle">选择 Google Gemini 或 DeepSeek。</string>
<string name="settings_deepseek_api_key_title">DeepSeek API 密钥</string>
<string name="settings_deepseek_api_key_subtitle">DeepSeek AI 功能所需。</string>
```

## 技术实现细节

### Gemini Client
- 使用官方 Google Gemini SDK (com.google.genai)
- 支持流式生成和缓存
- API: `client.models.generateContent(model, prompt, null)`

### DeepSeek Client
- 使用 OpenAI 兼容 API
- 基于 OkHttp 实现 HTTP 请求
- 端点: `https://api.deepseek.com/v1/chat/completions`
- 支持 chat completion 格式

### 模型列表获取
- **Gemini**: 通过 HTTP GET 请求 Google 的 models API
- **DeepSeek**: 通过 HTTP GET 请求 `/v1/models` 端点
- 失败时返回默认模型列表

### 默认模型
- **Gemini**: `gemini-2.5-flash`
- **DeepSeek**: `deepseek-chat`

## 依赖项
项目已包含所需的所有依赖：
- ✅ Google Gemini SDK (`libs.google.genai`)
- ✅ OkHttp (`libs.okhttp`)
- ✅ Kotlinx Serialization

## 使用流程

1. **选择 AI 提供商**
   - 进入设置 > AI 集成
   - 在"AI Provider"中选择 Google Gemini 或 DeepSeek

2. **配置 API 密钥**
   - 输入对应提供商的 API 密钥
   - 系统会自动加载可用模型列表

3. **选择模型**（可选）
   - 从"AI Model"下拉列表中选择想要使用的模型
   - 如不选择，将使用默认模型

4. **使用 AI 功能**
   - AI 播放列表生成
   - AI 元数据补全
   - Daily Mix 智能推荐

所有现有的 AI 功能将自动使用所选的提供商。

## 优势对比

### Google Gemini
- ✅ 官方 SDK 支持
- ✅ 更好的上下文理解
- ✅ 支持自定义系统提示词
- ❌ API 调用成本较高

### DeepSeek
- ✅ 成本更低（通常比 Gemini 便宜 50-80%）
- ✅ OpenAI 兼容 API
- ✅ 快速响应
- ❌ 不支持图像生成
- ❌ 较少的自定义选项

## 文件变更清单

### 新增文件：
- `app/src/main/java/com/theveloper/pixelplay/data/ai/provider/AiProvider.kt`
- `app/src/main/java/com/theveloper/pixelplay/data/ai/provider/AiClient.kt`
- `app/src/main/java/com/theveloper/pixelplay/data/ai/provider/GeminiAiClient.kt`
- `app/src/main/java/com/theveloper/pixelplay/data/ai/provider/DeepSeekAiClient.kt`
- `app/src/main/java/com/theveloper/pixelplay/data/ai/provider/AiClientFactory.kt`

### 修改文件：
- `app/src/main/java/com/theveloper/pixelplay/data/preferences/UserPreferencesRepository.kt`
- `app/src/main/java/com/theveloper/pixelplay/data/ai/AiPlaylistGenerator.kt`
- `app/src/main/java/com/theveloper/pixelplay/data/ai/AiMetadataGenerator.kt`
- `app/src/main/java/com/theveloper/pixelplay/presentation/viewmodel/SettingsViewModel.kt`
- `app/src/main/java/com/theveloper/pixelplay/presentation/screens/SettingsCategoryScreen.kt`
- `app/src/main/res/values/strings.xml`
- `app/src/main/res/values-zh/strings.xml`

## 测试建议

1. **切换提供商测试**
   - 验证在 Gemini 和 DeepSeek 之间切换
   - 确认 API 密钥输入框正确显示

2. **模型列表测试**
   - 验证模型列表正确加载
   - 测试无效 API 密钥的错误处理

3. **AI 功能测试**
   - 使用 Gemini 生成播放列表
   - 使用 DeepSeek 生成播放列表
   - 验证元数据补全功能

4. **持久化测试**
   - 重启应用后验证设置保存
   - 验证提供商切换后保持选择

## 兼容性
- ✅ 向后兼容：现有用户默认使用 Gemini
- ✅ 数据迁移：无需迁移，新字段有合理默认值
- ✅ 编译通过：所有代码已验证编译成功

## 未来扩展
该架构支持轻松添加更多 AI 提供商：
- OpenAI GPT-4/GPT-3.5
- Anthropic Claude
- 本地 LLM（如 Ollama）
- 其他兼容 OpenAI API 的服务

只需：
1. 在 `AiProvider` 枚举中添加新提供商
2. 实现 `AiClient` 接口
3. 在 `AiClientFactory` 中添加创建逻辑
4. 添加相应的 UI 和字符串资源

## 注意事项
- DeepSeek 不支持图像生成功能
- 建议用户在 API 密钥中使用环境变量或安全存储
- 模型列表缓存在 UI 状态中，切换提供商时会刷新

## 版本信息
- 实现日期: 2026-02-28
- 架构版本: 1.0
- 支持的提供商: 2 (Gemini, DeepSeek)
