package video.api.livestream.enums

import android.util.Size

/**
 * Represents supported resolution.
 */
enum class Resolution(val size: Size) {
    RESOLUTION_240(Size(352, 240)),
    RESOLUTION_360(Size(640, 360)),
    RESOLUTION_480(Size(858, 480)),
    RESOLUTION_720(Size(1280, 720)),
    RESOLUTION_1080(Size(1920, 1080));

    /**
     * Prints a [Resolution].
     *
     * @return a string containing "${width}x${heigth}"
     */
    override fun toString() = "${size.width}x${size.height}"

    companion object {
        /**
         * Converts from a [Size] to a [Resolution].
         *
         * @param size describing width and height dimensions in pixels
         * @return corresponding [Resolution]
         */
        fun valueOf(size: Size) =
            values().first { it.size.width == size.width && it.size.height == size.height }
    }
}