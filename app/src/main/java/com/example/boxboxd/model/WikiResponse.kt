package com.example.boxboxd.model

import kotlinx.serialization.Serializable

@Serializable
data class WikiResponse(
    val query: WikiQuery
)

@Serializable
data class WikiQuery(
    val pages: Map<String, WikiPage>
)

@Serializable
data class WikiPage(
    val pageid: Int,
    val ns: Int,
    val title: String,
    val thumbnail: WikiThumbnail? = null
)

@Serializable
data class WikiThumbnail(
    val source: String,
    val width: Int,
    val height: Int
)