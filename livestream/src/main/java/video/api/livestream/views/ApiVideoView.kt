package video.api.livestream.views

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.util.AttributeSet
import android.util.Size
import android.view.Surface
import android.view.SurfaceHolder
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.camera.viewfinder.CameraViewfinder
import androidx.camera.viewfinder.ViewfinderSurfaceRequest
import androidx.camera.viewfinder.populateFromCharacteristics
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import io.github.thibaultbee.streampack.logger.Logger
import io.github.thibaultbee.streampack.streamers.interfaces.ICameraStreamer
import io.github.thibaultbee.streampack.utils.getCameraCharacteristics
import video.api.livestream.closestTo

/**
 * View where to display camera preview.
 */
class ApiVideoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {
    private val cameraViewFinder = CameraViewfinder(context, attrs, defStyle)
    private var viewFinderSurfaceRequest: ViewfinderSurfaceRequest? = null
    private var isPreviewing = false

    internal var streamer: ICameraStreamer? = null
        /**
         * Set the [ICameraStreamer] to use.
         *
         * @param value the [ICameraStreamer] to use
         */
        set(value) {
            if (field != null) {
                Logger.w(TAG, "Streamer has already been set")
                return
            }
            field = value
            startPreviewIfReady(Size(width, height), true)
        }

    init {
        addView(
            cameraViewFinder, ViewGroup.LayoutParams(
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

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        startPreviewIfReady(Size(w, h), true)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopPreview()
    }

    /**
     * Stops the preview.
     */
    internal fun stopPreview() {
        post {
            if (isPreviewing) {
                stopPreviewInternal()
            }
        }
    }

    private fun stopPreviewInternal() {
        streamer?.stopPreview()
        viewFinderSurfaceRequest?.markSurfaceSafeToRelease()
        viewFinderSurfaceRequest = null
        isPreviewing = false
    }

    /**
     * Starts the preview.
     */
    internal fun startPreview() = startPreviewIfReady(Size(width, height), false)

    /**
     * Starts the preview if the view size is ready.
     *
     * @param targetViewSize the view size
     * @param shouldFailSilently true to fail silently
     */
    private fun startPreviewIfReady(targetViewSize: Size, shouldFailSilently: Boolean) {
        val streamer = streamer ?: run {
            Logger.w(TAG, "Streamer has not been set")
            return
        }
        if (width == 0 || height == 0) {
            Logger.w(TAG, "View size is not ready")
            return
        }

        try {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                throw SecurityException("Camera permission is needed to run this application")
            }

            val camera = streamer.camera
            Logger.i(TAG, "Starting on camera: $camera")
            Logger.d(TAG, "Target view size: $targetViewSize")

            post {
                if (isPreviewing) {
                    Logger.e(TAG, "Preview is already running")
                    return@post
                }
                isPreviewing = true

                val request = createRequest(targetViewSize, camera)
                viewFinderSurfaceRequest = request

                sendRequest(request, { surface ->
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        viewFinderSurfaceRequest?.markSurfaceSafeToRelease()
                        viewFinderSurfaceRequest = null
                        isPreviewing = false
                        Logger.e(
                            TAG,
                            "Camera permission is needed to run this application"
                        )
                    } else {
                        try {
                            streamer.startPreview(surface, camera)
                        } catch (e: Exception) {
                            viewFinderSurfaceRequest?.markSurfaceSafeToRelease()
                            viewFinderSurfaceRequest = null
                            isPreviewing = false
                            Logger.w(TAG, "Failed to start preview: $e", e)
                        }
                    }
                }, { t ->
                    viewFinderSurfaceRequest?.markSurfaceSafeToRelease()
                    viewFinderSurfaceRequest = null
                    isPreviewing = false
                    Logger.w(TAG, "Failed to get a Surface: $t", t)
                })
            }
        } catch (e: Exception) {
            if (shouldFailSilently) {
                Logger.w(TAG, e.toString(), e)
            } else {
                throw e
            }
        }
    }

    private fun createRequest(
        targetViewSize: Size,
        camera: String,
    ): ViewfinderSurfaceRequest {
        /**
         * Get the closest available preview size to the view size.
         */
        val previewSize = getPreviewOutputSize(
            context.getCameraCharacteristics(camera),
            targetViewSize,
            SurfaceHolder::class.java
        )

        Logger.d(TAG, "Selected preview size: $previewSize")

        val builder = ViewfinderSurfaceRequest.Builder(previewSize)
        builder.populateFromCharacteristics(context.getCameraCharacteristics(camera))

        return builder.build()
    }

    private fun sendRequest(
        request: ViewfinderSurfaceRequest,
        onSuccess: (Surface) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        val surfaceListenableFuture =
            cameraViewFinder.requestSurfaceAsync(request)

        Futures.addCallback(
            surfaceListenableFuture,
            object : FutureCallback<Surface> {
                override fun onSuccess(surface: Surface) {
                    onSuccess(surface)
                }

                override fun onFailure(t: Throwable) {
                    onFailure(t)
                }
            },
            ContextCompat.getMainExecutor(context)
        )
    }

    companion object {
        private const val TAG = "ApiVideoView"
    }

    /**
     * Returns the largest available PREVIEW size.
     */
    private fun <T> getPreviewOutputSize(
        characteristics: CameraCharacteristics,
        targetSize: Size,
        targetClass: Class<T>,
    ): Size {
        val allSizes =
            characteristics[CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP]!!.getOutputSizes(
                targetClass
            ).toList()

        // Get available sizes and sort them by area from largest to smallest
        val validSizes = allSizes
            .sortedWith(compareBy { it.height * it.width })
            .map { Size(it.width, it.height) }.reversed()

        // Then, get the largest output size that is smaller or equal than our max size
        return validSizes.closestTo(targetSize)
    }
}