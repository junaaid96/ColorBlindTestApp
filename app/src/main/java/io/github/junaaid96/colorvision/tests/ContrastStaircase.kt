package io.github.junaaid96.colorvision.tests

import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log10

/**
 * 1-up/2-down adaptive staircase (converges near 71% correct) used by the contrast test.
 * Contrast is Weber contrast in 0.002..1.
 */
class ContrastStaircase(start: Double = 0.4, private val maxReversals: Int = 6, private val maxTrials: Int = 30) {
    var contrast = start
        private set
    private var streak = 0
    private var lastDirection = 0
    private val reversals = mutableListOf<Double>()
    var trials = 0
        private set

    val finished get() = reversals.size >= maxReversals || trials >= maxTrials

    fun answer(correct: Boolean) {
        if (finished) return
        trials++
        val direction = if (correct) { streak++; if (streak >= 2) { streak = 0; -1 } else 0 } else { streak = 0; 1 }
        if (direction != 0) {
            if (lastDirection != 0 && direction != lastDirection) reversals += contrast
            lastDirection = direction
            contrast = (if (direction < 0) contrast * 0.7 else contrast / 0.7).coerceIn(0.002, 1.0)
        }
    }

    /** Geometric mean of the last four reversals (or current level). */
    fun threshold(): Double {
        val last = reversals.takeLast(4).ifEmpty { listOf(contrast) }
        return exp(last.sumOf { ln(it) } / last.size)
    }

    /** Log contrast sensitivity; about 1.6-2.0 is typical on a phone screen. */
    fun logSensitivity(): Double = -log10(threshold())
}
