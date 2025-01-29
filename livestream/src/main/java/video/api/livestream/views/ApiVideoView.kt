package video.api.livestream.views

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import io.github.thibaultbee.streampack.core.streamers.interfaces.ICameraStreamer
import io.github.thibaultbee.streampack.ui.views.PreviewView

/**
 * View where to display camera preview.
 */
class ApiVideoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {
    private val previewView = PreviewView(context, attrs, defStyle)

    internal var streamer: ICameraStreamer?
        get() = previewView.streamer
        /**
         * Set the [ICameraStreamer] to use.
         *
         * @param value the [ICameraStreamer] to use
         */
        set(value) {
            try {
                previewView.streamer = value
            } catch (t: Throwable) {
                Log.w(TAG, "Failed to set streamer: $t")
            }
        }

    init {
        addView(
            previewView, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
    }

    /**
     * Manually trigger measure & layout, as RN on Android skips those.
     * See comment on https://github.com/facebook/react-native/issues/17968#issuecomment-721958427
     */
    override fun requestLayout() {
        super.requestLayout()
        post(measureAndLayout)
    }

    private val measureAndLayout = Runnable {
        measure(
            MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        )
        layout(left, top, right, bottom)
    }

    internal fun stopPreview() {
        previewView.stopPreview()
    }

    internal fun startPreview() {
        previewView.startPreview()
    }

    companion object {
        private const val TAG = "ApiVideoView"
    }
}