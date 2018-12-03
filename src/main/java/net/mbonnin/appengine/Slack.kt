package net.mbonnin.appengine

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import okio.Okio
import java.net.HttpURLConnection
import java.net.URL

object Slack {
    private val moshi = Moshi.Builder().build()!!

    private val mapAdapter by lazy {
        val type = Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
        moshi.adapter<Map<String, Any>>(type)
    }

    fun sendMessage(map: Map<String, Any>, incomingWebHook: String) {
        val url = URL(incomingWebHook)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.doInput = true
        connection.doOutput = true

        connection.addRequestProperty("Content-Type", "application/json")
        val sink = Okio.buffer(Okio.sink(connection.getOutputStream()))
        sink.use {
            mapAdapter.toJson(it, map)
            sink.flush()
        }

        val status = connection.responseCode
        if (status != HttpURLConnection.HTTP_OK) {
            val response = connection.getInputStream().bufferedReader().use {
                it.readText()
            }
            System.out.println("cannot send to slack: $response")
        }
    }
}