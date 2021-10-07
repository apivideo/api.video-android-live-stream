package video.api.livestream.models

/**
 * Describes audio configuration.
 */
data class AudioConfig(
    /**
     * Audio bitrate in bps.
     */
    val bitrate: Int,

    /**
     * Audio sample rate in Hz.
     */
    val sampleRate: Int,

    /**
     * [Boolean.true] if you want audio capture in stereo,
     * [Boolean.false] for mono.
     */
    val stereo: Boolean,

    /**
     * [Boolean.true] if you want to activate echo canceler.
     * [Boolean.false] to deactivate.
     */
    val echoCanceler: Boolean,

    /**
     * [Boolean.true] if you want to activate noise suppressor.
     * [Boolean.false] to deactivate.
     */
    val noiseSuppressor: Boolean
)