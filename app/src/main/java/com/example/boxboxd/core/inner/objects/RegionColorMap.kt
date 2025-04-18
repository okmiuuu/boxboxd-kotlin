package com.example.boxboxd.core.inner.objects

import androidx.compose.ui.graphics.Color
import com.example.boxboxd.ui.theme.africa
import com.example.boxboxd.ui.theme.asia
import com.example.boxboxd.ui.theme.australia
import com.example.boxboxd.ui.theme.europe
import com.example.boxboxd.ui.theme.northAmerica
import com.example.boxboxd.ui.theme.southAmerica

object RegionColorMap {
    val regionToColor: Map<String, Color> = mapOf(
        "Australia" to australia,
        "Asia" to asia,
        "Europe" to europe,
        "North America" to northAmerica,
        "South America" to southAmerica,
        "Africa" to africa
    )
}