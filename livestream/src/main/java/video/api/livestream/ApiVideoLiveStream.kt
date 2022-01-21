package video.api.livestream

import android.Manifest
import android.content.Context
import android.util.Log
import android.view.SurfaceHolder
import androidx.annotation.RequiresPermission
import com.pedro.encoder.input.video.CameraHelper
import com.pedro.rtmp.utils.ConnectCheckerRtmp
import com.pedro.rtplibrary.rtmp.RtmpCamera2
import video.api.livestream.enums.CameraFacingDirection
import video.api.livestream.interfaces.IConnectionChecker
import video.api.livestream.models.AudioConfig
import video.api.livestream.models.VideoConfig
import video.api.livestream.views.ApiVideoView
import java.lang.IllegalArgumentException

/**
 * Manages both livestream and camera preview.
 */
class ApiVideoLiveStream
/**
 * @param context application context
 * @param connectionChecker connection callbacks
 * @param initialAudioConfig initial audio configuration. Could be change later with [audioConfig] field.
 * @param initialVideoConfig initial video configuration. Could be change later with [videoConfig] field.
 * @param initialCamera initial camera. Could be change later with [camera] field.
 * @param apiVideoView where to display preview. Could be null if you don't have a preview.
 */
@RequiresPermission(allOf = [Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA])
constructor(
    private val context: Context,
    private val connectionChecker: IConnectionChecker,
    private val initialAudioConfig: AudioConfig,
    private val initialVideoConfig: VideoConfig,
    private val initialCamera: CameraFacingDirection = CameraFacingDirection.BACK,
    private val apiVideoView: ApiVideoView? = null
) {

    /**
     * Set/get audio configuration once you have created the a [ApiVideoLiveStream] instance.
     */
    var audioConfig: AudioConfig = initialAudioConfig

    /**
     * Set/get video configuration once you have created the a [ApiVideoLiveStream] instance.
     */
    var videoConfig: VideoConfig = initialVideoConfig
        /**
         * Set new video configuration.
         * It will restart preview if resolution has been changed.
         * Encoders settings will be applied in next [startStreaming].
         *
         * @param value new video configuration
         */
        set(value) {
            if (isStreaming) {
                throw UnsupportedOperationException("You have to stop streaming first")
            }
            if (videoConfig.resolution != value.resolution) {
                Log.i(
                    this::class.simpleName,
                    "Resolution has been changed from ${videoConfig.resolution} to ${value.resolution}. Restarting preview."
                )
                stopPreview()
                rtmpCamera2.startPreview(
                    rtmpCamera2.cameraFacing,
                    value.resolution.size.width,
                    value.resolution.size.height
                )
            }
            field = value
        }

    /**
     * [ConnectCheckerRtmp] implementation.
     */
    private val connectCheckerRtmp = object : ConnectCheckerRtmp {
        override fun onAuthErrorRtmp() {
            connectionChecker.onAuthError()
        }

        override fun onAuthSuccessRtmp() {
            connectionChecker.onAuthSuccess()
        }

        override fun onConnectionFailedRtmp(reason: String) {
            rtmpCamera2.stopStream()
            connectionChecker.onConnectionFailed(reason)
        }

        override fun onConnectionStartedRtmp(rtmpUrl: String) {
            connectionChecker.onConnectionStarted(rtmpUrl)
        }

        override fun onConnectionSuccessRtmp() {
            connectionChecker.onConnectionSuccess()
        }

        override fun onDisconnectRtmp() {
            connectionChecker.onDisconnect()
        }

        override fun onNewBitrateRtmp(bitrate: Long) {
        }
    }

    /**
     * [SurfaceHolder.Callback] implementation.
     */
    private val surfaceCallback = object : SurfaceHolder.Callback {
        /**
         * Calls when the provided surface is created. This is for internal purpose only. Do not call it.
         */
        override fun surfaceCreated(holder: SurfaceHolder) {
        }

        /**
         * Calls when the surface size has been changed. This is for internal purpose only. Do not call it.
         */
        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            rtmpCamera2.startPreview(initialCamera.facing)
        }

        /**
         * Calls when the surface size has been destroyed. This is for internal purpose only. Do not call it.
         */
        override fun surfaceDestroyed(holder: SurfaceHolder) {
            stopStreaming()
            stopPreview()
        }
    }

    /**
     * Internal RTMP stream object
     */
    private val rtmpCamera2: RtmpCamera2 = when {
        apiVideoView != null -> {
            apiVideoView.holder.addCallback(surfaceCallback)
            RtmpCamera2(apiVideoView, connectCheckerRtmp)
        }
        else -> {
            RtmpCamera2(
                context,
                true,
                connectCheckerRtmp
            ).apply { startPreview(initialCamera.facing) }
        }
    }

    /**
     * Get/set video bitrate during a streaming in bps.
     * Value will be reset to provided [VideoConfig.bitrate] for a new stream.
     */
    var videoBitrate: Int
        /**
         * Get video bitrate.
         *
         * @return video bitrate in bps
         */
        get() = rtmpCamera2.bitrate
        /**
         * Set video bitrate.
         *
         * @param value video bitrate in bps
         */
        set(value) {
            rtmpCamera2.setVideoBitrateOnFly(value)
        }

    /**
     * Get/set current camera facing direction.
     */
    var camera: CameraFacingDirection
        /**
         * Get current camera facing direction.
         *
         * @return facing direction of the current camera
         */
        get() {
            return if (rtmpCamera2.cameraFacing == CameraHelper.Facing.FRONT) CameraFacingDirection.FRONT
            else CameraFacingDirection.BACK
        }
        /**
         * Set current camera facing direction.
         *
         * @param value camera facing direction
         */
        set(value) {
            if (((value == CameraFacingDirection.BACK) && (rtmpCamera2.cameraFacing == CameraHelper.Facing.FRONT))
                || ((value == CameraFacingDirection.FRONT) && (rtmpCamera2.cameraFacing == CameraHelper.Facing.BACK))
            ) {
                rtmpCamera2.switchCamera()
            }
        }

    init {
        prepareEncoders()
        rtmpCamera2.setLogs(false)
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
        get() = rtmpCamera2.isAudioMuted
        /**
         * Set mute value.
         *
         * @param value [Boolean.true] to mute audio, [Boolean.false] to unmute audio.
         */
        set(value) {
            if (value) {
                rtmpCamera2.disableAudio()
            } else {
                rtmpCamera2.enableAudio()
            }
        }

    /**
     * Configures audio encoders.
     */
    private fun prepareAudioEncoders() {
        rtmpCamera2.prepareAudio(
            audioConfig.bitrate,
            audioConfig.sampleRate,
            audioConfig.stereo,
            audioConfig.echoCanceler,
            audioConfig.noiseSuppressor
        )
    }

    /**
     * Configures video encoders.
     */
    private fun prepareVideoEncoders() {
        rtmpCamera2.prepareVideo(
            videoConfig.resolution.size.width,
            videoConfig.resolution.size.height,
            videoConfig.fps,
            videoConfig.bitrate,
            CameraHelper.getCameraOrientation(context)
        )
    }

    private fun prepareEncoders() {
        prepareVideoEncoders()
        prepareAudioEncoders()
    }

    /**
     * Start a new RTMP stream.
     *
     * @param streamKey RTMP stream key. For security purpose, you must not expose it.
     * @param url RTML Url. Default value (not set or null) is api.video RTMP broadcast url.
     * @see [stopStreaming]
     */
    fun startStreaming(
        streamKey: String,
        url: String = context.getString(R.string.default_rtmp_url),
    ) {
        if (isStreaming) {
            throw UnsupportedOperationException("Stream is already started")
        }
        if (streamKey.isEmpty()) {
            throw IllegalArgumentException("Stream key must not be empty")
        }

        prepareEncoders()
        rtmpCamera2.startStream(url.addTrailingSlashIfNeeded() + streamKey)
    }

    /**
     * Stops running stream.
     *
     * @see [startStreaming]
     */
    fun stopStreaming() =
        rtmpCamera2.stopStream()

    /**
     * Check the streaming state.
     *
     * @return true if you are streaming, false otherwise
     * @see [startStreaming]
     * @see [stopStreaming]
     */
    val isStreaming: Boolean
        get() = rtmpCamera2.isStreaming

    /**
     * Starts camera preview of [camera].
     *
     * The surface provided in the constructor already manages [startPreview] and [stopPreview].
     * Use this method only if you need to explicitly start preview.
     *
     * @see [stopPreview]
     */
    fun startPreview() = rtmpCamera2.startPreview()

    /**
     * Stops camera preview.
     *
     * The surface provided in the constructor already manages [startPreview] and [stopPreview].
     * Use this method only if you need to explicitly stop preview.
     *
     * @see [startPreview]
     */
    fun stopPreview() = rtmpCamera2.stopPreview()
}