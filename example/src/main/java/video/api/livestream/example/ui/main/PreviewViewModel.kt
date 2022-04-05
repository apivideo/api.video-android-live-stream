package video.api.livestream.example.ui.main

import android.Manifest
import android.app.Application
import androidx.annotation.RequiresPermission
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import video.api.livestream.ApiVideoLiveStream
import video.api.livestream.example.ui.utils.Configuration
import video.api.livestream.enums.CameraFacingDirection
import video.api.livestream.enums.Resolution
import video.api.livestream.interfaces.IConnectionChecker
import video.api.livestream.models.AudioConfig
import video.api.livestream.models.VideoConfig
import video.api.livestream.views.ApiVideoView

class PreviewViewModel(application: Application) : AndroidViewModel(application),
    IConnectionChecker {
    private lateinit var liveStream: ApiVideoLiveStream
    private val configuration = Configuration(getApplication())

    val onError = MutableLiveData<String>()
    val onDisconnect = MutableLiveData<Boolean>()

    @RequiresPermission(allOf = [Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA])
    fun buildLiveStream(apiVideoView: ApiVideoView) {
        val audioConfig = AudioConfig(
            bitrate = configuration.audio.bitrate,
            sampleRate = configuration.audio.sampleRate,
            stereo = configuration.audio.numberOfChannels == 2,
            echoCanceler = configuration.audio.enableEchoCanceler,
            noiseSuppressor = configuration.audio.enableEchoCanceler
        )
        val videoConfig = VideoConfig(
            bitrate = configuration.video.bitrate,
            resolution = Resolution.valueOf(configuration.video.resolution),
            fps = configuration.video.fps,
        )
        liveStream =
            ApiVideoLiveStream(
                context = getApplication(),
                connectionChecker = this,
                initialAudioConfig = audioConfig,
                initialVideoConfig = videoConfig,
                apiVideoView = apiVideoView
            )
    }

    fun startStream() {
        try {
            liveStream.startStreaming(configuration.endpoint.streamKey, configuration.endpoint.url)
        } catch (e: Exception) {
            onError.postValue(e.message)
        }
    }

    fun stopStream() {
        liveStream.stopStreaming()
    }

    fun switchCamera() {
        if (liveStream.camera == CameraFacingDirection.BACK) {
            liveStream.camera = CameraFacingDirection.FRONT
        } else {
            liveStream.camera = CameraFacingDirection.BACK
        }
    }

    fun toggleMute() {
        liveStream.isMuted = !liveStream.isMuted
    }

    override fun onConnectionFailed(reason: String) {
        onError.postValue(reason)
    }

    override fun onConnectionSuccess() {
    }

    override fun onDisconnect() {
        onDisconnect.postValue(true)
    }
}