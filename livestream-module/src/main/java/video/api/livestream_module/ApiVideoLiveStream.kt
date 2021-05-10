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


class ApiVideoLiveStream(private val config: Config): SurfaceHolder.Callback {
    private var rtmpCamera2: RtmpCamera2 = when {
        config.surfaceView != null -> {
            RtmpCamera2(config.surfaceView, config.connectChecker)
        }
        config.openGlView != null -> {
            RtmpCamera2(config.openGlView, config.connectChecker)
        }
        else -> {
            RtmpCamera2(config.context,true, config.connectChecker)
        }
    }

    init {

        if (config.openGlView != null) {
            config.openGlView.holder.addCallback(this)
        }
    }

    override fun surfaceCreated(p0: SurfaceHolder) {
    }

    override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
        rtmpCamera2.startPreview()
    }

    override fun surfaceDestroyed(p0: SurfaceHolder) {
        if (rtmpCamera2.isStreaming) {
            rtmpCamera2.stopStream()
        }
        rtmpCamera2.stopPreview()
    }

    class Config private constructor(
        val audioBitrate: Int,
        val audioSampleRate: Int,
        val stereo: Boolean,
        val echoCanceler: Boolean,
        val noiseSuppressor: Boolean,
        val openGlView: OpenGlView?,
        val surfaceView: SurfaceView?,
        val context: Context,
        val connectChecker: ConnectCheckerRtmp,
        val videoBitrate: Int,
        val videoQuality: Quality,
        val videoFps: Int,
        val videoHardwareRotation: Boolean,
    ) {

        enum class Quality(val width: Int, val height: Int) {
            QUALITY_240(352, 240),
            QUALITY_480(858, 480),
            QUALITY_720(1280, 720),
            QUALITY_1080(1920, 1080),
            QUALITY_2160(3860, 2160),
        }

        data class Builder(
            val context: Context,
            val connectChecker: ConnectCheckerRtmp,
            var audioBitrate: Int = 128 * 1000,
            var audioSampleRate: Int = 44100,
            var audioStereo: Boolean = true,
            var audioCancelEcho: Boolean = false,
            var audioSuppressNoise: Boolean = false,
            var openGlView: OpenGlView? = null,
            var surfaceView: SurfaceView? = null,
            var videoBitrate: Int = 4500 * 1000,
            var videoQuality: Quality = Quality.QUALITY_720,
            var videoFps: Int = 25,
            var videoHardwareRotation: Boolean = false,
        ) {

            fun audioBitrate(audioBitrate: Int) =
                apply { this.audioBitrate = audioBitrate }

            fun audioSampleRate(audioSampleRate: Int) =
                apply { this.audioSampleRate = audioSampleRate }

            fun stereo(stereo: Boolean) =
                apply { this.audioStereo = stereo }

            fun echoCanceler(audioCancelEcho: Boolean) =
                apply { this.audioCancelEcho = audioCancelEcho }

            fun openGlView(openGlView: OpenGlView?) =
                apply { this.openGlView = openGlView }

            fun surfaceView(surfaceView: SurfaceView?) =
                apply { this.surfaceView = surfaceView }

            fun noiseSuppressor(audioSuppressNoise: Boolean) =
                apply { this.audioSuppressNoise = audioSuppressNoise }

            fun videoBitrate(videoBitrate: Int) =
                apply { this.videoBitrate = videoBitrate }

            fun videoQuality(videoQuality: Quality) =
                apply { this.videoQuality = videoQuality }

            fun videoFps(videoFps: Int) =
                apply { this.videoFps = videoFps }

            fun videoHardwareRotation(videoHardwareRotation: Boolean) =
                apply { this.videoHardwareRotation = videoHardwareRotation }

            fun build() = Config(
                audioBitrate,
                audioSampleRate,
                audioStereo,
                audioCancelEcho,
                audioSuppressNoise,
                openGlView,
                surfaceView,
                context,
                connectChecker,
                videoBitrate,
                videoQuality,
                videoFps,
                videoHardwareRotation,
            )
        }
    }

    fun startStreaming(
        streamKey: String,
        url: String?,
    ): Camera2Base {
        val audioReady = rtmpCamera2.prepareAudio(
            config.audioBitrate,
            config.audioSampleRate,
            config.stereo,
            config.echoCanceler,
            config.noiseSuppressor
        )
        val videoReady = rtmpCamera2.prepareVideo(
            config.videoQuality.width,
            config.videoQuality.height,
            config.videoFps,
            config.videoBitrate,
            config.videoHardwareRotation,
            CameraHelper.getCameraOrientation(config.context)
        )
        if (audioReady && videoReady) {
            val rtmp = url ?: "rtmp://broadcast.api.video/s/"
            rtmpCamera2.startStream( rtmp+streamKey)
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

    fun stopStreaming(){
        rtmpCamera2.stopStream()
    }
}