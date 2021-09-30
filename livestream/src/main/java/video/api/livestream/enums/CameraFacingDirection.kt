package video.api.livestream.enums

import com.pedro.encoder.input.video.CameraHelper

enum class CameraFacingDirection(val facing: CameraHelper.Facing) {
    BACK(CameraHelper.Facing.BACK),
    FRONT(CameraHelper.Facing.FRONT)
}