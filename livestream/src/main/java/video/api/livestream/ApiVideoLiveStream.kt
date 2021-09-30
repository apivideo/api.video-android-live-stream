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

class ApiVideoLiveStream
@RequiresPermission(allOf = [Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA])
constructor(
    private val context: Context,
    private val connectionChecker: IConnectionChecker,
    private val audioConfig: AudioConfig,
    private val videoConfig: VideoConfig,
    openGlView: OpenGlView? = null
) : SurfaceHolder.Callback {
    private val connectCheckerRtmp = object : ConnectCheckerRtmp {
        override fun onAuthErrorRtmp() {
            connectionChecker.onAuthError()
        }

        override fun onAuthSuccessRtmp() {
            connectionChecker.onAuthSuccess()
        }

        override fun onConnectionFailedRtmp(reason: String) {
            resetEncoders()
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
            connectionChecker.onNewBitrate(bitrate)
        }
    }

    private val rtmpCamera2: RtmpCamera2 = when {
        openGlView != null -> {
            openGlView.holder.addCallback(this)
            RtmpCamera2(openGlView, connectCheckerRtmp)
        }
        else -> {
            RtmpCamera2(context, true, connectCheckerRtmp)
        }
    }

    var videoBitrate: Int
        get() = rtmpCamera2.bitrate
        set(value) {
            rtmpCamera2.setVideoBitrateOnFly(value)
        }

    var camera: CameraFacingDirection
        get() {
            return if (rtmpCamera2.cameraFacing == CameraHelper.Facing.FRONT) CameraFacingDirection.FRONT
            else CameraFacingDirection.BACK
        }
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

    var isMuted: Boolean
        get() = rtmpCamera2.isAudioMuted
        set(value) {
            if (value) {
                rtmpCamera2.disableAudio()
            } else {
                rtmpCamera2.enableAudio()
            }
        }

    override fun surfaceCreated(holder: SurfaceHolder) {
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        rtmpCamera2.startPreview(videoConfig.camera.facing)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        if (rtmpCamera2.isStreaming) {
            rtmpCamera2.stopStream()
        }
        rtmpCamera2.stopPreview()
    }

    private fun resetEncoders() {
        rtmpCamera2.stopStream()
    }

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

    fun stopStreaming() =
        rtmpCamera2.stopStream()

}