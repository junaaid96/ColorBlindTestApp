package io.github.junaaid96.colorvision.data

import io.github.junaaid96.colorvision.tests.Classification
import io.github.junaaid96.colorvision.tests.Severity

enum class TestKind { QUICK, FULL, ARRANGEMENT, CONTRAST, PRACTICE }

/** One saved result. Stored on-device only. */
data class HistoryEntry(
    val timestamp: Long,
    val kind: TestKind,
    val classification: Classification,
    val severity: Severity,
    /** 0..100 for plates/practice, C-index x100 for arrangement, log CS x100 for contrast. */
    val score: Int,
    val confidence: Int,
) {
    fun encode() = listOf(timestamp, kind, classification, severity, score, confidence).joinToString(",")

    companion object {
        fun decode(s: String): HistoryEntry? = runCatching {
            val p = s.split(",")
            HistoryEntry(p[0].toLong(), TestKind.valueOf(p[1]), Classification.valueOf(p[2]), Severity.valueOf(p[3]), p[4].toInt(), p[5].toInt())
        }.getOrNull()

        fun encodeAll(list: List<HistoryEntry>) = list.joinToString(";") { it.encode() }
        fun decodeAll(s: String?): List<HistoryEntry> =
            s.orEmpty().split(";").filter { it.isNotBlank() }.mapNotNull { decode(it) }
    }
}

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class Settings(
    val onboarded: Boolean = false,
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val highContrast: Boolean = false,
    val reduceMotion: Boolean = false,
    val speech: Boolean = false,
    val haptics: Boolean = true,
)

/** Streak bookkeeping, pure so it can be unit tested. Days are epoch days. */
data class Streak(val current: Int = 0, val best: Int = 0, val lastDay: Long = -1) {
    fun record(today: Long): Streak = when (lastDay) {
        today -> this
        today - 1 -> (current + 1).let { Streak(it, maxOf(best, it), today) }
        else -> Streak(1, maxOf(best, 1), today)
    }
    fun active(today: Long) = if (lastDay >= today - 1) current else 0
}
