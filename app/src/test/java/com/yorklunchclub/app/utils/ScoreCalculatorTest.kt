package com.yorklunchclub.app.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ScoreCalculatorTest {

    @Test
    fun tc01_scoreAverageCalculation_singleVisitWithMultipleItems() {
        val scores = listOf(3, 4, 5)
        assertEquals(4.0, ScoreCalculator.visitAverage(scores), 0.0001)
    }

    @Test
    fun tc02_scoreAverageCalculation_multipleVisitsRestaurantAverage() {
        val visitAverages = listOf(3.4, 4.0, 2.6)
        assertEquals(
            3.3333,
            ScoreCalculator.restaurantAverage(visitAverages),
            0.0001
        )
    }

    @Test
    fun tc03_scoreMinimumBoundary_lowestAllowedValueAccepted() {
        assertEquals(1.0, ScoreCalculator.visitAverage(listOf(1)), 0.0001)
        assertEquals(1.0, ScoreCalculator.visitAverage(listOf(1, 1, 1)), 0.0001)
    }

    @Test
    fun tc04_scoreMaximumBoundary_highestAllowedValueAccepted() {
        assertEquals(5.0, ScoreCalculator.visitAverage(listOf(5)), 0.0001)
        assertEquals(5.0, ScoreCalculator.visitAverage(listOf(5, 5, 5)), 0.0001)
    }

    @Test
    fun tc05_scoreOutOfRange_belowMinimumThrows() {
        assertThrows(IllegalArgumentException::class.java) {
            ScoreCalculator.visitAverage(listOf(0))
        }
    }

    @Test
    fun tc05_scoreOutOfRange_aboveMaximumThrows() {
        assertThrows(IllegalArgumentException::class.java) {
            ScoreCalculator.visitAverage(listOf(6))
        }
    }

    @Test
    fun tc06_displayScore_nullMeansNoVisits_returnsNA() {
        assertEquals("N/A", ScoreCalculator.displayScore(null))
    }

    @Test
    fun tc06_displayScore_emptyVisitList_returnsNA() {
        assertEquals("N/A", ScoreCalculator.restaurantDisplayAverage(emptyList()))
    }

    @Test
    fun tc06_displayScore_validValue_formattedOneDecimal() {
        assertEquals("3.7", ScoreCalculator.displayScore(3.6666))
    }
}
