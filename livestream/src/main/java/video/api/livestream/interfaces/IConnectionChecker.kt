package video.api.livestream.interfaces

interface IConnectionChecker {
    fun onAuthError()
    fun onAuthSuccess()
    fun onConnectionFailed(reason: String)
    fun onConnectionStarted(url: String)
    fun onConnectionSuccess()
    fun onDisconnect()
    fun onNewBitrate(bitrate: Long)
}