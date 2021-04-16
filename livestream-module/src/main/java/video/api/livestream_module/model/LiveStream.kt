package video.api.livestream_module.model

import org.json.JSONObject

class LiveStream(val body: JSONObject = JSONObject()) {
    constructor(name: String, public: Boolean) : this(JSONObject().put("name", name).put("public", public))

    class QueryParams(body: JSONObject) : video.api.livestream_module.model.QueryParams() {
        // Filter
        fun name(value: String) = setSingle("name", value) as QueryParams
        fun streamKey(value: String) = setSingle("streamKey", value) as QueryParams

        // Sort
        fun sortByCreatedAt(order: SortOrder = SortOrder.ASC) =
            sortBy("createdAt", order) as QueryParams

        fun sortByName(order: SortOrder = SortOrder.ASC) = sortBy("name", order) as QueryParams

        fun sortByUpdatedAt(order: SortOrder = SortOrder.ASC) =
            sortBy("updatedAt", order) as QueryParams
    }

    // Read

    val liveStreamId: String?
        get() = body.getStringOrNull("liveStreamId")

    val streamKey: String?
        get() = body.getStringOrNull("streamKey")

    val broadcasting: Boolean
        get() = body.optBoolean("broadcasting", false)

    val assets: Assets?
        get() = if (body.has("assets")) {
            Assets(body.getJSONObject("assets"))
        } else {
            null
        }


    // Read/write

    var name: String?
        get() = body.getStringOrNull("name")
        set(value) {
            body.put("name", value)
        }

    var playerId: String?
        get() = body.getStringOrNull("playerId")
        set(value) {
            body.put("playerId", value)
        }

    var record: Boolean?
        get() = body.optBoolean("record", false)
        set(value) {
            body.put("record", value)
        }

    var public: Boolean?
        get() = body.optBoolean("public", false)
        set(value) {
            body.put("public", value)
        }

}