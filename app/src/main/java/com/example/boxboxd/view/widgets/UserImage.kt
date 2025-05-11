package com.example.boxboxd.view.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import co.yml.charts.common.extensions.isNotNull
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.boxboxd.R
import com.example.boxboxd.core.inner.User
import com.example.boxboxd.model.TintedPainter

@Composable
fun UserImage(user: User, modifier: Modifier = Modifier) {

    val placeholderPainter = TintedPainter(
        painter = painterResource(R.drawable.user),
        tint = MaterialTheme.colorScheme.tertiary
    )

    AsyncImage(
        model = user.picture,
        contentDescription = "User profile picture",
        modifier = modifier
            .clip(RoundedCornerShape(2.dp))
            .fillMaxSize(),
        contentScale = ContentScale.Crop,
        placeholder = placeholderPainter,
        error = placeholderPainter
    )
}