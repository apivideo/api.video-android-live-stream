package video.api.livestream_module.model

import okhttp3.HttpUrl
import org.json.JSONObject

open class QueryParams{
    enum class SortOrder(val value: String) {
        ASC("asc"),
        DESC("desc"),
    }

    val stringParams = mutableMapOf<String, String>()
    val setParams = mutableMapOf<String, Set<String>>()
    val mapParams = mutableMapOf<String, Map<String, String>>()

    fun setSingle(name: String, value: String) = apply {
        stringParams[name] = value
    }

    fun setSet(name: String, values: Set<String>) = apply {
        setParams[name] = values
    }

    fun setMap(name: String, values: Map<String, String>) = apply {
        mapParams[name] = values
    }

    fun sortBy(value: String, order: SortOrder = SortOrder.ASC) =
        setSingle("sortBy", value).setSingle("sortOrder", order.value)

    fun pageSize(size: Int) = setSingle("pageSize", size.toString())


    fun applyTo(urlBuilder: HttpUrl.Builder) {
        stringParams.forEach {
            urlBuilder.setQueryParameter(it.key, it.value)
        }
        setParams.forEach {
            val key = it.key
            urlBuilder.removeAllQueryParameters(key)
            it.value.forEach { value ->
                // TODO is it really necessary to NOT encode []?
                urlBuilder.addEncodedQueryParameter("$key[]", value)
            }
        }
        mapParams.forEach {
            val key = it.key
            urlBuilder.removeAllQueryParameters(key)
            it.value.forEach { pair ->
                // TODO is it really necessary to NOT encode []?
                urlBuilder.addEncodedQueryParameter("$key[${pair.key}]", pair.value)
            }
        }
    }
}