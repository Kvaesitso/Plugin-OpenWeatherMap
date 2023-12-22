package de.mm20.launcher2.plugin.openweathermap.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OwmGeo(
    val name: String?,
    @SerialName("local_names") val localNames: Map<String, String>?,
    val lat: Double?,
    val lon: Double?,
    val country: String?,
    val state: String?,
)