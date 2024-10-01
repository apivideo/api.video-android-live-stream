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
) {
    internal fun toSdkConfig(): io.github.thibaultbee.streampack.core.data.AudioConfig {
        return io.github.thibaultbee.streampack.core.data.AudioConfig(
            startBitrate = bitrate,
            sampleRate = sampleRate,
            channelConfig = if (stereo) AudioFormat.CHANNEL_IN_STEREO else AudioFormat.CHANNEL_IN_MONO
        )
    }

    companion object {
        internal fun fromSdkConfig(config: io.github.thibaultbee.streampack.core.data.AudioConfig): AudioConfig {
            return AudioConfig(
                bitrate = config.startBitrate,
                sampleRate = config.sampleRate,
                stereo = config.channelConfig == AudioFormat.CHANNEL_IN_STEREO
            )
        }
    }
}