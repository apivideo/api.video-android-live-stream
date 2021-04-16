package video.api.livestream_module.model

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

@Suppress("UNCHECKED_CAST")
operator fun <T> JSONArray.iterator(): Iterator<T>
        = (0 until length()).asSequence().map { get(it) as T }.iterator()

fun JSONObject.getStringOrNull(name: String): String? = if (has(name)) {
    getString(name)
} else {
    null
}

fun JSONObject.getIntOrNull(value: String): Int? = if (has(value)) {
    getInt(value)
} else {
    null
}

fun JSONObject.getDoubleOrNull(value: String): Double? = if (has(value)) {
    getDouble(value)
} else {
    null
}

private val dateFormat: SimpleDateFormat
    get() = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.getDefault())

fun JSONObject.getDateOrNull(name: String): Date? = if (has(name)) {
    dateFormat.parse(getString(name))
} else {
    null
}

fun JSONObject.getBooleanOrDefault(name: String, default: Boolean): Boolean =
    if (has(name)) getBoolean(name) else default

fun JSONObject.getHttpUrlOrNull(name: String): HttpUrl? =
    if (has(name)) {
        getString(name).toHttpUrl()
    } else {
        null
    }