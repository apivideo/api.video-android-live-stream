package video.api.livestream.interfaces

/**
 * Connection callbacks interface.
 * Use it to manage a connection.
 */
interface IConnectionChecker {
    /**
     * Triggered when authentication failed.
     */
    fun onAuthError()

    /**
     * Triggered when authentication is successful.
     */
    fun onAuthSuccess()

    /**
     * Triggered when connection failed.
     *
     * @param reason reason of connection failure
     */
    fun onConnectionFailed(reason: String)

    /**
     * Triggered when stream has been started.
     *
     * @param url url address of stream
     */
    fun onConnectionStarted(url: String)

    /**
     * Triggered when connection is successful.
     */
    fun onConnectionSuccess()

    /**
     * Triggered on disconnect event.
     */
    fun onDisconnect()
}