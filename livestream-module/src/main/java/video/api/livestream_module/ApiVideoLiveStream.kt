package video.api.livestream_module

import android.content.Context
import android.util.Log
import android.view.SurfaceView
import com.pedro.encoder.input.video.CameraHelper
import com.pedro.rtplibrary.base.Camera1Base
import com.pedro.rtplibrary.rtmp.RtmpCamera1
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



    fun start(
        liveStream: LiveStream,
        surfaceView: SurfaceView,
        context: Context,
        connectChecker: ConnectCheckerRtmp
    ): Camera1Base =
        start(liveStream.streamKey!!, surfaceView, context, connectChecker)

    fun start(
        liveStream: LiveStream,
        context: Context,
        connectChecker: ConnectCheckerRtmp
    ): Camera1Base =
        start(liveStream.streamKey!!, null, context, connectChecker)

    fun start(
        streamKey: String,
        surfaceView: SurfaceView?,
        context: Context,
        connectChecker: ConnectCheckerRtmp
    ): Camera1Base {
        val rtmpCamera1: RtmpCamera1 = if(surfaceView != null){
            RtmpCamera1(surfaceView, connectChecker)
        }else{
            RtmpCamera1(context, connectChecker)
        }


        val audioReady = rtmpCamera1.prepareAudio(
            config.audioBitrate,
            config.audioSampleRate,
            config.stereo,
            config.echoCanceler,
            config.noiseSuppressor
        )

        val videoReady = rtmpCamera1.prepareVideo(
            config.videoQuality.width,
            config.videoQuality.height,
            config.videoFps,
            config.videoBitrate,
            config.videoHardwareRotation,
            CameraHelper.getCameraOrientation(context)
        )

        if (audioReady && videoReady) {
            rtmpCamera1.startStream("rtmp://broadcast.api.video/s/$streamKey")

            return rtmpCamera1
        }

        throw IOException("Could not start RTMP streaming. audioReady=$audioReady, videoReady=$videoReady")
    }
}