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
                    sendToServer(sender, body)
                }
            }
        }
    }

    private fun sendToServer(sender: String, body: String) {
        thread {
            try {
                val client = OkHttpClient()
                val json = JSONObject().apply {
                    put("sender", sender)
                    put("raw_sms", body)
                    put("secret_key", "MY_SECURE_PAY_KEY_123")
                }
                val requestBody = json.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("https://spdigitalit.xyz/api/verify.php")
                    .post(requestBody)
                    .build()

                client.newCall(request).execute().close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
