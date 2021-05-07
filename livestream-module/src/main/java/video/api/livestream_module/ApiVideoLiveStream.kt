package video.api.livestream_module

import android.content.Context
import android.os.Build
import android.view.SurfaceView
import androidx.annotation.RequiresApi
import com.pedro.encoder.input.video.CameraHelper
import com.pedro.rtplibrary.base.Camera2Base
import com.pedro.rtplibrary.rtmp.RtmpCamera2
import com.pedro.rtplibrary.view.OpenGlView
import net.ossrs.rtmp.ConnectCheckerRtmp
import video.api.livestream_module.model.LiveStream
import java.io.IOException

class ApiVideoLiveStream(private val config: Config) {
    class Config private constructor(
        val audioBitrate: Int,
        val audioSampleRate: Int,
        val stereo: Boolean,
        val echoCanceler: Boolean,
        val noiseSuppressor: Boolean,
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
            var audioBitrate: Int = 128 * 1000,
            var audioSampleRate: Int = 44100,
            var audioStereo: Boolean = true,
            var audioCancelEcho: Boolean = false,
            var audioSuppressNoise: Boolean = false,
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
                videoBitrate,
                videoQuality,
                videoFps,
                videoHardwareRotation,
            )
        }
    }



    // SurfaceView
    // LiveStream
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun start(
        liveStream: LiveStream,
        url: String? = null,
        surfaceView: SurfaceView,
        context: Context,
        connectChecker: ConnectCheckerRtmp
    ): Camera2Base =
        start(liveStream.streamKey!!,url, surfaceView, null, context, connectChecker)

    // LiveStream
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun start(
        liveStream: LiveStream,
        url: String? = null,
        context: Context,
        connectChecker: ConnectCheckerRtmp
    ): Camera2Base =
        start(liveStream.streamKey!!,url,null, null, context, connectChecker)

    //streamKey
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun start(
        streamKey: String,
        url: String? = null,
        context: Context,
        connectChecker: ConnectCheckerRtmp
    ): Camera2Base =
        start(streamKey,url, null,null, context, connectChecker)

    // SurfaceView
    // LiveStream
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun start(
        liveStream: LiveStream,
        url: String? = null,
        openGleView: OpenGlView,
        context: Context,
        connectChecker: ConnectCheckerRtmp
    ): Camera2Base =
        start(liveStream.streamKey!!,url, null, openGleView, context, connectChecker)


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun start(
        streamKey: String,
        url: String? = "rtmp://broadcast.api.video/s/",
        surfaceView: SurfaceView?,
        openGlView: OpenGlView?,
        context: Context,
        connectChecker: ConnectCheckerRtmp
    ): Camera2Base {
        val rtmpCamera2: RtmpCamera2 = when {
            surfaceView != null -> {
                RtmpCamera2(surfaceView, connectChecker)
            }
            openGlView != null -> {
                RtmpCamera2(openGlView, connectChecker)
            }
            else -> {
                RtmpCamera2(context,true,connectChecker)
            }
        }

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
            CameraHelper.getCameraOrientation(context)
        )

        if (audioReady && videoReady) {
            rtmpCamera2.startStream(url+streamKey)

            return rtmpCamera2
        }

        throw IOException("Could not start RTMP streaming. audioReady=$audioReady, videoReady=$videoReady")
    }
}