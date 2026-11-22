package com.nexusai.assistant.data.remote

import com.nexusai.assistant.domain.model.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/** Talks to the NEXUS AI backend over HTTPS. Never logs tokens or message text. */
class BackendApi(baseUrl: String) {

    private val base = baseUrl.trimEnd('/')

    suspend fun health(): Boolean = withContext(Dispatchers.IO) {
        try {
            val conn = open("/health", "GET")
            try {
                conn.responseCode == 200
            } finally {
                conn.disconnect()
            }
        } catch (e: IOException) {
            false
        }
    }

    suspend fun chat(token: String, history: List<ChatMessage>, message: String): String =
        withContext(Dispatchers.IO) {
            val body = JSONObject()
                .put("session_id", "app")
                .put("message", message)
                .put(
                    "history",
                    JSONArray().apply {
                        history.forEach {
                            put(
                                JSONObject()
                                    .put("role", if (it.isUser) "user" else "assistant")
                                    .put("content", it.text)
                            )
                        }
                    }
                )

            val conn = open("/chat", "POST")
            try {
                conn.setRequestProperty("Authorization", "Bearer $token")
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true
                conn.outputStream.use { it.write(body.toString().toByteArray()) }

                val code = conn.responseCode
                val text = (if (code in 200..299) conn.inputStream else conn.errorStream)
                    ?.bufferedReader()?.use { it.readText() }.orEmpty()

                when {
                    code in 200..299 -> JSONObject(text).getString("reply")
                    code == 401 -> throw IOException("Login problem. Please log out and log in again.")
                    code == 429 -> throw IOException("Too many requests. Wait a minute and retry.")
                    else -> throw IOException(detail(text) ?: "Server error ($code). Please retry.")
                }
            } finally {
                conn.disconnect()
            }
        }

    private fun open(path: String, method: String): HttpURLConnection {
        val conn = URL(base + path).openConnection() as HttpURLConnection
        conn.requestMethod = method
        // The free server may need up to a minute to wake up after being idle.
        conn.connectTimeout = 30_000
        conn.readTimeout = 90_000
        return conn
    }

    private fun detail(body: String): String? =
        try {
            JSONObject(body).optString("detail").takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            null
        }
}
