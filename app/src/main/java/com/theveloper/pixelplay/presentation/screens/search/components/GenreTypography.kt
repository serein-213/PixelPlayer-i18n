package com.theveloper.pixelplay.presentation.screens.search.components

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import com.theveloper.pixelplay.R
import com.theveloper.pixelplay.ui.theme.GoogleSansRounded
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.FontVariation
import kotlin.math.abs

// FALTA: genre_variable.ttf (Variable Font)
// Por favor, coloca el archivo de fuente variable (ej. RobotoFlex-VariableFont_GRAD,XTRA,YOPQ,YTAS,YTDE,YTFI,YTLC,YTUC,opsz,slnt,wdth,wght.ttf)
// en la carpeta: app/src/main/res/font/
// Y renómbralo a: genre_variable.ttf

object GenreTypography {

    /**
     * Generates a consistent, clean rounded style for Genre cards.
     * Uses the app's primary rounded font with smart sizing based on text length.
     */
    fun getGenreStyle(genreId: String, genreName: String): TextStyle {
        // Analyze Text Length for smart adjustments
        val length = genreName.length
        val wordCount = genreName.split(" ").size
        val isLongText = length > 10 || wordCount > 2
        val isVeryLongText = length > 16

        // Unified Style: Use GoogleSansRounded for consistency
        // Using more uniform sizing: only tiny reduction for very long text
        return TextStyle(
            fontFamily = GoogleSansRounded,
            fontWeight = FontWeight.Bold,
            fontSize = if (isVeryLongText) 18.sp else 20.sp,
            letterSpacing = if (isVeryLongText) (-0.4).sp else 0.sp
        )
    }
}
