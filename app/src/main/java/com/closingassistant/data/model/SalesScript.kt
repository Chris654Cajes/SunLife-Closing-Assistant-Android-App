package com.closingassistant.data.model

data class SalesStep(
    val stepNumber: Int,
    val title: String,
    val script: String,
    val tips: String
)

data class Recommendation(
    val planName: String,
    val planDescription: String,
    val estimatedPremium: String,
    val coverage: String,
    val talkingPoints: List<String>,
    val emotionalTriggers: List<EmotionalTrigger>,
    val closingScript: String
)

data class EmotionalTrigger(
    val emoji: String,
    val label: String,
    val description: String,
    val backgroundType: TriggerType
)

enum class TriggerType {
    FAMILY, SECURITY, FUTURE, LEGACY
}
