package video.api.livestream.example.ui.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.util.Size
import androidx.preference.PreferenceManager
import video.api.livestream.app.R

class Configuration(context: Context) {
    private val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
    private val resources = context.resources
    val video = Video(sharedPref, resources)
    val audio = Audio(sharedPref, resources)
    val endpoint = Endpoint(sharedPref, resources)

    class Video(private val sharedPref: SharedPreferences, private val resources: Resources) {
        var fps: Int = 30
            get() = sharedPref.getString(
                resources.getString(R.string.video_fps_key),
                field.toString()
            )!!.toInt()

        var resolution: Size = Size(1280, 720)
            get() {
                val res = sharedPref.getString(
                    resources.getString(R.string.video_resolution_key),
                    field.toString()
                )!!
                val resArray = res.split("x")
                return Size(
                    resArray[0].toInt(),
                    resArray[1].toInt()
                )
            }

        var bitrate: Int = 2000
            get() = sharedPref.getInt(
                resources.getString(R.string.video_bitrate_key),
                field
            )
    }

    class Audio(private val sharedPref: SharedPreferences, private val resources: Resources) {
        var numberOfChannels: Int = 2
            get() = sharedPref.getString(
                resources.getString(R.string.audio_number_of_channels_key),
                field.toString()
            )!!.toInt()

        var bitrate: Int = 128000
            get() = sharedPref.getString(
                resources.getString(R.string.audio_bitrate_key),
                field.toString()
            )!!.toInt()

        var sampleRate: Int = 48000
            get() = sharedPref.getString(
                resources.getString(R.string.audio_sample_rate_key),
                field.toString()
            )!!.toInt()

        var enableEchoCanceler: Boolean = false
            get() = sharedPref.getBoolean(
                resources.getString(R.string.audio_enable_echo_canceler_key),
                field
            )

        var enableNoiseSuppressor: Boolean = false
            get() = sharedPref.getBoolean(
                resources.getString(R.string.audio_enable_noise_suppressor_key),
                field
            )
    }

    class Endpoint(private val sharedPref: SharedPreferences, private val resources: Resources) {
        var url: String = ""
            get() = sharedPref.getString(
                resources.getString(R.string.rtmp_endpoint_url_key),
                field
            )!!

        var streamKey: String = ""
            get() = sharedPref.getString(resources.getString(R.string.rtmp_stream_key_key), field)!!
    }
}