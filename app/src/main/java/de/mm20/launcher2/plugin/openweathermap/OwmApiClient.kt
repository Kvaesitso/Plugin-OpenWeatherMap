package de.mm20.launcher2.plugin.openweathermap

import android.content.Context
import android.util.Log
import de.mm20.launcher2.plugin.openweathermap.api.OwmForecast
import de.mm20.launcher2.plugin.openweathermap.api.OwmGeo
import de.mm20.launcher2.plugin.openweathermap.api.OwmWeather
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import io.ktor.http.path
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.io.IOException

class OwmApiClient(
    private val context: Context
) {

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                explicitNulls = false
            })
        }
    }

    suspend fun weather(
        lat: Double,
        lon: Double,
        lang: String? = null,
        appid: String? = null
    ): OwmWeather {
        val apiKey = appid ?: apiKey.first() ?: throw IllegalArgumentException("No API key provided")
        val response = client.get {
            url {
                protocol = URLProtocol.HTTPS
                host = "api.openweathermap.org"
                path("data", "2.5", "weather")
                parameters["lat"] = lat.toString()
                parameters["lon"] = lon.toString()
                parameters["appid"] = apiKey
                if (lang != null) parameters["lang"] = lang
            }
        }
        if (response.status == HttpStatusCode.Unauthorized) {
            throw IllegalArgumentException("Unauthorized. Invalid API key?; body ${response.bodyAsText()}")
        } else if (response.status != HttpStatusCode.OK) {
            throw IOException("API error: status ${response.status.value}; body ${response.bodyAsText()}")
        }
        return response.body()
    }

    suspend fun forecast(
        lat: Double,
        lon: Double,
        cnt: Int? = null,
        lang: String? = null,
        appid: String? = null
    ): OwmForecast {
        val apiKey = appid ?: apiKey.first() ?: throw IllegalArgumentException("No API key provided")
        val response = client.get {
            url {
                protocol = URLProtocol.HTTPS
                host = "api.openweathermap.org"
                path("data", "2.5", "forecast")
                parameters["lat"] = lat.toString()
                parameters["lon"] = lon.toString()
                parameters["appid"] = apiKey
                if (lang != null) parameters["lang"] = lang
                if (cnt != null) parameters["cnt"] = cnt.toString()
            }
        }
        if (response.status == HttpStatusCode.Unauthorized) {
            throw IllegalArgumentException("Unauthorized. Invalid API key?; body ${response.bodyAsText()}")
        } else if (response.status != HttpStatusCode.OK) {
            throw IOException("API error: status ${response.status.value}; body ${response.bodyAsText()}")
        }
        return response.body()
    }

    suspend fun geo(
        q: String,
        limit: Int? = null,
        appid: String? = null
    ): Array<OwmGeo> {
        val apiKey = appid ?: apiKey.first() ?: throw IllegalArgumentException("No API key provided")
        val response = client.get {
            url {
                protocol = URLProtocol.HTTPS
                host = "api.openweathermap.org"
                path("geo", "1.0", "direct")
                parameters["q"] = q
                parameters["appid"] = apiKey
                if (limit != null) parameters["limit"] = limit.toString()
            }
        }
        if (response.status == HttpStatusCode.Unauthorized) {
            throw IllegalArgumentException("Unauthorized. Invalid API key?; body ${response.bodyAsText()}")
        } else if (response.status != HttpStatusCode.OK) {
            throw IOException("API error: status ${response.status.value}; body ${response.bodyAsText()}")
        }
        return response.body()
    }

    suspend fun setApiKey(apiKey: String) {
        context.dataStore.updateData {
            it.copy(apiKey = apiKey)
        }
    }

    suspend fun testApiKey(apiKey: String): Boolean {
        return try {
            weather(
                lat = 51.5,
                lon = 0.0,
                appid = apiKey
            )
            return true
        } catch (e: IllegalArgumentException) {
            Log.e("OwmApiClient", "Invalid API key", e)
            return false
        }
    }

    val apiKey: Flow<String?> = context.dataStore.data.map { it.apiKey }

}