package video.api.livestream

import android.content.Context
import android.media.MediaFormat
import io.github.thibaultbee.streampack.core.internal.endpoints.composites.CompositeEndpoint
import io.github.thibaultbee.streampack.core.internal.endpoints.composites.muxers.flv.FlvMuxerInfo
import io.github.thibaultbee.streampack.core.streamers.infos.AudioStreamerConfigurationInfo
import io.github.thibaultbee.streampack.core.streamers.infos.CameraStreamerConfigurationInfo
import io.github.thibaultbee.streampack.core.streamers.infos.VideoCameraStreamerConfigurationInfo
import io.github.thibaultbee.streampack.core.utils.extensions.backCameras
import io.github.thibaultbee.streampack.core.utils.extensions.cameras
import io.github.thibaultbee.streampack.core.utils.extensions.frontCameras

object ConfigurationHelper {
    private val helper =
        CameraStreamerConfigurationInfo(CompositeEndpoint.EndpointInfo(FlvMuxerInfo))
    val audio = AudioConfigurationHelper(helper.audio)
    val video = VideoStreamerConfigurationHelper(helper.video)
}

class AudioConfigurationHelper(private val audioInfo: AudioStreamerConfigurationInfo) {
    /**
     * Get supported bitrate range.
     *
     * @return bitrate range
     */
    fun getSupportedBitrates() =
        audioInfo.getSupportedBitrates(MediaFormat.MIMETYPE_AUDIO_AAC)

    /**
     * Get maximum supported number of channel by encoder.
     *
     * @return maximum number of channel supported by the encoder
     */
    fun getSupportedInputChannelRange() =
        audioInfo.getSupportedInputChannelRange(MediaFormat.MIMETYPE_AUDIO_AAC)

    /**
     * Get audio supported sample rates.
     *
     * @return sample rates list in Hz.
     */
    fun getSupportedSampleRates() =
        audioInfo.getSupportedSampleRates(MediaFormat.MIMETYPE_AUDIO_AAC)
}

class VideoStreamerConfigurationHelper(private val videoInfo: VideoCameraStreamerConfigurationInfo) {

    /**
     * Get supported bitrate range.
     *
     * @return bitrate range
     */
    fun getSupportedBitrates() =
        videoInfo.getSupportedBitrates(MediaFormat.MIMETYPE_VIDEO_AVC)

    /**
     * Get encoder supported resolutions range.
     *
     * @return pair that contains supported width ([Pair.first]) and supported height ([Pair.second]).
     */
    fun getSupportedResolutions() =
        videoInfo.getSupportedResolutions(MediaFormat.MIMETYPE_VIDEO_AVC)

    /**
     * Get camera supported resolutions that are also supported by the encoder.
     *
     * @param context application context
     * @return list of resolutions
     */
    fun getCameraSupportedResolutions(context: Context) =
        videoInfo.getSupportedResolutions(context, MediaFormat.MIMETYPE_VIDEO_AVC)


    /**
     * Get camera supported frame rate that are also supported by the encoder.
     *
     * @param context application context
     * @param cameraId camera id
     * @return list of frame rate
     */
    fun getSupportedFrameRates(
        context: Context,
        cameraId: String
    ) = videoInfo.getSupportedFramerates(context, MediaFormat.MIMETYPE_VIDEO_AVC, cameraId)

    /**
     * Get cameras list
     *
     * @param context application context
     * @return list of camera
     */
    fun getCamerasList(context: Context) = context.cameras

    /**
     * Get back cameras list
     *
     * @param context application context
     * @return list of back camera
     */
    fun getBackCamerasList(context: Context) = context.backCameras

    /**
     * Get front cameras list
     *
     * @param context application context
     * @return list of front camera
     */
    fun getFrontCamerasList(context: Context) = context.frontCameras
}
