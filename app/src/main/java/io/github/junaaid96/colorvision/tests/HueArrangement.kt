package io.github.junaaid96.colorvision.tests

import io.github.junaaid96.colorvision.color.ColorMath
import io.github.junaaid96.colorvision.color.CvdType
import io.github.junaaid96.colorvision.color.Lab
import io.github.junaaid96.colorvision.color.Rgb
import io.github.junaaid96.colorvision.color.toRgb
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class ArrangementResult(
    /** Angle (degrees, -90..90) of the major error axis. */
    val angle: Double,
    /** Confusion index: 1 for a perfect order, above about 1.6 indicates a deficiency. */
    val cIndex: Double,
    /** Selectivity index: how polarised (single-axis) the errors are. */
    val sIndex: Double,
    val crossings: Int,
    val likelyAxis: CvdType?,
    val passed: Boolean,
)

/**
 * D-15 style cap arrangement. Caps are drawn on a hue circle of constant lightness and
 * chroma; scoring follows the Vingrys & King-Smith (1988) moment-of-inertia method applied in
 * CIE Lab, with confusion axes derived from the same simulation model the app uses.
 */
object HueArrangement {
    const val CAPS = 15

    /** Pilot cap at index 0 followed by [CAPS] movable caps, in the correct order. */
    fun caps(lightness: Double = 58.0, chroma: Double = 28.0): List<Rgb> = (0..CAPS).map { i ->
        val h = Math.toRadians(250.0 - i * 20.0)
        Lab(lightness, chroma * cos(h), chroma * sin(h)).toRgb().clamp()
    }

    private fun ab(c: Rgb) = ColorMath.toLab(c).let { it.a to it.b }

    /** Confusion axis angle (mod 180) for a deficiency in this cap set. */
    fun axisAngle(type: CvdType): Double {
        var sxx = 0.0; var syy = 0.0; var sxy = 0.0
        caps().forEach { c ->
            val (a1, b1) = ab(c); val (a2, b2) = ab(ColorMath.simulate(c, type))
            val dx = a1 - a2; val dy = b1 - b2
            sxx += dx * dx; syy += dy * dy; sxy += dx * dy
        }
        return majorAngle(sxx, syy, sxy)
    }

    private fun majorAngle(sxx: Double, syy: Double, sxy: Double): Double {
        var deg = Math.toDegrees(0.5 * atan2(2 * sxy, sxx - syy))
        if (deg > 90) deg -= 180; if (deg < -90) deg += 180
        return deg
    }

    private fun moments(order: List<Int>): Triple<Double, Double, Double> {
        val pts = caps().map { ab(it) }
        val seq = listOf(0) + order
        var sxx = 0.0; var syy = 0.0; var sxy = 0.0
        for (i in 1 until seq.size) {
            val (x1, y1) = pts[seq[i - 1]]; val (x2, y2) = pts[seq[i]]
            val dx = x2 - x1; val dy = y2 - y1
            sxx += dx * dx; syy += dy * dy; sxy += dx * dy
        }
        return Triple(sxx, syy, sxy)
    }

    private fun radii(m: Triple<Double, Double, Double>): Pair<Double, Double> {
        val (sxx, syy, sxy) = m
        val tr = sxx + syy
        val det = sqrt(((sxx - syy) / 2).let { it * it } + sxy * sxy)
        val n = CAPS.toDouble()
        return sqrt((tr / 2 + det) / n) to sqrt(maxOf(1e-9, tr / 2 - det) / n)
    }

    /** [order] lists cap indexes 1..[CAPS] in the order the person placed them. */
    fun score(order: List<Int>): ArrangementResult {
        require(order.sorted() == (1..CAPS).toList()) { "order must be a permutation of 1..$CAPS" }
        val m = moments(order)
        val (major, minor) = radii(m)
        val (refMajor, _) = radii(moments((1..CAPS).toList()))
        val angle = majorAngle(m.first, m.second, m.third)
        val crossings = (1 until order.size).count { kotlin.math.abs(order[it] - order[it - 1]) > 2 } +
            if (kotlin.math.abs(order[0]) > 2) 1 else 0
        val cIndex = major / refMajor
        val passed = cIndex < 1.6 && crossings <= 1
        val axis = if (passed) null else CvdType.entries.minBy { t ->
            val d = kotlin.math.abs(angle - axisAngle(t)) % 180
            minOf(d, 180 - d)
        }
        return ArrangementResult(angle, cIndex, major / minor, crossings, axis, passed)
    }
}
