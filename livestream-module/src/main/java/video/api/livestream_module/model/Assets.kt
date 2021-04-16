package video.api.livestream_module.model

import okhttp3.HttpUrl
import org.json.JSONObject

class Assets(
    val body: JSONObject
) {
    val hls: HttpUrl?
        get() = body.getHttpUrlOrNull("hls")

    val iFrame: String?
        get() = body.getStringOrNull("iframe")

    val player: HttpUrl?
        get() = body.getHttpUrlOrNull("player")

    val thumbnail: HttpUrl?
        get() = body.getHttpUrlOrNull("thumbnail")

    val mp4: HttpUrl?
        get() = body.getHttpUrlOrNull("mp4")

}