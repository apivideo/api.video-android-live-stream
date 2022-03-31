package video.api.livestream.views

import android.content.Context
import android.util.AttributeSet
import io.github.thibaultbee.streampack.views.AutoFitSurfaceView

/**
 * View where to display camera preview.
 */
class ApiVideoView: AutoFitSurfaceView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
}