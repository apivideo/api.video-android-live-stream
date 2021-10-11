package video.api.livestream.models

import video.api.livestream.enums.CameraFacingDirection
import video.api.livestream.enums.Resolution

/**
 * Describes video configuration.
 */
data class VideoConfig(
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
)