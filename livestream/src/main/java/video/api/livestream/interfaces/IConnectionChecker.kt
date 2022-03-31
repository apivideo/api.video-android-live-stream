package video.api.livestream.interfaces

/**
 * Connection callbacks interface.
 * Use it to manage a connection.
 */
interface IConnectionChecker {
    /**
     * Triggered when connection failed.
     *
     * @param reason reason of connection failure
     */
    fun onConnectionFailed(reason: String)

    /**
     * Triggered when connection is successful.
     */
    fun onConnectionSuccess()

    /**
     * Triggered on disconnect event.
     */
    fun onDisconnect()
}