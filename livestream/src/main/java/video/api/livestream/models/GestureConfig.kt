package video.api.livestream.models

class GestureConfig(
    /**
     * Enables/Disables all gestures
     */
    val enabled: Boolean = true,

    /**
     * Zoom/pinch gesture config
     */
    val zoom: ZoomConfig = ZoomConfig(),

    /**
     * Switch camera / double tap gesture config
     */
    val switchCamera: SwitchCameraConfig = SwitchCameraConfig()
)

class ZoomConfig (
    /**
     * Defines if the zoom/pinch gesture should be enabled or not
     */
    val enabled: Boolean = true,

    /**
     * Defines the zoom in multiplier.
     * The zoom in is linear.
     * The multiplier will be multiplied with the amount zoomed in.
     */
    val zoomInMultiplier: Float = 1f,

    /**
     * Defines the zoom out multiplier.
     * Zoom out already zooms by a percentage from when the gesture is begun.
     * The multiplier will be multiplied with the percentage zoomed out.
     */
    val zoomOutMultiplier: Float = 1f,
)

class SwitchCameraConfig (
    /**
     * Defines if the double tap / switch camera gesture should be enabled or not
     */
    val enabled: Boolean = true,
)