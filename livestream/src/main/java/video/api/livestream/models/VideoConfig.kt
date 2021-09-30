package video.api.livestream.models

import video.api.livestream.enums.CameraFacingDirection
import video.api.livestream.enums.Resolution

data class VideoConfig(
    val bitrate: Int,
    val resolution: Resolution,
    val fps: Int,
    val camera: CameraFacingDirection
)