package de.mm20.launcher2.plugin.openweathermap.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OwmWeather(
    val coord: OwmWeatherCoord?,
    val weather: List<OwmWeatherWeather>?,
    val base: String?,
    val main: OwmWeatherMain?,
    val visibility: Double?,
    val wind: OwmWeatherWind?,
    val clouds: OwmWeatherClouds?,
    val rain: OwmWeatherRain?,
    val snow: OwmWeatherSnow?,
    val dt: Long?,
    val sys: OwmWeatherSys?,
    val timezone: Long?,
    val id: Int?,
    val name: String?,
    val cod: Int?,
)

@Serializable
data class OwmWeatherCoord(
    val lon: Double?,
    val lat: Double?,
)

@Serializable
data class OwmWeatherMain(
    val temp: Double?,
    @SerialName("feels_like")val feelsLike: Double?,
    val pressure: Double?,
    val humidity: Double?,
    @SerialName("temp_max") val tempMin: Double?,
    @SerialName("temp_min") val tempMax: Double?,
    @SerialName("sea_level") val seaLevel: Double?,
    @SerialName("grnd_level") val grndLevel: Double?,
)

@Serializable
data class OwmWeatherWeather(
    val id: Int?,
    val main: String?,
    val description: String?,
    val icon: String?,
)

@Serializable
data class OwmWeatherWind(
    val speed: Double?,
    val deg: Double?,
    val gust: Double?,
)

@Serializable
data class OwmWeatherClouds(
    val all: Double?,
)

@Serializable
data class OwmWeatherRain(
    val `1h`: Double?,
    val `3h`: Double?,
)

@Serializable
data class OwmWeatherSnow(
    val `1h`: Double?,
    val `3h`: Double?,
)

@Serializable
data class OwmWeatherSys(
    val type: Int?,
    val id: Int?,
    val country: String?,
    val sunrise: Long?,
    val sunset: Long?,
)
