package video.api.livestream

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.SurfaceHolder
import androidx.annotation.RequiresPermission
import io.github.thibaultbee.streampack.error.StreamPackError
import io.github.thibaultbee.streampack.ext.rtmp.streamers.CameraRtmpLiveStreamer
import io.github.thibaultbee.streampack.listeners.OnConnectionListener
import io.github.thibaultbee.streampack.listeners.OnErrorListener
import io.github.thibaultbee.streampack.utils.*
import io.github.thibaultbee.streampack.views.getPreviewOutputSize
import kotlinx.coroutines.*
import video.api.livestream.enums.CameraFacingDirection
import video.api.livestream.interfaces.IConnectionListener
import video.api.livestream.models.AudioConfig
import video.api.livestream.models.VideoConfig
import video.api.livestream.views.ApiVideoView


/**
 * @param context application context
 * @param apiVideoView where to display preview. Could be null if you don't have a preview.
 * @param connectionListener connection callbacks
 * @param initialAudioConfig initial audio configuration. Could be change later with [audioConfig] field.
 * @param initialVideoConfig initial video configuration. Could be change later with [videoConfig] field.
 * @param initialCameraPosition initial camera. Could be change later with [cameraPosition] field.
 * @param permissionRequester permission requester. Called when permissions are required. Always call [onGranted] when permissions are granted.
 */
@RequiresPermission(allOf = [Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA])
fun ApiVideoLiveStream(
    context: Context,
    apiVideoView: ApiVideoView,
    connectionListener: IConnectionListener,
    initialAudioConfig: AudioConfig? = null,
    initialVideoConfig: VideoConfig? = null,
    initialCameraPosition: CameraFacingDirection = CameraFacingDirection.BACK,
    permissionRequester: (List<String>, onGranted: () -> Unit) -> Unit = { _, onGranted -> onGranted() }
): ApiVideoLiveStream {
    return ApiVideoLiveStream(
        context,
        apiVideoView,
        connectionListener,
        permissionRequester
    ).apply {
        audioConfig = initialAudioConfig
        videoConfig = initialVideoConfig
        cameraPosition = initialCameraPosition
    }
}

/**
 * Manages both livestream and camera preview.
 */
class ApiVideoLiveStream
/**
 * @param context application context
 * @param apiVideoView where to display preview. Could be null if you don't have a preview.
 * @param connectionListener connection callbacks
 * @param permissionRequester permission requester. Called when permissions are required. Always call [onGranted] when permissions are granted.
 */
