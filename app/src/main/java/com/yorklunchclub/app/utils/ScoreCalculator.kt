package com.yorklunchclub.app.utils

import java.util.Locale

object ScoreCalculator {

    fun validateScore(score: Int) {
        require(score in 1..5) { "Score must be between 1 and 5, got $score" }
    }

    fun visitAverage(scores: List<Int>): Double {
        scores.forEach { validateScore(it) }
        return if (scores.isEmpty()) 0.0 else scores.map { it.toDouble() }.average()
    }

    fun restaurantAverage(visitAverages: List<Double>): Double =
        if (visitAverages.isEmpty()) 0.0 else visitAverages.average()

    fun displayScore(average: Double?): String =
        if (average == null) "N/A" else String.format(Locale.UK, "%.1f", average)

    fun restaurantDisplayAverage(visitAverages: List<Double>): String =
        if (visitAverages.isEmpty()) "N/A"
        else displayScore(restaurantAverage(visitAverages))
}
