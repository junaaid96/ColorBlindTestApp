package io.github.junaaid96.colorvision.tests

import io.github.junaaid96.colorvision.color.ColorMath
import io.github.junaaid96.colorvision.color.CvdType
import io.github.junaaid96.colorvision.color.Rgb
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/** What a plate is designed to reveal. */
enum class PlateKind {
    /** Readable by everyone; failing it means the test conditions are unreliable. */
    CONTROL,
    /** Figure vanishes for protan and deutan viewers. */
    RED_GREEN,
    /** Figure vanishes for tritan viewers. */
    TRITAN,
    /** Two digits: the left one vanishes for protans, the right one for deutans. */
    DIAGNOSTIC,
}

data class Dot(val x: Float, val y: Float, val r: Float, val argb: Int)

data class Plate(val id: Int, val kind: PlateKind, val answer: String, val difficulty: Int, val dots: List<Dot>)

/**
 * Procedurally generates pseudo-isochromatic plates. Figure colors are searched so that
 * they sit on a confusion line with the background for the target deficiency (they collapse
 * after simulation) while staying clearly different for typical color vision.
 */
object PlateGenerator {

    private data class Candidate(val rgb: Rgb, val score: Double)

    private val candidates: List<Rgb> by lazy {
        buildList {
            for (h in 0 until 360 step 6) for (s in listOf(0.45, 0.6, 0.75)) for (l in listOf(0.42, 0.5, 0.58))
                add(Rgb.hsl(h.toDouble(), s, l))
        }
    }

    /** Distance between two colors as seen by [type] (null = typical vision). */
    fun seenDistance(a: Rgb, b: Rgb, type: CvdType?): Double =
        if (type == null) ColorMath.deltaE(a, b)
        else ColorMath.deltaE(ColorMath.simulate(a, type), ColorMath.simulate(b, type))

    /**
     * Picks a figure color for [background] that is hidden for every type in [hiddenFor] and
     * visible for every type in [visibleFor]. [difficulty] 0..2 lowers the normal-vision contrast.
     */
    fun figureColor(background: Rgb, hiddenFor: List<CvdType>, visibleFor: List<CvdType>, difficulty: Int): Rgb {
        val targetNormal = when (difficulty) { 0 -> 45.0; 1 -> 32.0; else -> 22.0 }
        return candidates.map { c ->
            val normal = seenDistance(c, background, null)
            val hidden = hiddenFor.sumOf { seenDistance(c, background, it) }
            val visible = visibleFor.minOfOrNull { seenDistance(c, background, it) } ?: normal
            val lum = kotlin.math.abs(ColorMath.luminance(c) - ColorMath.luminance(background)) * 200
            Candidate(c, hidden * 3 + lum + kotlin.math.abs(normal - targetNormal) - visible * 0.8)
        }.minBy { it.score }.rgb
    }

    private fun jitter(c: Rgb, rnd: Random, amount: Double = 0.035): Rgb =
        Rgb(c.r + rnd.nextDouble(-amount, amount), c.g + rnd.nextDouble(-amount, amount), c.b + rnd.nextDouble(-amount, amount)).clamp()

    /** Packs non-overlapping dots inside the unit circle, deterministically for [seed]. */
    fun packDots(seed: Int, count: Int = 900): List<Triple<Float, Float, Float>> {
        val rnd = Random(seed)
        val placed = ArrayList<Triple<Float, Float, Float>>()
        var attempts = 0
        while (placed.size < count && attempts < count * 30) {
            attempts++
            val r = rnd.nextDouble(0.008, 0.024).toFloat()
            val ang = rnd.nextDouble(0.0, Math.PI * 2)
            val rad = sqrt(rnd.nextDouble()) * (0.48 - r)
            val x = (0.5 + rad * cos(ang)).toFloat(); val y = (0.5 + rad * sin(ang)).toFloat()
            if (placed.none { (px, py, pr) -> val dx = px - x; val dy = py - y; dx * dx + dy * dy < (pr + r + 0.002f).let { it * it } })
                placed += Triple(x, y, r)
        }
        return placed
    }

    fun generate(id: Int, kind: PlateKind, answer: String, difficulty: Int, seed: Int = id * 7919): Plate {
        val rnd = Random(seed)
        val (background, figures) = when (kind) {
            PlateKind.CONTROL -> Rgb(0.55, 0.6, 0.62) to listOf(Rgb(0.92, 0.5, 0.15))
            PlateKind.RED_GREEN -> {
                val bg = Rgb.hsl(95.0 + rnd.nextInt(-10, 10), 0.45, 0.5)
                bg to listOf(figureColor(bg, listOf(CvdType.PROTAN, CvdType.DEUTAN), emptyList(), difficulty))
            }
            PlateKind.TRITAN -> {
                val bg = Rgb.hsl(60.0 + rnd.nextInt(-8, 8), 0.5, 0.55)
                bg to listOf(figureColor(bg, listOf(CvdType.TRITAN), listOf(CvdType.PROTAN, CvdType.DEUTAN), difficulty))
            }
            PlateKind.DIAGNOSTIC -> {
                val bg = Rgb(0.55, 0.55, 0.55)
                bg to listOf(
                    figureColor(bg, listOf(CvdType.PROTAN), listOf(CvdType.DEUTAN), 0),
                    figureColor(bg, listOf(CvdType.DEUTAN), listOf(CvdType.PROTAN), 0),
                )
            }
        }
        val dots = packDots(seed).map { (x, y, r) ->
            val idx = DigitFont.hit(answer, x.toDouble(), y.toDouble())
            val base = if (idx < 0) background else figures[idx.coerceAtMost(figures.lastIndex)]
            // Random lightness noise defeats pure-luminance cues.
            val lift = rnd.nextDouble(-0.06, 0.06)
            val c = jitter(Rgb(base.r + lift, base.g + lift, base.b + lift).clamp(), rnd)
            Dot(x, y, r, c.toArgb())
        }
        return Plate(id, kind, answer, difficulty, dots)
    }

    /** Plate plan for a quick (about 1 minute) or full screening. */
    fun plan(full: Boolean, seed: Int = 1): List<Plate> {
        val rnd = Random(seed)
        fun num(two: Boolean) = if (two) "${rnd.nextInt(1, 10)}${rnd.nextInt(0, 10)}" else "${rnd.nextInt(2, 10)}"
        val spec = buildList {
            add(PlateKind.CONTROL to 0)
            val rg = if (full) 9 else 4
            repeat(rg) { add(PlateKind.RED_GREEN to it * 3 / rg) }
            repeat(if (full) 3 else 1) { add(PlateKind.TRITAN to it) }
            repeat(if (full) 2 else 1) { add(PlateKind.DIAGNOSTIC to 0) }
            if (full) add(PlateKind.CONTROL to 0)
        }
        return spec.mapIndexed { i, (kind, diff) ->
            val answer = when (kind) {
                PlateKind.DIAGNOSTIC -> "${rnd.nextInt(1, 10)}${rnd.nextInt(1, 10)}"
                else -> num(rnd.nextBoolean())
            }
            generate(i + 1, kind, answer, diff, seed * 1000 + i)
        }
    }
}
