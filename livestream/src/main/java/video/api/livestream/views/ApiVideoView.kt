package video.api.livestream.views

import android.content.Context
import android.util.AttributeSet
import com.pedro.rtplibrary.view.OpenGlView

/**
 * View where to display camera preview.
 */
class ApiVideoView: OpenGlView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
}