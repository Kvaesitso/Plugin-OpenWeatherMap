package de.mm20.launcher2.plugin.openweathermap.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OwmForecast(
    val cod: Int?,
    val message: Int?,
    val cnt: Int?,
    val list: List<OwmForecastList>?,
    val city: OwmForecastCity?,
)

@Serializable
data class OwmForecastList(
    val dt: Long?,
    val main: OwmForecastMain?,
    val weather: List<OwmForecastWeather>?,
    val clouds: OwmForecastClouds?,
    val wind: OwmForecastWind?,
    val visibility: Double?,
    val pop: Double?,
    val rain: OwmForecastRain?,
    val snow: OwmForecastSnow?,
    val sys: OwmForecastSys?,
    @SerialName("dt_txt") val dtTxt: String?,
)

@Serializable
data class OwmForecastMain(
    val temp: Double?,
    @SerialName("feels_like")val feelsLike: Double?,
    val pressure: Double?,
    val humidity: Double?,
    @SerialName("temp_max") val tempMin: Double?,
    @SerialName("temp_min") val tempMax: Double?,
    @SerialName("sea_level") val seaLevel: Double?,
    @SerialName("grnd_level") val grndLevel: Double?,
    @SerialName("temp_kf") val tempKf: Double?,
)

@Serializable
data class OwmForecastWeather(
    val id: Int?,
    val main: String?,
    val description: String?,
    val icon: String?,
)

@Serializable
data class OwmForecastClouds(
    val all: Double?,
)

@Serializable
data class OwmForecastWind(
    val speed: Double?,
    val deg: Double?,
    val gust: Double?,
)

@Serializable
data class OwmForecastRain(
    val `3h`: Double?,
)

@Serializable
data class OwmForecastSnow(
    val `3h`: Double?,
)

@Serializable
data class OwmForecastSys(
    val pod: String?,
)

@Serializable
data class OwmForecastCity(
    val id: Int?,
    val name: String?,
    val coord: OwmForecastCoord?,
    val country: String?,
    val population: Int?,
    val timezone: Int?,
    val sunrise: Long?,
    val sunset: Long?,
)

@Serializable
data class OwmForecastCoord(
    val lon: Double?,
    val lat: Double?,
)