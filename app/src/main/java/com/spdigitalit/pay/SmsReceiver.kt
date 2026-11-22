package com.spdigitalit.pay

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import kotlin.concurrent.thread

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            for (sms in messages) {
                val sender = sms.displayOriginatingAddress ?: ""
                val body = sms.displayMessageBody ?: ""

                val allowedSenders = listOf("bKash", "Nagad", "NAGAD", "Rocket", "16216")
                if (allowedSenders.any { sender.contains(it, ignoreCase = true) }) {
                    sendToServer(context, sender, body)
                }
            }
        }
    }

    private fun sendToServer(context: Context, sender: String, body: String) {
        val prefs = context.getSharedPreferences("SpPayConfig", Context.MODE_PRIVATE)
        val serverUrl = prefs.getString("server_url", null)
        val secretKey = prefs.getString("secret_key", null)

        // যদি URL বা Key সেট করা না থাকে তবে রিকোয়েস্ট যাবে না
        if (serverUrl.isNullOrEmpty() || secretKey.isNullOrEmpty()) {
            return
        }

        thread {
            try {
                val client = OkHttpClient()
                val json = JSONObject().apply {
                    put("sender", sender)
                    put("raw_sms", body)
                    put("secret_key", secretKey)
                }
                val requestBody = json.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url(serverUrl)
                    .post(requestBody)
                    .build()

                client.newCall(request).execute().close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
