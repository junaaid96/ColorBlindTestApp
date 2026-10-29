package io.github.junaaid96.colorvision

import io.github.junaaid96.colorvision.color.ColorMath
import io.github.junaaid96.colorvision.color.CvdType
import io.github.junaaid96.colorvision.color.Rgb
import io.github.junaaid96.colorvision.tests.Classification
import io.github.junaaid96.colorvision.tests.ContrastStaircase
import io.github.junaaid96.colorvision.tests.DigitFont
import io.github.junaaid96.colorvision.tests.HueArrangement
import io.github.junaaid96.colorvision.tests.OddHue
import io.github.junaaid96.colorvision.tests.PlateGenerator
import io.github.junaaid96.colorvision.tests.PlateKind
import io.github.junaaid96.colorvision.tests.PlateScoring
import io.github.junaaid96.colorvision.tests.Severity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class TestsLogicTest {
    @Test fun digitFontHitsCentreOfEight() {
        assertEquals(0, DigitFont.hit("8", 0.5, 0.25))
        assertEquals(-1, DigitFont.hit("8", 0.02, 0.02))
    }

    @Test fun plansHaveExpectedLength() {
        assertEquals(7, PlateGenerator.plan(false).size)
        assertEquals(16, PlateGenerator.plan(true).size)
        assertTrue(PlateGenerator.plan(true).first().kind == PlateKind.CONTROL)
    }

    @Test fun plansAreDeterministicPerSeed() {
        assertEquals(PlateGenerator.plan(false, 5).map { it.answer }, PlateGenerator.plan(false, 5).map { it.answer })
    }

    @Test fun redGreenFigureCollapsesUnderSimulation() {
        val bg = Rgb.hsl(95.0, 0.45, 0.5)
        val fig = PlateGenerator.figureColor(bg, listOf(CvdType.PROTAN, CvdType.DEUTAN), emptyList(), 0)
        val normal = PlateGenerator.seenDistance(fig, bg, null)
        listOf(CvdType.PROTAN, CvdType.DEUTAN).forEach {
            assertTrue("$it", PlateGenerator.seenDistance(fig, bg, it) < normal * 0.5)
        }
    }

    @Test fun perfectAnswersAreTypical() {
        val plates = PlateGenerator.plan(true)
        val r = PlateScoring.score(plates, plates.map { it.answer })
        assertEquals(Classification.TYPICAL, r.classification)
        assertEquals(Severity.NONE, r.severity)
        assertEquals(100, r.score)
    }

    @Test fun missedControlIsInconclusive() {
        val plates = PlateGenerator.plan(false)
        val answers = plates.map { it.answer }.toMutableList().also { it[0] = "" }
        assertEquals(Classification.INCONCLUSIVE, PlateScoring.score(plates, answers).classification)
    }

    @Test fun protanPatternDetected() {
        val plates = PlateGenerator.plan(true)
        val answers = plates.map { p ->
            when (p.kind) {
                PlateKind.RED_GREEN -> ""
                PlateKind.DIAGNOSTIC -> p.answer.substring(1)
                else -> p.answer
            }
        }
        val r = PlateScoring.score(plates, answers)
        assertEquals(Classification.PROTAN, r.classification)
        assertEquals(Severity.STRONG, r.severity)
    }

    @Test fun deutanPatternDetected() {
        val plates = PlateGenerator.plan(true)
        val answers = plates.map { p ->
            when (p.kind) {
                PlateKind.RED_GREEN -> ""
                PlateKind.DIAGNOSTIC -> p.answer.substring(0, 1)
                else -> p.answer
            }
        }
        assertEquals(Classification.DEUTAN, PlateScoring.score(plates, answers).classification)
    }

    @Test fun correctArrangementPasses() {
        val r = HueArrangement.score((1..HueArrangement.CAPS).toList())
        assertTrue(r.passed); assertEquals(0, r.crossings)
    }

    @Test fun scrambledArrangementFails() {
        val order = (1..HueArrangement.CAPS).shuffled(Random(3))
        assertFalse(HueArrangement.score(order).passed)
    }

    @Test fun capsAreDistinct() {
        val caps = HueArrangement.caps()
        assertEquals(HueArrangement.CAPS + 1, caps.size)
        (1 until caps.size).forEach { assertTrue(ColorMath.deltaE(caps[it], caps[it - 1]) > 3) }
    }

    @Test fun staircaseConvergesNearThreshold() {
        val s = ContrastStaircase()
        val trueThreshold = 0.03
        while (!s.finished) s.answer(s.contrast >= trueThreshold)
        assertTrue("t=${s.threshold()}", s.threshold() in 0.015..0.06)
        assertTrue(s.logSensitivity() > 1.2)
    }

    @Test fun oddHueTileDiffers() {
        val r = OddHue.round(5, Random(1))
        assertNotEquals(r.base, r.odd)
        assertTrue(r.oddIndex in 0 until r.size * r.size)
    }
}
