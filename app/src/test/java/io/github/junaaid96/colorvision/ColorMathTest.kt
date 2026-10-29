package io.github.junaaid96.colorvision

import io.github.junaaid96.colorvision.color.ColorMath
import io.github.junaaid96.colorvision.color.ColorNamer
import io.github.junaaid96.colorvision.color.CvdType
import io.github.junaaid96.colorvision.color.Rgb
import io.github.junaaid96.colorvision.color.toRgb
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ColorMathTest {
    private val white = Rgb(1.0, 1.0, 1.0)
    private val black = Rgb(0.0, 0.0, 0.0)

    @Test fun contrastRatioBlackWhiteIs21() = assertEquals(21.0, ColorMath.contrastRatio(white, black), 0.01)

    @Test fun gammaRoundTrip() {
        for (i in 0..10) { val v = i / 10.0; assertEquals(v, ColorMath.toGamma(ColorMath.toLinear(v)), 1e-9) }
    }

    @Test fun simulationPreservesGrey() {
        CvdType.entries.forEach { t ->
            val g = ColorMath.simulate(Rgb(0.5, 0.5, 0.5), t)
            assertEquals(0.5, g.r, 0.02); assertEquals(0.5, g.g, 0.02); assertEquals(0.5, g.b, 0.02)
        }
    }

    @Test fun zeroSeverityIsIdentity() {
        val c = Rgb(0.8, 0.2, 0.3)
        val s = ColorMath.simulate(c, CvdType.PROTAN, 0.0)
        assertEquals(c.r, s.r, 1e-6); assertEquals(c.g, s.g, 1e-6); assertEquals(c.b, s.b, 1e-6)
    }

    @Test fun redAndGreenConvergeForDeutan() {
        val red = Rgb(0.8, 0.3, 0.2); val green = Rgb(0.45, 0.5, 0.2)
        val normal = ColorMath.deltaE(red, green)
        val sim = ColorMath.deltaE(ColorMath.simulate(red, CvdType.DEUTAN), ColorMath.simulate(green, CvdType.DEUTAN))
        assertTrue("normal=$normal sim=$sim", sim < normal * 0.5)
    }

    @Test fun androidMatrixHas20Entries() {
        assertEquals(20, ColorMath.androidColorMatrix(CvdType.TRITAN, 1.0, true).size)
        assertEquals(1f, ColorMath.androidColorMatrix(CvdType.PROTAN, 0.0, false)[0], 1e-4f)
    }

    @Test fun labRoundTrip() {
        val c = Rgb(0.2, 0.6, 0.4)
        val back = ColorMath.toLab(c).toRgb()
        assertEquals(c.r, back.r, 0.01); assertEquals(c.g, back.g, 0.01); assertEquals(c.b, back.b, 0.01)
    }

    @Test fun hexFormat() = assertEquals("#FF0000", Rgb(1.0, 0.0, 0.0).hex())

    @Test fun namerFindsPrimaries() {
        assertEquals("Black", ColorNamer.name(black).name)
        assertEquals("White", ColorNamer.name(white).name)
        assertTrue(ColorNamer.confidence(ColorNamer.name(white)) > 0.9)
    }
}
