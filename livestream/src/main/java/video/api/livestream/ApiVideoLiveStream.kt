package video.api.livestream

import android.Manifest
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.annotation.RequiresPermission
import io.github.thibaultbee.streampack.core.data.mediadescriptor.MediaDescriptor
import io.github.thibaultbee.streampack.core.data.mediadescriptor.UriMediaDescriptor
import io.github.thibaultbee.streampack.core.internal.endpoints.MediaSinkType
import io.github.thibaultbee.streampack.core.streamers.callbacks.DefaultCameraCallbackStreamer
import io.github.thibaultbee.streampack.core.streamers.interfaces.ICallbackStreamer
import io.github.thibaultbee.streampack.core.utils.extensions.isBackCamera
import io.github.thibaultbee.streampack.core.utils.extensions.isFrontCamera
import io.github.thibaultbee.streampack.ext.srt.data.mediadescriptor.SrtMediaDescriptor
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

    /**
     * Sets/gets audio configuration once you have created the a [ApiVideoLiveStream] instance.
     */
    var audioConfig: AudioConfig?
        get() = streamer.audioConfig?.let { AudioConfig.fromSdkConfig(it) }
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
        }

    /**
     * Sets/gets video configuration once you have created the a [ApiVideoLiveStream] instance.
     */
    var videoConfig: VideoConfig?
        get() {
            val videoConfig = streamer.videoConfig
            return if (videoConfig != null) {
                VideoConfig.fromSdkConfig(videoConfig)
            } else {
                null
            }
        }
        /**
         * Sets the new video configuration.
         * It will restart preview if resolution has been changed.
         * Encoders settings will be applied in next [startStreaming].
         *
         * @param value new video configuration
         */
        @RequiresPermission(Manifest.permission.CAMERA)
        set(value) {
            require(value != null) { "Video config must not be null" }
            if (isStreaming) {
                throw UnsupportedOperationException("You have to stop streaming first")
            }

            val mustRestartPreview = if (videoConfig?.fps != value.fps) {
                Log.i(
                    TAG,
                    "Frame rate has been changed from ${videoConfig?.fps} to ${value.fps}. Restarting preview."
                )
                true
            } else {
                false
            }

            if (mustRestartPreview) {
                stopPreview()
            }

            streamer.configure(value.toSdkConfig())

            if (mustRestartPreview) {
                try {
                    startPreview()
                } catch (t: Throwable) {
                    Log.i(TAG, "Can't start preview: ${t.message}")
                }
            }
        }

    private val internalListener = object : ICallbackStreamer.Listener {
        override fun onOpenFailed(t: Throwable) {
            connectionListener.onConnectionFailed(t.message ?: "Unknown error")
        }

        override fun onIsOpenChanged(isOpen: Boolean) {
            if (isOpen) {
                connectionListener.onConnectionSuccess()
            } else {
                connectionListener.onDisconnect()
            }
        }

        override fun onError(throwable: Throwable) {
            Log.e(TAG, "An error happened", throwable)
        }
    }

    private val streamer = DefaultCameraCallbackStreamer(
        context = context,
        enableMicrophone = true
    ).apply {
        addListener(internalListener)
    }

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
        get() = streamer.videoEncoder?.bitrate ?: 0
        /**
         * Set video bitrate.
         *
         * @param value video bitrate in bps
         */
        set(value) {
            streamer.videoEncoder?.bitrate = value
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
        try {
            apiVideoView.streamer = streamer
        } catch (e: Exception) {
            Log.w(TAG, "Can't set streamer to ApiVideoView: ${e.message}")
        }
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
        get() = streamer.audioSource?.isMuted ?: true
        /**
         * Set mute value.
         *
         * @param value [Boolean.true] to mute audio, [Boolean.false] to unmute audio.
         */
        set(value) {
            streamer.audioSource?.isMuted = value
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
        get() = streamer.videoSource.settings.zoom.zoomRatio
        /**
         * Set the zoom ratio.
         *
         * @param value the zoom ratio
         */
        set(value) {
            streamer.videoSource.settings.zoom.zoomRatio = value
        }

    /**
     * Start a new RTMP or SRT stream.
     *
     * Example of RTMP server url:
     * ```
     * rtmp://broadcast.api.video/s
     * ```
     *
     * Example of SRT server url:
     * ```
     * srt://broadcast.api.video:6200
     * ```
     * Other query parameters will be ignored.
     *
     * @param streamKey The RTMP stream key or SRT stream id. For security purpose, you must not expose it.
     * @param url The RTMP or SRT server Url. Default value (not set or null) is api.video RTMP broadcast url.
     * @see [stopStreaming]
     */
    fun startStreaming(
        streamKey: String,
        url: String = context.getString(R.string.default_server_url),
    ) {
        require(!isStreaming) { "Stream is already running" }
        require(streamKey.isNotEmpty()) { "Stream key must not be empty" }
        require(url.isNotEmpty()) { "Url must not be empty" }
        requireNotNull(audioConfig) { "Audio config must be set" }
        requireNotNull(videoConfig) { "Video config must be set" }

        val descriptor = if (url.startsWith("srt://")) {
            val uri = Uri.parse(url)
            SrtMediaDescriptor(
                host = requireNotNull(uri.host),
                port = uri.port,
                streamId = streamKey
            )
        } else {
            UriMediaDescriptor(url.addTrailingSlashIfNeeded() + streamKey)
        }

        startStreaming(descriptor)
    }

    /**
     * Start a new RTMP or SRT stream.
     *
     * Example of RTMP url:
     * ```
     * rtmp://broadcast.api.video/s/{streamKey}
     * ```
     *
     * Example of SRT url:
     * ```
     * srt://broadcast.api.video:6200?streamid={streamKey}
     * ```
     *
     * Get the stream key from the api.video dashboard or through the API.
     *
     * @param url The RTMP or SRT server Url with stream key (RTMP) or stream id (SRT).
     * @see [stopStreaming]
     */
    fun startStreaming(
        url: String
    ) {
        require(!isStreaming) { "Stream is already running" }
        require(url.isNotEmpty()) { "Url must not be empty" }
        requireNotNull(audioConfig) { "Audio config must be set" }
        requireNotNull(videoConfig) { "Video config must be set" }

        startStreaming(UriMediaDescriptor(url))
    }

    /**
     * Start a new RTMP or SRT stream.
     *
     * @param descriptor The media descriptor
     * @see [stopStreaming]
     */
    private fun startStreaming(
        descriptor: MediaDescriptor
    ) {
        require(!isStreaming) { "Stream is already running" }
        requireNotNull(audioConfig) { "Audio config must be set" }
        requireNotNull(videoConfig) { "Video config must be set" }

        require((descriptor.type.sinkType == MediaSinkType.RTMP) || (descriptor.type.sinkType == MediaSinkType.SRT)) { "URL must be RTMP or SRT" }

        try {
            streamer.startStream(descriptor)
        } catch (t: Throwable) {
            connectionListener.onConnectionFailed("$t")
            throw t
        }
    }

    /**
     * Stops running stream.
     *
     * @see [startStreaming]
     */
    fun stopStreaming() {
        streamer.stopStream()
        streamer.close()
    }

    /**
     * Whether you are streaming or not.
     *
     * @return true if you are streaming, false otherwise
     * @see [startStreaming]
     * @see [stopStreaming]
     */
    val isStreaming: Boolean
        get() = streamer.isStreaming

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
        permissionRequester(
            listOf(
                Manifest.permission.CAMERA,
            )
        ) {
            if (videoConfig == null) {
                Log.w(TAG, "Video config is not set")
                return@permissionRequester
            }
            try {
                apiVideoView.startPreview()
            } catch (t: Throwable) {
                Log.e(TAG, "Can't start preview: ${t.message}")
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
    fun stopPreview() = apiVideoView.stopPreview()

    /**
     * Release internal elements.
     *
     * You won't be able to use this instance after calling this method.
     */
    fun release() {
        streamer.release()
    }
}