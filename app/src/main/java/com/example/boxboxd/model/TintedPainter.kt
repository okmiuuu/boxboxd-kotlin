package com.example.boxboxd.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter

class TintedPainter(
    private val painter: Painter,
    private val tint: Color
) : Painter() {
    override val intrinsicSize = painter.intrinsicSize

    override fun DrawScope.onDraw() {
        with(painter) {
            draw(size, colorFilter = ColorFilter.tint(tint))
        }
    }
}