@RequiresPermission(allOf = [Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA])
constructor(
    private val context: Context,
    private val apiVideoView: ApiVideoView,
    private val connectionListener: IConnectionListener,
    private val permissionRequester: (List<String>, onGranted: () -> Unit) -> Unit = { _, onGranted -> onGranted() }
) {
    companion object {
        private const val TAG = "ApiVideoLiveStream"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    /**
     * Set/get audio configuration once you have created the a [ApiVideoLiveStream] instance.
     */
    var audioConfig: AudioConfig? = null
        @RequiresPermission(Manifest.permission.RECORD_AUDIO)
        set(value) {
            require(value != null) { "Audio config must not be null" }
            if (isStreaming) {
                throw UnsupportedOperationException("You have to stop streaming first")
            }
            permissionRequester(
                listOf(
                    Manifest.permission.RECORD_AUDIO,
                )
            ) {
                streamer.configure(value.toSdkConfig())
            }

            field = value
        }

    /**
     * Set/get video configuration once you have created the a [ApiVideoLiveStream] instance.
     */
    var videoConfig: VideoConfig? = null
        /**
         * Set new video configuration.
         * It will restart preview if resolution has been changed.
         * Encoders settings will be applied in next [startStreaming].
         *
         * @param value new video configuration
         */
        @RequiresPermission(allOf = [Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO])
        set(value) {
            require(value != null) { "Audio config must not be null" }
            if (isStreaming) {
                throw UnsupportedOperationException("You have to stop streaming first")
            }
            if ((videoConfig?.resolution != value.resolution) || (videoConfig?.fps != value.fps)) {
                Log.i(
                    this::class.simpleName,
                    "Resolution has been changed from ${videoConfig?.resolution} to ${value.resolution} or fps from ${videoConfig?.fps} to ${value.fps}. Restarting preview."
                )
                stopPreview()
                streamer.configure(value.toSdkConfig())
                try {
                    startPreview()
                } catch (e: UnsupportedOperationException) {
                    Log.i(TAG, "Can't start preview: ${e.message}")
                }
            }
            field = value
        }

    private val internalConnectionListener = object : OnConnectionListener {
        override fun onFailed(message: String) {
            connectionListener.onConnectionFailed(message)
        }

        override fun onLost(message: String) {
            connectionListener.onDisconnect()
        }

        override fun onSuccess() {
            connectionListener.onConnectionSuccess()
        }
    }

    private val errorListener = object : OnErrorListener {
        override fun onError(error: StreamPackError) {
            _isStreaming = false
            Log.e(TAG, "An error happened", error)
        }
    }

    /**
     * [SurfaceHolder.Callback] implementation.
     */
    private val surfaceCallback = object : SurfaceHolder.Callback {
        /**
         * Calls when the provided surface is created. This is for internal purpose only. Do not call it.
         */
        @SuppressLint("MissingPermission")
        override fun surfaceCreated(holder: SurfaceHolder) {
            try {
                startPreview()
            } catch (e: UnsupportedOperationException) {
                Log.i(TAG, "Can't start preview: ${e.message}")
            }
        }

        /**
         * Calls when the surface size has been changed. This is for internal purpose only. Do not call it.
         */
        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) =
            Unit

        /**
         * Calls when the surface size has been destroyed. This is for internal purpose only. Do not call it.
         */
        override fun surfaceDestroyed(holder: SurfaceHolder) {
            stopStreaming()
            stopPreview()
        }
    }

    private val streamer = CameraRtmpLiveStreamer(
        context = context,
        enableAudio = true,
        initialOnErrorListener = errorListener,
        initialOnConnectionListener = internalConnectionListener
    )

    /**
     * Get/set video bitrate during a streaming in bps.
     * Value will be reset to provided [VideoConfig.startBitrate] for a new stream.
     */
    var videoBitrate: Int
        /**
         * Get video bitrate.
         *
         * @return video bitrate in bps
         */
        get() = streamer.settings.video.bitrate
        /**
         * Set video bitrate.
         *
         * @param value video bitrate in bps
         */
        set(value) {
            streamer.settings.video.bitrate = value
        }

    /**
     * Get/set current camera facing direction.
     *
     * @see [camera]
     */
    var cameraPosition: CameraFacingDirection
        /**
         * Get current camera facing direction.
         *
         * @return facing direction of the current camera
         */
        get() = CameraFacingDirection.fromCameraId(context, streamer.camera)
        /**
         * Set current camera facing direction.
         *
         * @param value camera facing direction
         */
        set(value) {
            if (((value == CameraFacingDirection.BACK) && (context.isFrontCamera(streamer.camera)))
                || ((value == CameraFacingDirection.FRONT) && (context.isBackCamera(streamer.camera)))
            ) {
                permissionRequester(
                    listOf(
                        Manifest.permission.CAMERA,
                    )
                ) {
                    streamer.camera = value.toCameraId(context)
                }
            }
        }

    /**
     * Get/set current camera.
     *
     * @see [cameraPosition]
     */
    var camera: String
        /**
         * Gets current camera.
         * It is often like "0" for back camera and "1" for front camera.
         *
         * @return the current camera
         */
        get() = streamer.camera
        /**
         * Sets current camera.
         *
         * @param value the current camera
         */
        set(value) {
            streamer.camera = value
        }

    init {
        apiVideoView.holder.addCallback(surfaceCallback)
    }

    /**
     * Mute/Unmute microphone
     */
    var isMuted: Boolean
        /**
         * Get mute value.
         *
         * @return [Boolean.true] if audio is muted, [Boolean.false] if audio is not muted.
         */
        get() = streamer.settings.audio.isMuted
        /**
         * Set mute value.
         *
         * @param value [Boolean.true] to mute audio, [Boolean.false] to unmute audio.
         */
        set(value) {
            streamer.settings.audio.isMuted = value
        }


    /**
     * Set/get the zoom ratio.
     */
    var zoomRatio: Float
        /**
         * Get the zoom ratio.
         *
         * @return the zoom ratio
         */
        get() = streamer.settings.camera.zoom.zoomRatio
        /**
         * Set the zoom ratio.
         *
         * @param value the zoom ratio
         */
        set(value) {
            streamer.settings.camera.zoom.zoomRatio = value
        }

    /**
     * Start a new RTMP stream.
     *
     * @param streamKey RTMP stream key. For security purpose, you must not expose it.
     * @param url RTMP Url. Default value (not set or null) is api.video RTMP broadcast url.
     * @see [stopStreaming]
     */
    fun startStreaming(
        streamKey: String,
        url: String = context.getString(R.string.default_rtmp_url),
    ) {
        require(!isStreaming) { "Stream is already running" }
        require(streamKey.isNotEmpty()) { "Stream key must not be empty" }
        require(url.isNotEmpty()) { "Url must not be empty" }
        require(audioConfig != null) { "Audio config must be set" }
        require(videoConfig != null) { "Video config must be set" }

        scope.launch {
            withContext(context = Dispatchers.IO) {
                try {
                    streamer.connect(url.addTrailingSlashIfNeeded() + streamKey)
                    try {
                        streamer.startStream()
                        _isStreaming = true
                    } catch (e: Exception) {
                        streamer.disconnect()
                        connectionListener.onConnectionFailed("$e")
                        throw e
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to start stream", e)
                }
            }
        }
    }

    /**
     * Stops running stream.
     *
     * @see [startStreaming]
     */
    fun stopStreaming() {
        val isConnected = streamer.isConnected
        scope.launch {
            withContext(context = Dispatchers.IO) {
                streamer.stopStream()
                streamer.disconnect()
                if (isConnected) {
                    connectionListener.onDisconnect()
                }
                _isStreaming = false
            }
        }
    }


    /**
     * Hack for private setter of [isStreaming].
     */
    private var _isStreaming: Boolean = false

    /**
     * Check the streaming state.
     *
     * @return true if you are streaming, false otherwise
     * @see [startStreaming]
     * @see [stopStreaming]
     */
    val isStreaming: Boolean
        get() = _isStreaming

    /**
     * Starts camera preview of [cameraPosition].
     *
     * The surface provided in the constructor already manages [startPreview] and [stopPreview].
     * Use this method only if you need to explicitly start preview.
     *
     * @see [stopPreview]
     */
    @RequiresPermission(Manifest.permission.CAMERA)
    fun startPreview() {
        if (apiVideoView.display == null) {
            throw UnsupportedOperationException("display is null")
        }
        // Selects appropriate preview size and configures view finder
        streamer.camera.let {
            val previewSize = getPreviewOutputSize(
                apiVideoView.display,
                context.getCameraCharacteristics(it),
                SurfaceHolder::class.java
            )
            Log.d(
                TAG,
                "View finder size: ${apiVideoView.width} x ${apiVideoView.height}"
            )
            Log.d(TAG, "Selected preview size: $previewSize")

            // To ensure that size is set, initialize camera in the view's thread
            apiVideoView.post {
                apiVideoView.setAspectRatio(previewSize.width, previewSize.height)

                permissionRequester(
                    listOf(
                        Manifest.permission.CAMERA,
                    )
                ) {
                    if (videoConfig == null) {
                        Log.w(TAG, "Video config is not set")
                        return@permissionRequester
                    }
                    streamer.startPreview(
                        apiVideoView.holder.surface,
                        it
                    )
                }
            }
        }
    }

    /**
     * Stops camera preview.
     *
     * The surface provided in the constructor already manages [startPreview] and [stopPreview].
     * Use this method only if you need to explicitly stop preview.
     *
     * @see [startPreview]
     */
    fun stopPreview() = streamer.stopPreview()

    /**
     * Release internal elements.
     *
     * You won't be able to use this instance after calling this method.
     */
    fun release() {
        streamer.release()
        scope.cancel()
    }
}