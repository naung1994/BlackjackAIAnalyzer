package com.ai.blackjackanalyzer

object ProbabilityCalculator {

    data class AnalysisResult(
        val playerTotal: Int,
        val dealerShowing: Int,
        val winProbability: Double,
        val recommendedAction: String,
        val cardCountingAdvice: String
    )

    fun analyzeHand(playerCards: List<Int>, dealerCard: Int, runningCount: Int): AnalysisResult {
        val playerSum = playerCards.sum()
        
        // ရိုးရှင်းသော Blackjack Basic Strategy နှင့် Probability တွက်ချက်မှု
        var winProb = 48.5
        val recommendation = when {
            playerSum < 12 -> {
                winProb = 42.0
                "HIT (ဖဲထပ်တောင်းပါ)"
            }
            playerSum in 12..16 -> {
                if (dealerCard >= 7) {
                    winProb = 39.0
                    "HIT (ဒီလာက ဖဲကောင်းနေသည်)"
                } else {
                    winProb = 52.0
                    "STAND (ရပ်ထားပါ - ဒီလာ ကျနိုင်ခြေရှိသည်)"
                }
            }
            playerSum >= 17 -> {
                winProb = 58.0
                "STAND (လုံလောက်ပါပြီ)"
            }
            else -> "DOUBLE DOWN (ဖဲနှစ်ဆမြှင့်ပါ)"
        }

        val countAdvice = if (runningCount > 2) {
            "High Cards များနေသည် (Player အသာစီးရနိုင်သည်)"
        } else {
            "Low Cards များနေသည် (သတိထားကစားပါ)"
        }

        return AnalysisResult(playerSum, dealerCard, winProb, recommendation, countAdvice)
    }
}
