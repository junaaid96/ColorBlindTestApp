package io.github.junaaid96.colorvision.tests

import io.github.junaaid96.colorvision.color.Rgb
import kotlin.random.Random

/** Daily practice round: find the tile whose hue differs. Gets harder each level. */
data class OddHueRound(val size: Int, val base: Rgb, val odd: Rgb, val oddIndex: Int)

object OddHue {
    fun round(level: Int, rnd: Random = Random.Default): OddHueRound {
        val size = (2 + level / 3).coerceAtMost(7)
        val hue = rnd.nextDouble(0.0, 360.0)
        val delta = (40.0 * Math.pow(0.82, level.toDouble())).coerceAtLeast(2.5)
        val sat = rnd.nextDouble(0.45, 0.75); val light = rnd.nextDouble(0.4, 0.6)
        return OddHueRound(size, Rgb.hsl(hue, sat, light), Rgb.hsl(hue + delta, sat, light), rnd.nextInt(size * size))
    }
}
