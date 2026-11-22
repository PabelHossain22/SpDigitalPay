package com.spdigitalit.pay

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            val prefs = context.getSharedPreferences("SpPayConfig", Context.MODE_PRIVATE)

            // ব্যবহারকারীর সেট করা অনুমোদিত সেন্ডার তালিকা
            val rawSenders = prefs.getString("allowed_senders", "bKash,Nagad,NAGAD,Rocket,16216") ?: ""
            val allowedSenders = rawSenders.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }

            for (sms in messages) {
                val sender = sms.displayOriginatingAddress ?: ""
                val body = sms.displayMessageBody ?: ""
                val senderLower = sender.lowercase()

                // চেক করা প্রেরক অনুমোদিত তালিকায় আছে কি না
                val isAllowed = allowedSenders.any { senderLower.contains(it) }

                if (isAllowed) {
                    sendToServerAndLog(context, sender, body)
                }
            }
        }
    }

    private fun sendToServerAndLog(context: Context, sender: String, body: String) {
        val prefs = context.getSharedPreferences("SpPayConfig", Context.MODE_PRIVATE)
        val serverUrl = prefs.getString("server_url", "") ?: ""
        val secretKey = prefs.getString("secret_key", "") ?: ""

        val timeFormat = SimpleDateFormat("dd MMM, hh:mm:ss a", Locale.getDefault())
        val currentTime = timeFormat.format(Date())

        if (serverUrl.isEmpty() || secretKey.isEmpty()) {
            saveLog(context, currentTime, sender, "FAILED", "URL অথবা Secret Key কনফিগার করা নেই")
            return
        }

        thread {
            try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .build()

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

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    saveLog(context, currentTime, sender, "SUCCESS", "HTTP ${response.code}: সার্ভারে সফলভাবে পাঠানো হয়েছে")
                } else {
                    saveLog(context, currentTime, sender, "FAILED", "সার্ভার এরর কোড: ${response.code}")
                }
                response.close()
            } catch (e: Exception) {
                saveLog(context, currentTime, sender, "FAILED", e.localizedMessage ?: "কানেকশন টাইমআউট বা নেটওয়ার্ক সমস্যা")
            }
        }
    }

    private fun saveLog(context: Context, time: String, sender: String, status: String, msg: String) {
        val prefs = context.getSharedPreferences("SpPayConfig", Context.MODE_PRIVATE)
        val currentLogsJson = prefs.getString("system_logs", "[]") ?: "[]"

        try {
            val logArray = JSONArray(currentLogsJson)
            val newLog = JSONObject().apply {
                put("time", time)
                put("sender", sender)
                put("status", status)
                put("message", msg)
            }

            // নতুন লগ শুরুতে যোগ করা (সর্বোচ্চ ৫০টি লগ রাখা হবে)
            val updatedArray = JSONArray()
            updatedArray.put(newLog)
            for (i in 0 until minOf(logArray.length(), 49)) {
                updatedArray.put(logArray.get(i))
            }

            prefs.edit().putString("system_logs", updatedArray.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
