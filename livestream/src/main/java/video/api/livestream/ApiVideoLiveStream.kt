package video.api.livestream

import android.Manifest
import android.content.Context
import android.view.SurfaceHolder
import androidx.annotation.RequiresPermission
import com.pedro.encoder.input.video.CameraHelper
import com.pedro.rtmp.utils.ConnectCheckerRtmp
import com.pedro.rtplibrary.rtmp.RtmpCamera2
import com.pedro.rtplibrary.view.OpenGlView
import video.api.livestream.enums.CameraFacingDirection
import video.api.livestream.interfaces.IConnectionChecker
import video.api.livestream.models.AudioConfig
import video.api.livestream.models.VideoConfig

/**
 * Manages both livestream and camera preview.
 */
class ApiVideoLiveStream
/**
 * @param context application context
 * @param connectionChecker connection callbacks
 * @param audioConfig audio configuration
 * @param videoConfig video configuration
 * @param openGlView where to display preview. Could be null if you don't have a preview.
 */
@RequiresPermission(allOf = [Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA])
constructor(
    private val context: Context,
    private val connectionChecker: IConnectionChecker,
    private val audioConfig: AudioConfig,
    private val videoConfig: VideoConfig,
    openGlView: OpenGlView? = null
) {
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
            rtmpCamera2.startPreview(videoConfig.camera.facing)
        }

        /**
         * Calls when the surface size has been destroyed. This is for internal purpose only. Do not call it.
         */
        override fun surfaceDestroyed(holder: SurfaceHolder) {
            if (rtmpCamera2.isStreaming) {
                rtmpCamera2.stopStream()
            }
            rtmpCamera2.stopPreview()
        }
    }

    /**
     * Internal RTMP stream object
     */
    private val rtmpCamera2: RtmpCamera2 = when {
        openGlView != null -> {
            openGlView.holder.addCallback(surfaceCallback)
            RtmpCamera2(openGlView, connectCheckerRtmp)
        }
        else -> {
            RtmpCamera2(context, true, connectCheckerRtmp)
        }
    }

    /**
     * Get/set video bitrate during a stream in bps.
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
    }

    /**
     * Mute/Unmute a stream
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
         * @param [Boolean.true] to mute audio, [Boolean.false] to unmute audio.
         */
        set(value) {
            if (value) {
                rtmpCamera2.disableAudio()
            } else {
                rtmpCamera2.enableAudio()
            }
        }

    /**
     * Configures audio and video encoders.
     */
    private fun prepareEncoders() {
        rtmpCamera2.prepareAudio(
            audioConfig.bitrate,
            audioConfig.sampleRate,
            audioConfig.stereo,
            audioConfig.echoCanceler,
            audioConfig.noiseSuppressor
        )
        rtmpCamera2.prepareVideo(
            videoConfig.resolution.size.width,
            videoConfig.resolution.size.height,
            videoConfig.fps,
            videoConfig.bitrate,
            CameraHelper.getCameraOrientation(context)
        )
    }

    /**
     * Start a new RTMP stream.
     *
     * @param streamKey RTMP stream key. For security purpose, you must not expose it.
     * @param url RTML Url. Default value is api.video RTMP broadcast url.
     * @see [stopStreaming]
     */
    fun startStreaming(
        streamKey: String,
        url: String = context.getString(R.string.default_rtmp_url),
    ) {
        if (rtmpCamera2.isStreaming) {
            throw UnsupportedOperationException("Stream is already started")
        }

        prepareEncoders()
        rtmpCamera2.startStream(url + streamKey)
    }

    /**
     * Stops running stream.
     *
     * @see [startStreaming]
     */
    fun stopStreaming() =
        rtmpCamera2.stopStream()

}