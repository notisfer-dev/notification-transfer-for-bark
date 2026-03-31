package dev.yakitori.barkforwarder.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request

class BarkPushClient(private val httpClient: OkHttpClient) {
    suspend fun sendEncrypted(serverUrl: String, deviceKey: String, ciphertext: String, iv: String) {
        withContext(Dispatchers.IO) {
            val body = FormBody.Builder()
                .add("device_key", deviceKey)
                .add("ciphertext", ciphertext)
                .add("iv", iv)
                .build()

            val request = Request.Builder()
                .url(serverUrl)
                .post(body)
                .build()

            httpClient.newCall(request).execute().use { response ->
                check(response.isSuccessful) {
                    "Bark push failed with HTTP ${response.code}"
                }
            }
        }
    }
}

