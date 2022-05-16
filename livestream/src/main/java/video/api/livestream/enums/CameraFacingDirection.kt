package video.api.livestream.enums

import android.content.Context
import io.github.thibaultbee.streampack.utils.getBackCameraList
import io.github.thibaultbee.streampack.utils.getFrontCameraList
import io.github.thibaultbee.streampack.utils.isBackCamera
import io.github.thibaultbee.streampack.utils.isFrontCamera

/**
 * Represents camera facing direction.
 */
enum class CameraFacingDirection {
    /**
     * The facing of the camera is opposite to that of the screen.
     */
    BACK,

    /**
     * The facing of the camera is the same as that of the screen.
     */
    FRONT;

    /**
     * Returns the camera id from the camera facing direction.
     *
     * @param context the application context
     * @return the camera id
     */
    fun toCameraId(context: Context): String {
        val cameraList = if (this == BACK) {
            context.getBackCameraList()
        } else {
            context.getFrontCameraList()
        }
        return cameraList[0]
    }

    companion object {
        /**
         * Returns the camera facing direction from the camera id.
         *
         * @param context the application context
         * @param cameraId the camera id
         * @return the camera facing direction
         */
        fun fromCameraId(context: Context, cameraId: String): CameraFacingDirection {
            return when {
                context.isFrontCamera(cameraId) -> FRONT
                context.isBackCamera(cameraId) -> BACK
                else -> throw IllegalArgumentException("Unknown camera id: $cameraId")
            }
        }
    }
}