package video.api.livestream.models

import video.api.livestream.enums.Resolution

/**
 * Describes video configuration.
 */
class VideoConfig(
    /**
     * Video bitrate in bps.
     */
    val bitrate: Int,

    /**
     * Video resolution.
     * @see [Resolution]
     */
    val resolution: Resolution,

    /**
     * Video frame rate.
     */
    val fps: Int
) {
    internal fun toSdkConfig(): io.github.thibaultbee.streampack.data.VideoConfig {
        return io.github.thibaultbee.streampack.data.VideoConfig(
            startBitrate = bitrate,
            resolution = resolution.size,
            fps = fps
        )
    }

    companion object {
        internal fun fromSdkConfig(config: io.github.thibaultbee.streampack.data.VideoConfig): VideoConfig {
            return VideoConfig(
                bitrate = config.startBitrate,
                resolution = Resolution.valueOf(config.resolution),
                fps = config.fps
            )
        }
    }
}