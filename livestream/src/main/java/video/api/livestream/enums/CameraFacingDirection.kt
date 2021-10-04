package video.api.livestream.enums

import com.pedro.encoder.input.video.CameraHelper

/**
 * Represents camera facing direction.
 */
enum class CameraFacingDirection(val facing: CameraHelper.Facing) {
    /**
     * The facing of the camera is opposite to that of the screen.
     */
    BACK(CameraHelper.Facing.BACK),

    /**
     * The facing of the camera is the same as that of the screen.
     */
    FRONT(CameraHelper.Facing.FRONT)
}