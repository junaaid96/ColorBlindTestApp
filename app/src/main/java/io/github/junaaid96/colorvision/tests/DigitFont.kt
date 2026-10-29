package io.github.junaaid96.colorvision.tests

/** Tiny 5x7 bitmap font used to rasterize plate figures without any copyrighted artwork. */
object DigitFont {
    private val glyphs = mapOf(
        '0' to listOf("01110", "10001", "10011", "10101", "11001", "10001", "01110"),
        '1' to listOf("00100", "01100", "00100", "00100", "00100", "00100", "01110"),
        '2' to listOf("01110", "10001", "00001", "00010", "00100", "01000", "11111"),
        '3' to listOf("11110", "00001", "00001", "01110", "00001", "00001", "11110"),
        '4' to listOf("00010", "00110", "01010", "10010", "11111", "00010", "00010"),
        '5' to listOf("11111", "10000", "11110", "00001", "00001", "10001", "01110"),
        '6' to listOf("00110", "01000", "10000", "11110", "10001", "10001", "01110"),
        '7' to listOf("11111", "00001", "00010", "00100", "01000", "01000", "01000"),
        '8' to listOf("01110", "10001", "10001", "01110", "10001", "10001", "01110"),
        '9' to listOf("01110", "10001", "10001", "01111", "00001", "00010", "01100"),
    )

    /**
     * Returns which of the [text] characters (index) covers point (x, y) in the unit square,
     * or -1 when the point is background. Strokes are thickened so dots read clearly.
     */
    fun hit(text: String, x: Double, y: Double): Int {
        val n = text.length
        val glyphW = 0.62 / n * (if (n == 1) 1.0 else 1.25)
        val glyphH = 0.62
        val total = glyphW * n + 0.06 * (n - 1)
        val left = 0.5 - total / 2
        val top = 0.5 - glyphH / 2
        if (y < top || y >= top + glyphH) return -1
        for (i in 0 until n) {
            val gx = left + i * (glyphW + 0.06)
            if (x >= gx && x < gx + glyphW) {
                val rows = glyphs[text[i]] ?: return -1
                val col = ((x - gx) / glyphW * 5).toInt().coerceIn(0, 4)
                val row = ((y - top) / glyphH * 7).toInt().coerceIn(0, 6)
                return if (rows[row][col] == '1') i else -1
            }
        }
        return -1
    }
}
