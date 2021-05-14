package video.api.livestream_module

import android.content.Context
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.pedro.encoder.input.video.CameraHelper
import com.pedro.rtplibrary.base.Camera2Base
import com.pedro.rtplibrary.rtmp.RtmpCamera2
import com.pedro.rtplibrary.view.OpenGlView
import net.ossrs.rtmp.ConnectCheckerRtmp
import video.api.livestream_module.model.LiveStream
import java.io.IOException

enum class Resolution(val width: Int, val height: Int) {
    RESOLUTION_240(352, 240),
    RESOLUTION_360(480, 360),
    RESOLUTION_480(858, 480),
    RESOLUTION_720(1280, 720),
    RESOLUTION_1080(1920, 1080),
    RESOLUTION_2160(3860, 2160),
}

class ApiVideoLiveStream(
    private val context: Context,
    private val connectChecker: ConnectCheckerRtmp,
    private val openGlView: OpenGlView?,
    private val surfaceView: SurfaceView?,
) : SurfaceHolder.Callback {
    private var videoNeedsRefresh = false

    var videoResolution: Resolution = Resolution.RESOLUTION_720
        set(value) {
            field = value
            updateVideo()
        }

    var videoFps: Int = 30
        set(value) {
            field = value
            updateVideo()
        }

    var videoBitrate: Int = 4500 * 1000
        set(value) {
            field = value
            updateVideo()
        }

    var videoCamera: CameraHelper.Facing = CameraHelper.Facing.BACK
        set(value) {
            field = value
            if (field == CameraHelper.Facing.BACK && rtmpCamera2.isFrontCamera
                || field == CameraHelper.Facing.FRONT && !rtmpCamera2.isFrontCamera) {
                rtmpCamera2.switchCamera()
            }
        }

    var audioMuted: Boolean = false
        set(value) {
            field = value
            if (value) {
                rtmpCamera2.disableAudio()
            } else {
                rtmpCamera2.enableAudio()
            }
        }

    var stereo: Boolean = true
        set(value) {
            field = value
            updateAudio()
        }

    var echoCanceler: Boolean = false
        set(value) {
            field = value
            updateAudio()
        }

    var noiseSuppressor: Boolean = false
        set(value) {
            field = value
            updateAudio()
        }

    var audioBitrate: Int = 128 * 1000
        set(value) {
            field = value
            updateAudio()
        }

    var audioSampleRate: Int = 44100
        set(value) {
            field = value
            updateAudio()
        }

    private fun updateVideo(): Boolean {
        if (!rtmpCamera2.isStreaming) {
            videoNeedsRefresh = false
            if (rtmpCamera2.isOnPreview) {
                rtmpCamera2.stopPreview()
            }
            val res = rtmpCamera2.prepareVideo(
                videoResolution.width,
                videoResolution.height,
                videoFps,
                videoBitrate,
                CameraHelper.getCameraOrientation(context)
            )
            if (openGlView != null && openGlView.holder.surface.isValid) {
                rtmpCamera2.startPreview(videoCamera)
            }
            return res
        } else {
            videoNeedsRefresh = true
        }
        return false
    }

    private fun updateAudio(): Boolean {
        if (!rtmpCamera2.isStreaming) {
            return rtmpCamera2.prepareAudio(
                audioBitrate,
                audioSampleRate,
                stereo,
                echoCanceler,
                noiseSuppressor
            )
        }
        return false
    }


    private var rtmpCamera2: RtmpCamera2 = when {
        surfaceView != null -> {
            RtmpCamera2(surfaceView, connectChecker)
        }
        openGlView != null -> {
            openGlView.holder.addCallback(this)
            RtmpCamera2(openGlView, connectChecker)
        }
        else -> {
            RtmpCamera2(context, true, connectChecker)
        }
    }

    override fun surfaceCreated(p0: SurfaceHolder) {
    }

    override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
        rtmpCamera2.startPreview(videoCamera)
    }

    override fun surfaceDestroyed(p0: SurfaceHolder) {
        if (rtmpCamera2.isStreaming) {
            rtmpCamera2.stopStream()
        }
        rtmpCamera2.stopPreview()
    }

    fun startStreaming(
        streamKey: String,
        url: String?,
    ): Camera2Base {
        if (rtmpCamera2.isStreaming) {
            throw IOException("Stream is already started")
        }
        val audioReady = updateAudio()
        if (audioMuted) {
            rtmpCamera2.disableAudio()
        }
        val videoReady = updateVideo()
        if (audioReady && videoReady) {
            val rtmp = url ?: "rtmp://broadcast.api.video/s/"
            rtmpCamera2.startStream(rtmp + streamKey)
            return rtmpCamera2
        }
        throw IOException("Could not start RTMP streaming. audioReady=$audioReady, videoReady=$videoReady")
    }

    fun startStreaming(
        liveStream: LiveStream,
        url: String? = "rtmp://broadcast.api.video/s/",
    ): Camera2Base {
        return startStreaming(liveStream.streamKey!!, url)
    }

    fun stopStreaming() {
        rtmpCamera2.stopStream()
        if (videoNeedsRefresh) {
            updateVideo()
        }
    }
}