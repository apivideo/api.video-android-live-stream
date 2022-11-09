package video.api.livestream

import android.content.Context
import android.media.MediaFormat
import io.github.thibaultbee.streampack.streamers.helpers.AudioStreamerConfigurationHelper
import io.github.thibaultbee.streampack.streamers.helpers.CameraStreamerConfigurationHelper
import io.github.thibaultbee.streampack.streamers.helpers.VideoCameraStreamerConfigurationHelper
import io.github.thibaultbee.streampack.utils.getBackCameraList
import io.github.thibaultbee.streampack.utils.getCameraList
import io.github.thibaultbee.streampack.utils.getFrontCameraList

object ConfigurationHelper {
    private val helper = CameraStreamerConfigurationHelper.flvHelper
    val audio = AudioConfigurationHelper(helper.audio)
    val video = VideoStreamerConfigurationHelper(helper.video)
}

class AudioConfigurationHelper(private val audioHelper: AudioStreamerConfigurationHelper) {
    /**
     * Get supported bitrate range.
     *
     * @return bitrate range
     */
    fun getSupportedBitrates() =
        audioHelper.getSupportedBitrates(MediaFormat.MIMETYPE_AUDIO_AAC)

    /**
     * Get maximum supported number of channel by encoder.
     *
     * @return maximum number of channel supported by the encoder
     */
    fun getSupportedInputChannelRange() =
        audioHelper.getSupportedInputChannelRange(MediaFormat.MIMETYPE_AUDIO_AAC)

    /**
     * Get audio supported sample rates.
     *
     * @return sample rates list in Hz.
     */
    fun getSupportedSampleRates() =
        audioHelper.getSupportedSampleRates(MediaFormat.MIMETYPE_AUDIO_AAC)
}

class VideoStreamerConfigurationHelper(private val videoHelper: VideoCameraStreamerConfigurationHelper) {

    /**
     * Get supported bitrate range.
     *
     * @return bitrate range
     */
    fun getSupportedBitrates() =
        videoHelper.getSupportedBitrates(MediaFormat.MIMETYPE_VIDEO_AVC)

    /**
     * Get encoder supported resolutions range.
     *
     * @return pair that contains supported width ([Pair.first]) and supported height ([Pair.second]).
     */
    fun getSupportedResolutions() =
        videoHelper.getSupportedResolutions(MediaFormat.MIMETYPE_VIDEO_AVC)

    /**
     * Get camera supported resolutions that are also supported by the encoder.
     *
     * @param context application context
     * @return list of resolutions
     */
    fun getCameraSupportedResolutions(context: Context) =
        videoHelper.getSupportedResolutions(context, MediaFormat.MIMETYPE_VIDEO_AVC)


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
    ) = videoHelper.getSupportedFramerates(context, MediaFormat.MIMETYPE_VIDEO_AVC, cameraId)

    /**
     * Get cameras list
     *
     * @param context application context
     * @return list of camera
     */
    fun getCamerasList(context: Context) = context.getCameraList()

    /**
     * Get back cameras list
     *
     * @param context application context
     * @return list of back camera
     */
    fun getBackCamerasList(context: Context) = context.getBackCameraList()

    /**
     * Get front cameras list
     *
     * @param context application context
     * @return list of front camera
     */
    fun getFrontCamerasList(context: Context) = context.getFrontCameraList()
}
