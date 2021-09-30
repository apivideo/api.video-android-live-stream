package video.api.livestream.enums

import android.util.Size

enum class Resolution(val size: Size) {
    RESOLUTION_240(Size(352, 240)),
    RESOLUTION_360(Size(640, 360)),
    RESOLUTION_480(Size(858, 480)),
    RESOLUTION_720(Size(1280, 720)),
    RESOLUTION_1080(Size(1920, 1080)),
    RESOLUTION_2160(Size(3860, 2160));

    override fun toString() = "${size.width}x${size.height}"

    companion object {
        fun valueOf(size: Size) =
            values().first { it.size.width == size.width && it.size.height == size.height }
    }
}