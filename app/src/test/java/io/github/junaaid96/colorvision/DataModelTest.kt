package io.github.junaaid96.colorvision

import io.github.junaaid96.colorvision.data.HistoryEntry
import io.github.junaaid96.colorvision.data.Streak
import io.github.junaaid96.colorvision.data.TestKind
import io.github.junaaid96.colorvision.tests.Classification
import io.github.junaaid96.colorvision.tests.Severity
import org.junit.Assert.assertEquals
import org.junit.Test

class DataModelTest {
    @Test fun historyRoundTrip() {
        val list = listOf(
            HistoryEntry(1L, TestKind.QUICK, Classification.DEUTAN, Severity.MILD, 71, 64),
            HistoryEntry(2L, TestKind.CONTRAST, Classification.TYPICAL, Severity.NONE, 165, 70),
        )
        assertEquals(list, HistoryEntry.decodeAll(HistoryEntry.encodeAll(list)))
        assertEquals(emptyList<HistoryEntry>(), HistoryEntry.decodeAll(null))
        assertEquals(1, HistoryEntry.decodeAll("garbage;" + list[0].encode()).size)
    }

    @Test fun streakCounting() {
        var s = Streak()
        s = s.record(100); assertEquals(1, s.current)
        s = s.record(100); assertEquals(1, s.current)
        s = s.record(101); assertEquals(2, s.current)
        s = s.record(105); assertEquals(1, s.current); assertEquals(2, s.best)
        assertEquals(0, s.active(107)); assertEquals(1, s.active(106))
    }
}
