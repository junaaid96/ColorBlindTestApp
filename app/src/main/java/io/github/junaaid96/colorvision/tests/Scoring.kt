package io.github.junaaid96.colorvision.tests

import kotlin.math.min

/** Overall screening outcome. Never a diagnosis. */
enum class Classification { TYPICAL, PROTAN, DEUTAN, RED_GREEN, TRITAN, INCONCLUSIVE }

enum class Severity { NONE, MILD, MODERATE, STRONG }

data class PlateOutcome(val plate: Int, val kind: PlateKind, val expected: String, val given: String, val correct: Boolean)

data class ScreeningResult(
    val classification: Classification,
    val severity: Severity,
    /** 0..1, how much the answers agree with each other and how many plates were seen. */
    val confidence: Double,
    /** 0..100 share of plates read correctly. */
    val score: Int,
    val outcomes: List<PlateOutcome>,
)

object PlateScoring {

    fun severityFor(errorRate: Double): Severity = when {
        errorRate < 0.25 -> Severity.NONE
        errorRate < 0.5 -> Severity.MILD
        errorRate < 0.8 -> Severity.MODERATE
        else -> Severity.STRONG
    }

    /** [answers] holds what the person typed for each plate ("" = saw nothing). */
    fun score(plates: List<Plate>, answers: List<String>): ScreeningResult {
        require(plates.size == answers.size) { "one answer per plate" }
        val outcomes = plates.zip(answers).map { (p, a) ->
            PlateOutcome(p.id, p.kind, p.answer, a.trim(), a.trim() == p.answer)
        }
        fun rate(kind: PlateKind): Double? {
            val list = outcomes.filter { it.kind == kind }
            return if (list.isEmpty()) null else list.count { !it.correct }.toDouble() / list.size
        }
        val controlFailed = outcomes.any { it.kind == PlateKind.CONTROL && !it.correct }
        val rg = rate(PlateKind.RED_GREEN) ?: 0.0
        val tr = rate(PlateKind.TRITAN) ?: 0.0

        // Diagnostic plates: protans miss the left digit, deutans miss the right one.
        var protanVotes = 0; var deutanVotes = 0
        outcomes.filter { it.kind == PlateKind.DIAGNOSTIC && !it.correct }.forEach {
            when (it.given) {
                it.expected.substring(1) -> protanVotes++
                it.expected.substring(0, 1) -> deutanVotes++
            }
        }

        val classification = when {
            controlFailed -> Classification.INCONCLUSIVE
            rg >= 0.25 && rg >= tr -> when {
                protanVotes > deutanVotes -> Classification.PROTAN
                deutanVotes > protanVotes -> Classification.DEUTAN
                else -> Classification.RED_GREEN
            }
            tr >= 0.34 -> Classification.TRITAN
            else -> Classification.TYPICAL
        }
        val severity = when (classification) {
            Classification.TYPICAL, Classification.INCONCLUSIVE -> Severity.NONE
            Classification.TRITAN -> severityFor(tr)
            else -> severityFor(rg)
        }
        val sizeFactor = min(1.0, outcomes.size / 15.0)
        val consistency = when (classification) {
            Classification.TYPICAL -> 1.0 - maxOf(rg, tr)
            Classification.INCONCLUSIVE -> 0.2
            Classification.PROTAN, Classification.DEUTAN -> 0.6 + 0.4 * rg
            Classification.RED_GREEN -> 0.4 + 0.4 * rg
            Classification.TRITAN -> 0.5 + 0.4 * tr
        }
        val confidence = (0.35 + 0.65 * sizeFactor) * consistency
        val score = if (outcomes.isEmpty()) 0 else outcomes.count { it.correct } * 100 / outcomes.size
        return ScreeningResult(classification, severity, confidence.coerceIn(0.0, 1.0), score, outcomes)
    }
}
