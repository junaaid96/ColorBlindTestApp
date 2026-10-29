package io.github.junaaid96.colorvision.color

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cbrt
import kotlin.math.pow
import kotlin.math.sqrt

/** Kinds of color vision deficiency the app screens for and simulates. */
enum class CvdType { PROTAN, DEUTAN, TRITAN }

/** A color with channels in the 0..1 sRGB (gamma-encoded) range. */
data class Rgb(val r: Double, val g: Double, val b: Double) {
    fun clamp() = Rgb(r.coerceIn(0.0, 1.0), g.coerceIn(0.0, 1.0), b.coerceIn(0.0, 1.0))
    fun toArgb(): Int {
        val c = clamp()
        return (0xFF shl 24) or ((c.r * 255 + 0.5).toInt() shl 16) or
            ((c.g * 255 + 0.5).toInt() shl 8) or (c.b * 255 + 0.5).toInt()
    }
    fun hex(): String = "#%06X".format(toArgb() and 0xFFFFFF)

    companion object {
        fun ofArgb(argb: Int) = Rgb(
            ((argb shr 16) and 0xFF) / 255.0,
            ((argb shr 8) and 0xFF) / 255.0,
            (argb and 0xFF) / 255.0,
        )
        fun hsl(h: Double, s: Double, l: Double): Rgb {
            val c = (1 - abs(2 * l - 1)) * s
            val hp = ((h % 360) + 360) % 360 / 60.0
            val x = c * (1 - abs(hp % 2 - 1))
            val (r1, g1, b1) = when {
                hp < 1 -> Triple(c, x, 0.0)
                hp < 2 -> Triple(x, c, 0.0)
                hp < 3 -> Triple(0.0, c, x)
                hp < 4 -> Triple(0.0, x, c)
                hp < 5 -> Triple(x, 0.0, c)
                else -> Triple(c, 0.0, x)
            }
            val m = l - c / 2
            return Rgb(r1 + m, g1 + m, b1 + m)
        }
    }
}

data class Lab(val l: Double, val a: Double, val b: Double)

/**
 * Pure color science helpers. Simulation uses the Machado, Oliveira & Fernandes (2009)
 * matrices in linear sRGB; daltonization follows Fidaner, Lin & Ozguven (2005).
 */
object ColorMath {
    fun toLinear(c: Double): Double = if (c <= 0.04045) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
    fun toGamma(c: Double): Double {
        val v = c.coerceIn(0.0, 1.0)
        return if (v <= 0.0031308) v * 12.92 else 1.055 * v.pow(1 / 2.4) - 0.055
    }

    /** Relative luminance (WCAG). */
    fun luminance(c: Rgb): Double =
        0.2126 * toLinear(c.r) + 0.7152 * toLinear(c.g) + 0.0722 * toLinear(c.b)

    fun contrastRatio(a: Rgb, b: Rgb): Double {
        val la = luminance(a); val lb = luminance(b)
        return (maxOf(la, lb) + 0.05) / (minOf(la, lb) + 0.05)
    }

    private val identity = doubleArrayOf(1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0)

    // Machado 2009, severity 1.0 (dichromacy).
    private val protanopia = doubleArrayOf(
        0.152286, 1.052583, -0.204868,
        0.114503, 0.786281, 0.099216,
        -0.003882, -0.048116, 1.051998,
    )
    private val deuteranopia = doubleArrayOf(
        0.367322, 0.860646, -0.227968,
        0.280085, 0.672501, 0.047413,
        -0.011820, 0.042940, 0.968881,
    )
    private val tritanopia = doubleArrayOf(
        1.255528, -0.076749, -0.178779,
        -0.078411, 0.930809, 0.147602,
        0.004733, 0.691367, 0.303900,
    )

    /** 3x3 simulation matrix (linear RGB) for a deficiency at [severity] 0..1. */
    fun simulationMatrix(type: CvdType, severity: Double): DoubleArray {
        val full = when (type) {
            CvdType.PROTAN -> protanopia
            CvdType.DEUTAN -> deuteranopia
            CvdType.TRITAN -> tritanopia
        }
        val s = severity.coerceIn(0.0, 1.0)
        return DoubleArray(9) { identity[it] * (1 - s) + full[it] * s }
    }

    private fun apply(m: DoubleArray, r: Double, g: Double, b: Double) = Triple(
        m[0] * r + m[1] * g + m[2] * b,
        m[3] * r + m[4] * g + m[5] * b,
        m[6] * r + m[7] * g + m[8] * b,
    )

