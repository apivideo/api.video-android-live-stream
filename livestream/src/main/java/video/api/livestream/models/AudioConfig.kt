package video.api.livestream.models

import android.media.AudioFormat

/**
 * Describes audio configuration.
 */
data class AudioConfig(
    /**
     * Audio bitrate in bps.
     */
    val bitrate: Int = 128000,

    /**
     * Audio sample rate in Hz.
     */
    val sampleRate: Int = 44100,

    /**
     * [Boolean.true] if you want audio capture in stereo,
     * [Boolean.false] for mono.
     */
    val stereo: Boolean = true,

    /**
     * [Boolean.true] if you want to activate echo canceler.
     * [Boolean.false] to deactivate.
     */
    val echoCanceler: Boolean = true,

    /**
     * [Boolean.true] if you want to activate noise suppressor.
     * [Boolean.false] to deactivate.
     */
    val noiseSuppressor: Boolean = true
) {
    internal fun toSdkConfig(): io.github.thibaultbee.streampack.data.AudioConfig {
        return io.github.thibaultbee.streampack.data.AudioConfig(
            startBitrate = bitrate,
            sampleRate = sampleRate,
            channelConfig = if (stereo) AudioFormat.CHANNEL_IN_STEREO else AudioFormat.CHANNEL_IN_MONO,
            enableEchoCanceler = echoCanceler,
            enableNoiseSuppressor = noiseSuppressor
        )
    }

    companion object {
        internal fun fromSdkConfig(config: io.github.thibaultbee.streampack.data.AudioConfig): AudioConfig {
            return AudioConfig(
                bitrate = config.startBitrate,
                sampleRate = config.sampleRate,
                stereo = config.channelConfig == AudioFormat.CHANNEL_IN_STEREO,
                echoCanceler = config.enableEchoCanceler,
                noiseSuppressor = config.enableNoiseSuppressor
            )
        }
    }
}