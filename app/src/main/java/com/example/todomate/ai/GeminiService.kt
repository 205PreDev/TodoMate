package com.example.todomate.ai

import com.example.todomate.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class GeminiService {

    private val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            temperature = 0.7f
            topK = 40
            topP = 0.95f
            maxOutputTokens = 1024
        }
    )

    /**
     * 격려 메시지 생성
     */
    suspend fun generateEncouragement(
        weeklyStats: Map<String, Int>,      // 영역별 할 일 개수
        completedStats: Map<String, Int>,   // 영역별 완료 개수
        goalStats: Map<String, Int>         // 영역별 목표 비율
    ): EncouragementResult {
        return withContext(Dispatchers.IO) {
            try {
                val prompt = buildEncouragementPrompt(weeklyStats, completedStats, goalStats)
                val response = model.generateContent(prompt)
                val text = response.text ?: return@withContext EncouragementResult.Error("응답이 비어있습니다")

                parseEncouragementResponse(text)
            } catch (e: Exception) {
                EncouragementResult.Error(e.message ?: "AI 요청 실패")
            }
        }
    }

    /**
     * 할 일 목록 기반 격려 메시지 생성
     */
    suspend fun generateEncouragementForTodos(
        stats: Map<String, Int>
    ): EncouragementResult {
        return withContext(Dispatchers.IO) {
            try {
                val prompt = buildTodoEncouragementPrompt(stats)
                val response = model.generateContent(prompt)
                val text = response.text ?: return@withContext EncouragementResult.Error("응답이 비어있습니다")

                parseEncouragementResponse(text)
            } catch (e: Exception) {
                EncouragementResult.Error(e.message ?: "AI 요청 실패")
            }
        }
    }

    /**
     * 할 일 태그 제안
     */
    suspend fun suggestLifeArea(
        title: String,
        description: String,
        availableAreas: List<String>
    ): TagSuggestionResult {
        return withContext(Dispatchers.IO) {
            try {
                val prompt = buildTagSuggestionPrompt(title, description, availableAreas)
                val response = model.generateContent(prompt)
                val text = response.text ?: return@withContext TagSuggestionResult.Error("응답이 비어있습니다")

                parseTagSuggestionResponse(text, availableAreas)
            } catch (e: Exception) {
                TagSuggestionResult.Error(e.message ?: "AI 요청 실패")
            }
        }
    }

    private fun buildTodoEncouragementPrompt(stats: Map<String, Int>): String {
        val statsText = stats.entries.joinToString("\n") { "- ${it.key}: ${it.value}개" }

        return """
당신은 친절하고 따뜻한 동기부여 코치입니다.

[오늘의 할 일 현황]
$statsText

위 정보를 바탕으로 사용자에게 짧은 격려 메시지를 작성해주세요.

조건:
- 1-2문장으로 아주 간결하게
- 현재 상황에 맞는 격려
- 한국어로 작성
- 이모지 1개 사용
- 진심어린 따뜻한 톤으로
- 매번 다른 표현 사용

응답은 반드시 아래 JSON 형식으로만 작성하세요:
{"message": "메시지 내용"}
        """.trimIndent()
    }

    private fun buildEncouragementPrompt(
        weeklyStats: Map<String, Int>,
        completedStats: Map<String, Int>,
        goalStats: Map<String, Int>
    ): String {
        val statsText = buildString {
            appendLine("이번 주 현황:")
            weeklyStats.forEach { (area, total) ->
                val completed = completedStats[area] ?: 0
                val goal = goalStats[area] ?: 0
                appendLine("- $area: 총 ${total}개, 완료 ${completed}개, 목표 비율 ${goal}%")
            }
        }

        return """
당신은 친절하고 따뜻한 라이프 코치입니다.

$statsText

위 정보를 바탕으로 사용자에게 동기부여 메시지를 작성해주세요.

조건:
- 2-3문장으로 간결하게
- 잘하고 있는 점을 먼저 언급
- 개선이 필요한 점은 부드럽게 제안
- 한국어로 작성
- 이모지 1-2개 사용
- 너무 과장하지 않고 진심어린 톤으로

응답은 반드시 아래 JSON 형식으로만 작성하세요:
{"message": "메시지 내용"}
        """.trimIndent()
    }

    private fun buildTagSuggestionPrompt(
        title: String,
        description: String,
        availableAreas: List<String>
    ): String {
        return """
당신은 할 일 분류 전문가입니다.

[사용 가능한 생활 영역]
${availableAreas.joinToString(", ")}

[할 일 정보]
제목: $title
설명: ${description.ifEmpty { "(없음)" }}

위 할 일이 어떤 생활 영역에 해당하는지 분석해주세요.

조건:
- 반드시 위 목록에 있는 영역 중 하나를 선택
- 확실하지 않으면 "알 수 없음"이라고 답변
- 선택한 이유를 간단히 설명

응답은 반드시 아래 JSON 형식으로만 작성하세요:
{"areaName": "영역이름", "confidence": 0.8, "reason": "이유"}
        """.trimIndent()
    }

    private fun parseEncouragementResponse(text: String): EncouragementResult {
        return try {
            // JSON 부분만 추출
            val jsonStart = text.indexOf("{")
            val jsonEnd = text.lastIndexOf("}") + 1
            if (jsonStart == -1 || jsonEnd == 0) {
                return EncouragementResult.Success(text.trim())
            }

            val jsonStr = text.substring(jsonStart, jsonEnd)
            val json = JSONObject(jsonStr)
            val message = json.getString("message")
            EncouragementResult.Success(message)
        } catch (e: Exception) {
            // JSON 파싱 실패 시 원본 텍스트 반환
            EncouragementResult.Success(text.trim())
        }
    }

    private fun parseTagSuggestionResponse(text: String, availableAreas: List<String>): TagSuggestionResult {
        return try {
            val jsonStart = text.indexOf("{")
            val jsonEnd = text.lastIndexOf("}") + 1
            if (jsonStart == -1 || jsonEnd == 0) {
                return TagSuggestionResult.Error("JSON 파싱 실패")
            }

            val jsonStr = text.substring(jsonStart, jsonEnd)
            val json = JSONObject(jsonStr)

            val areaName = json.getString("areaName")
            val confidence = json.optDouble("confidence", 0.5)
            val reason = json.optString("reason", "")

            if (areaName == "알 수 없음" || !availableAreas.contains(areaName)) {
                TagSuggestionResult.NoSuggestion
            } else {
                TagSuggestionResult.Success(
                    areaName = areaName,
                    confidence = confidence.toFloat(),
                    reason = reason
                )
            }
        } catch (e: Exception) {
            TagSuggestionResult.Error("응답 파싱 실패: ${e.message}")
        }
    }
}

sealed class EncouragementResult {
    data class Success(val message: String) : EncouragementResult()
    data class Error(val message: String) : EncouragementResult()
}

sealed class TagSuggestionResult {
    data class Success(
        val areaName: String,
        val confidence: Float,
        val reason: String
    ) : TagSuggestionResult()
    object NoSuggestion : TagSuggestionResult()
    data class Error(val message: String) : TagSuggestionResult()
}
