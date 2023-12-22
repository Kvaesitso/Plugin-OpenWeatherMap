package de.mm20.launcher2.plugin.openweathermap

import android.content.Context
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.InputStream
import java.io.OutputStream

@Serializable
data class UserData(
    val apiKey: String? = null
)

val Context.dataStore by dataStore("userdata.json", UserDataSerializer)

object UserDataSerializer: Serializer<UserData> {
    override val defaultValue: UserData = UserData()

    override suspend fun readFrom(input: InputStream): UserData {
        return Json.decodeFromStream(input)
    }

    override suspend fun writeTo(t: UserData, output: OutputStream) {
        Json.encodeToStream(UserData.serializer(), t, output)
    }
}