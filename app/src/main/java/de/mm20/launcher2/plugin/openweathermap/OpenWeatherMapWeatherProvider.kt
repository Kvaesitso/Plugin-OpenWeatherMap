package de.mm20.launcher2.plugin.openweathermap

import android.content.Intent
import android.util.Log
import de.mm20.launcher2.plugin.config.WeatherPluginConfig
import de.mm20.launcher2.plugin.openweathermap.api.OwmForecast
import de.mm20.launcher2.plugin.openweathermap.api.OwmForecastList
import de.mm20.launcher2.plugin.openweathermap.api.OwmWeather
import de.mm20.launcher2.sdk.PluginState
import de.mm20.launcher2.sdk.weather.Forecast
import de.mm20.launcher2.sdk.weather.K
import de.mm20.launcher2.sdk.weather.WeatherIcon
import de.mm20.launcher2.sdk.weather.WeatherLocation
import de.mm20.launcher2.sdk.weather.WeatherProvider
import de.mm20.launcher2.sdk.weather.hPa
import de.mm20.launcher2.sdk.weather.m_s
import de.mm20.launcher2.sdk.weather.mm
import kotlinx.coroutines.flow.first
import java.util.Locale
import kotlin.math.roundToInt

class OpenWeatherMapWeatherProvider : WeatherProvider(
    WeatherPluginConfig()
) {

    private lateinit var apiClient: OwmApiClient

    override fun onCreate(): Boolean {
        apiClient = OwmApiClient(context!!.applicationContext)
        return super.onCreate()
    }

    override suspend fun getWeatherData(location: WeatherLocation, lang: String?): List<Forecast>? {
        return when (location) {
            is WeatherLocation.LatLon -> getWeatherData(
                location.lat,
                location.lon,
                lang,
                location.name
            )

            else -> {
                Log.e("OWMWeatherProvider", "Invalid location $location")
                null
            }
        }
    }

    override suspend fun getWeatherData(lat: Double, lon: Double, lang: String?): List<Forecast>? {
        return getWeatherData(lat, lon, lang, null)
    }

    private suspend fun getWeatherData(
        lat: Double,
        lon: Double,
        lang: String?,
        locationName: String?
    ): List<Forecast>? {
        val weather: OwmWeather =
            apiClient.weather(lat, lon, getLanguageCode(lang ?: Locale.getDefault().language))

        val forecastList = mutableListOf<Forecast>()

        val city = weather.name
        val country = weather.sys?.country ?: return null
        val cityId = weather.id ?: return null
        val coords = weather.coord ?: return null
        if (coords.lat == null || coords.lon == null) return null
        val loc = locationName ?: "$city, $country"

        val currentForecast = owmToForecast(weather, loc) ?: return null
        forecastList.add(currentForecast)

        val forecasts: OwmForecast = apiClient.forecast(
            lat,
            lon,
            lang = getLanguageCode(lang ?: Locale.getDefault().language)
        )

        if (forecasts.list == null) {
            Log.e("OWMWeatherProvider", "Forecast response has no forecasts")
            return null
        }

        for (forecast in forecasts.list) {
            forecastList += owmToForecast(
                forecast,
                loc,
                forecasts.city?.id
            ) ?: continue
        }

        return forecastList
    }

    private fun owmToForecast(weather: OwmWeather, location: String): Forecast? {
        val context = context ?: return null
        return Forecast(
            timestamp = (weather.dt ?: return null) * 1000,
            condition = weather.weather?.firstOrNull()?.description ?: return null,
            temperature = weather.main?.temp?.K ?: return null,
            minTemp = weather.main.tempMin?.K,
            maxTemp = weather.main.tempMax?.K,
            pressure = weather.main.pressure?.hPa,
            humidity = weather.main.humidity?.roundToInt(),
            precipitation = ((weather.rain?.`3h` ?: 0.0) + (weather.snow?.`3h` ?: 0.0)).mm,
            icon = iconForId(weather.weather.firstOrNull()?.id ?: return null),
            clouds = weather.clouds?.all?.roundToInt(),
            windSpeed = weather.wind?.speed?.m_s,
            windDirection = weather.wind?.deg ?: -1.0,
            night = run {
                val sunrise = weather.sys?.sunrise ?: 0
                val sunset = weather.sys?.sunset ?: 0
                weather.dt > sunset || weather.dt < sunrise
            },
            location = location,
            provider = context.getString(R.string.plugin_name),
            providerUrl = weather.id?.let { "https://openweathermap.org/city/$it" },
            createdAt = System.currentTimeMillis()
        )
    }

    private fun owmToForecast(
        forecast: OwmForecastList,
        location: String,
        cityId: Int?
    ): Forecast? {
        val context = context ?: return null
        return Forecast(
            timestamp = (forecast.dt ?: return null) * 1000L,
            icon = iconForId(forecast.weather?.firstOrNull()?.id ?: return null),
            condition = forecast.weather.firstOrNull()?.description ?: return null,
            temperature = forecast.main?.temp?.K ?: return null,
            createdAt = System.currentTimeMillis(),
            provider = context.getString(R.string.plugin_name),
            providerUrl = cityId?.let { "https://openweathermap.org/city/$it" },
            location = location,
            clouds = forecast.clouds?.all?.roundToInt(),
            windSpeed = forecast.wind?.speed?.m_s,
            windDirection = forecast.wind?.deg,
            night = forecast.sys?.pod == "n",
            humidity = forecast.main.humidity?.roundToInt(),
            pressure = forecast.main.pressure?.hPa,
            maxTemp = forecast.main.tempMax?.K,
            minTemp = forecast.main.tempMin?.K,
            precipitation = ((forecast.rain?.`3h` ?: 0.0) + (forecast.snow?.`3h` ?: 0.0)).mm,
            rainProbability = forecast.pop?.roundToInt(),
        )
    }

    private fun iconForId(id: Int): WeatherIcon {
        return when (id) {
            200, 201, in 230..232 -> WeatherIcon.ThunderstormWithRain
            202 -> WeatherIcon.ThunderstormWithRain
            210, 211 -> WeatherIcon.Thunderstorm
            212, 221 -> WeatherIcon.HeavyThunderstorm
            in 300..302, in 310..312 -> WeatherIcon.Drizzle
            313, 314, 321, in 500..504, 511, in 520..522, 531 -> WeatherIcon.Showers
            in 600..602 -> WeatherIcon.Snow
            611, 612, 615, 616, in 620..622 -> WeatherIcon.Sleet
            701, 711, 731, 741, 761, 762 -> WeatherIcon.Fog
            721 -> WeatherIcon.Haze
            771, 781, in 900..902, in 958..962 -> WeatherIcon.Storm
            800 -> WeatherIcon.Clear
            801 -> WeatherIcon.PartlyCloudy
            802 -> WeatherIcon.MostlyCloudy
            803 -> WeatherIcon.BrokenClouds
            804, 951 -> WeatherIcon.Cloudy
            903 -> WeatherIcon.Cold
            904 -> WeatherIcon.Hot
            905, in 952..957 -> WeatherIcon.Wind
            906 -> WeatherIcon.Hail
            else -> WeatherIcon.Unknown
        }
    }

    /**
     * OWM incorrectly expects country codes instead of language codes for some languages
     * see https://openweathermap.org/current#multi
     */
    private fun getLanguageCode(lang: String): String {
        when (lang) {
            "cs" -> return "cz"
            "al" -> return "sq"
            "kr" -> return "ko"
            "lv" -> return "la"
            else -> return lang
        }
    }

    override suspend fun findLocations(query: String, lang: String): List<WeatherLocation> {
        val geo = apiClient.geo(q = query, limit = 5)

        // Here, OWM uses the correct language codes, so we don't need to map anything
        return geo.mapNotNull {
            val name = it.localNames?.get(lang) ?: it.name ?: return@mapNotNull null
            WeatherLocation.LatLon(
                name = "$name, ${it.country}",
                lat = it.lat ?: return@mapNotNull null,
                lon = it.lon ?: return@mapNotNull null,
            )
        }
    }

    override suspend fun getPluginState(): PluginState {
        val context = context!!
        apiClient.apiKey.first() ?: return PluginState.SetupRequired(
            Intent(context, SettingsActivity::class.java),
            context.getString(R.string.plugin_state_setup_required)
        )
        return PluginState.Ready()
    }

}