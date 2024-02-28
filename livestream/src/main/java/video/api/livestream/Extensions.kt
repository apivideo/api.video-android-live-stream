package video.api.livestream

import android.util.Size
import kotlin.math.abs

/**
 * Add a slash at the end of a [String] only if it is missing.
 *
 * @return the given string with a trailing slash.
 */
fun String.addTrailingSlashIfNeeded(): String {
    return if (this.endsWith("/")) this else "$this/"
}

/**
 * Find the closest size to the given size in a list of sizes.
 */
fun List<Size>.closestTo(size: Size): Size =
    this.minBy { abs((it.width * it.height) - (size.width * size.height)) }