    fun simulate(c: Rgb, type: CvdType, severity: Double = 1.0): Rgb {
        val (r, g, b) = apply(simulationMatrix(type, severity), toLinear(c.r), toLinear(c.g), toLinear(c.b))
        return Rgb(toGamma(r), toGamma(g), toGamma(b))
    }

    /** Shifts information lost by the deficiency into channels that are still seen. */
    fun daltonize(c: Rgb, type: CvdType, severity: Double = 1.0): Rgb {
        val r = toLinear(c.r); val g = toLinear(c.g); val b = toLinear(c.b)
        val (sr, sg, sb) = apply(simulationMatrix(type, severity), r, g, b)
        val er = r - sr; val eg = g - sg; val eb = b - sb
        val (cr, cg, cb) = when (type) {
            CvdType.TRITAN -> Triple(er * 0.7 + r, eg * 0.7 + g, b)
            else -> Triple(r, g + 0.7 * er, b + 0.7 * er + eb)
        }
        return Rgb(toGamma(cr), toGamma(cg), toGamma(cb))
    }

    /**
     * 4x5 Android ColorMatrix approximation (applied in gamma space) for real-time
     * GPU filters. [daltonize] chooses correction instead of simulation.
     */
    fun androidColorMatrix(type: CvdType, severity: Double, daltonize: Boolean): FloatArray {
        val m = simulationMatrix(type, severity)
        val out = if (!daltonize) m else {
            // corrected = I + K * (I - M), K redistributes the error.
            val err = DoubleArray(9) { identity[it] - m[it] }
            val k = if (type == CvdType.TRITAN) doubleArrayOf(0.7, 0.0, 0.0, 0.0, 0.7, 0.0, 0.0, 0.0, 0.0)
            else doubleArrayOf(0.0, 0.0, 0.0, 0.7, 0.0, 0.0, 0.7, 0.0, 1.0)
            val ke = DoubleArray(9) { i ->
                val row = i / 3; val col = i % 3
                (0 until 3).sumOf { k[row * 3 + it] * err[it * 3 + col] }
            }
            DoubleArray(9) { identity[it] + ke[it] }
        }
        return floatArrayOf(
            out[0].toFloat(), out[1].toFloat(), out[2].toFloat(), 0f, 0f,
            out[3].toFloat(), out[4].toFloat(), out[5].toFloat(), 0f, 0f,
            out[6].toFloat(), out[7].toFloat(), out[8].toFloat(), 0f, 0f,
            0f, 0f, 0f, 1f, 0f,
        )
    }

    fun toLab(c: Rgb): Lab {
        val r = toLinear(c.r); val g = toLinear(c.g); val b = toLinear(c.b)
        val x = (0.4124 * r + 0.3576 * g + 0.1805 * b) / 0.95047
        val y = 0.2126 * r + 0.7152 * g + 0.0722 * b
        val z = (0.0193 * r + 0.1192 * g + 0.9505 * b) / 1.08883
        fun f(t: Double) = if (t > 0.008856) cbrt(t) else 7.787 * t + 16.0 / 116
        val fx = f(x); val fy = f(y); val fz = f(z)
        return Lab(116 * fy - 16, 500 * (fx - fy), 200 * (fy - fz))
    }

    /** CIE76 distance; adequate for naming and plate design. */
    fun deltaE(a: Rgb, b: Rgb): Double {
        val la = toLab(a); val lb = toLab(b)
        return sqrt((la.l - lb.l).pow(2) + (la.a - lb.a).pow(2) + (la.b - lb.b).pow(2))
    }

    /** Hue angle in degrees of the Lab a/b plane. */
    fun labHue(c: Rgb): Double {
        val l = toLab(c)
        return (Math.toDegrees(atan2(l.b, l.a)) + 360) % 360
    }
}

/** Inverse of [ColorMath.toLab] (D65). Result may be out of gamut; call [Rgb.clamp]. */
fun Lab.toRgb(): Rgb {
    val fy = (l + 16) / 116; val fx = fy + a / 500; val fz = fy - b / 200
    fun inv(t: Double) = if (t * t * t > 0.008856) t * t * t else (t - 16.0 / 116) / 7.787
    val x = inv(fx) * 0.95047; val y = inv(fy); val z = inv(fz) * 1.08883
    val r = 3.2406 * x - 1.5372 * y - 0.4986 * z
    val g = -0.9689 * x + 1.8758 * y + 0.0415 * z
    val bl = 0.0557 * x - 0.2040 * y + 1.0570 * z
    return Rgb(ColorMath.toGamma(r), ColorMath.toGamma(g), ColorMath.toGamma(bl))
}
