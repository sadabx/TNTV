package com.tntv.tv

data class StreamSource(val label: String, val url: String)

data class Channel(
    val id: String,
    val name: String,
    val shortName: String,
    val category: String,
    val logo: String,
    val streams: List<StreamSource>,
)

data class ChannelCategory(
    val name: String,
    val channels: List<Channel>,
)
