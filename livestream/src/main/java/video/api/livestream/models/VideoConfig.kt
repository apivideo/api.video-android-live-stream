package video.api.livestream.models

import video.api.livestream.enums.Resolution

/**
 * Describes video configuration.
 */
class VideoConfig(
    /**
     * Video resolution.
     * @see [Resolution]
     */
    val resolution: Resolution = Resolution.RESOLUTION_720,

    /**
     * Video bitrate in bps.
     */
    val bitrate: Int = getDefaultBitrate(resolution),

    /**
     * Video frame rate.
     */
    val fps: Int = 30,

    /**
     * The time interval between two consecutive key frames.
     */
    val gopDuration: Float = 1f,
) {
    internal fun toSdkConfig(): io.github.thibaultbee.streampack.data.VideoConfig {
        return io.github.thibaultbee.streampack.data.VideoConfig(
            startBitrate = bitrate,
            resolution = resolution.size,
            fps = fps,
            gopDuration = gopDuration
        )
    }

    companion object {
        internal fun fromSdkConfig(config: io.github.thibaultbee.streampack.data.VideoConfig): VideoConfig {
            return VideoConfig(
                bitrate = config.startBitrate,
                resolution = Resolution.valueOf(config.resolution),
                fps = config.fps,
                gopDuration = config.gopDuration
            )
        }

        private fun getDefaultBitrate(resolution: Resolution): Int {
            return when (resolution) {
                Resolution.RESOLUTION_240 -> 800000
                Resolution.RESOLUTION_360 -> 1000000
                Resolution.RESOLUTION_480 -> 1300000
                Resolution.RESOLUTION_720 -> 2000000
                Resolution.RESOLUTION_1080 -> 3500000
            }
        }
    }
}