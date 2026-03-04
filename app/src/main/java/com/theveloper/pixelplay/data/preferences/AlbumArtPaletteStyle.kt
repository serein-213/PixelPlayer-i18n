package com.theveloper.pixelplay.data.preferences

import androidx.annotation.StringRes
import com.theveloper.pixelplay.R

enum class AlbumArtPaletteStyle(
    val storageKey: String,
    val label: String,
    @StringRes val labelRes: Int,
    @StringRes val descriptionRes: Int
) {
    TONAL_SPOT("tonal_spot", "Tonal Spot", R.string.palette_style_tonal_spot, R.string.palette_description_tonal_spot),
    VIBRANT("vibrant", "Vibrant", R.string.palette_style_vibrant, R.string.palette_description_vibrant),
    EXPRESSIVE("expressive", "Expressive", R.string.palette_style_expressive, R.string.palette_description_expressive),
    FRUIT_SALAD("fruit_salad", "Fruit Salad", R.string.palette_style_fruit_salad, R.string.palette_description_fruit_salad),
    MONOCHROME("monochrome", "Monochrome", R.string.palette_style_monochrome, R.string.palette_description_monochrome);

    companion object {
        val default: AlbumArtPaletteStyle = TONAL_SPOT

        fun fromStorageKey(value: String?): AlbumArtPaletteStyle {
            return entries.firstOrNull { it.storageKey == value } ?: default
        }
    }
}
