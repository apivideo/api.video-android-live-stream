package video.api.livestream.models

data class AudioConfig(
    val bitrate: Int,
    val sampleRate: Int,
    val stereo: Boolean,
    val echoCanceler: Boolean,
    val noiseSuppressor: Boolean
)