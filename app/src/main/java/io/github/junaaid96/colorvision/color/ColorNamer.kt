package io.github.junaaid96.colorvision.color

/** Names a color by its nearest entry (CIE Lab distance) in a curated everyday palette. */
object ColorNamer {
    data class Named(val name: String, val rgb: Rgb, val distance: Double)

    private val palette: List<Pair<String, Int>> = listOf(
        "Black" to 0x000000, "Charcoal" to 0x36454F, "Gray" to 0x808080, "Silver" to 0xC0C0C0,
        "White" to 0xFFFFFF, "Ivory" to 0xFFFFF0, "Beige" to 0xF5F5DC, "Cream" to 0xFFFDD0,
        "Red" to 0xE53935, "Dark red" to 0x8B0000, "Maroon" to 0x800000, "Crimson" to 0xDC143C,
        "Pink" to 0xFFC0CB, "Hot pink" to 0xFF69B4, "Magenta" to 0xFF00FF, "Rose" to 0xFF007F,
        "Orange" to 0xFF8C00, "Coral" to 0xFF7F50, "Salmon" to 0xFA8072, "Peach" to 0xFFDAB9,
        "Brown" to 0x8B4513, "Chocolate" to 0x5D3A1A, "Tan" to 0xD2B48C, "Khaki" to 0xC3B091,
        "Gold" to 0xFFD700, "Yellow" to 0xFFEB3B, "Mustard" to 0xE1AD01, "Lemon" to 0xFFF44F,
        "Olive" to 0x808000, "Lime" to 0x9ACD32, "Green" to 0x2E7D32, "Bright green" to 0x00FF00,
        "Dark green" to 0x013220, "Mint" to 0x98FF98, "Sage" to 0x9CAF88, "Forest green" to 0x228B22,
        "Teal" to 0x008080, "Turquoise" to 0x40E0D0, "Cyan" to 0x00FFFF, "Aqua" to 0x7FFFD4,
        "Sky blue" to 0x87CEEB, "Light blue" to 0xADD8E6, "Blue" to 0x1E63E9, "Navy" to 0x000080,
        "Royal blue" to 0x4169E1, "Indigo" to 0x4B0082, "Purple" to 0x800080, "Violet" to 0x8F00FF,
        "Lavender" to 0xE6E6FA, "Lilac" to 0xC8A2C8, "Plum" to 0x8E4585,
    )

    fun name(c: Rgb): Named = palette
        .map { (n, hex) -> val p = Rgb.ofArgb(hex); Named(n, p, ColorMath.deltaE(c, p)) }
        .minBy { it.distance }

    /** 0..1 confidence that the name is a good fit. */
    fun confidence(n: Named): Double = (1.0 - n.distance / 40.0).coerceIn(0.0, 1.0)
}
