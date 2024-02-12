package video.api.livestream.models

import android.util.Size
import video.api.livestream.enums.Resolution

/**
 * Describes video configuration.
 */
class VideoConfig(
    /**
     * Video resolution.
     * @see [Resolution]
     */
    val resolution: Size = Resolution.RESOLUTION_720.size,

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
    constructor(
        resolution: Resolution,
        bitrate: Int,
        fps: Int,
        gopDuration: Float
    ) : this(resolution.size, bitrate, fps, gopDuration)

    internal fun toSdkConfig(): io.github.thibaultbee.streampack.data.VideoConfig {
        return io.github.thibaultbee.streampack.data.VideoConfig(
            startBitrate = bitrate,
            resolution = resolution,
            fps = fps,
            gopDuration = gopDuration
        )
    }

    companion object {
        internal fun fromSdkConfig(config: io.github.thibaultbee.streampack.data.VideoConfig): VideoConfig {
            return VideoConfig(
                bitrate = config.startBitrate,
                resolution = config.resolution,
                fps = config.fps,
                gopDuration = config.gopDuration
            )
        }

        private fun getDefaultBitrate(size: Size): Int {
            return when (size.width * size.height) {
                in 0..102_240 -> 800_000 // for 4/3 and 16/9 240p
                in 102_241..230_400 -> 1_000_000 // for 16/9 360p
                in 230_401..409_920 -> 1_300_000 // for 4/3 and 16/9 480p
                in 409_921..921_600 -> 2_000_000 // for 4/3 600p, 4/3 768p and 16/9 720p
                else -> 3_000_000 // for 16/9 1080p
            }
        }
    }
}