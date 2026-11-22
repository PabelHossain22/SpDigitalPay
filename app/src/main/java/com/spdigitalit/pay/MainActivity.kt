package com.spdigitalit.pay

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import org.json.JSONArray

class MainActivity : Activity() {

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("SpPayConfig", Context.MODE_PRIVATE)

        val permissions = arrayOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.POST_NOTIFICATIONS
        )
        requestPermissions(permissions, 101)

        val savedUrl = prefs.getString("server_url", null)
        val savedKey = prefs.getString("secret_key", null)

        if (!savedUrl.isNullOrEmpty() && !savedKey.isNullOrEmpty()) {
            showActiveRunningView()
        } else {
            showSettingsView()
        }
    }

    // ১. সেটিংস পেজ (Webhook URL, Secret Key এবং Allowed Senders)
    private fun showSettingsView() {
        val scrollView = ScrollView(this).apply {
            setBackgroundColor(Color.parseColor("#0F172A"))
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 60, 50, 60)
        }
        scrollView.addView(root)

        val title = TextView(this).apply {
            text = "SpDigitalPay Settings"
            textSize = 22f
            setTextColor(Color.parseColor("#38BDF8"))
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 0, 0, 35)
        }
        root.addView(title)

        // Webhook URL
        root.addView(createLabel("SERVER WEBHOOK URL"))
        val urlInput = createStyledInput("https://spdigitalit.xyz/api/verify.php", false).apply {
            setText(prefs.getString("server_url", "https://spdigitalit.xyz/api/verify.php"))
        }
        root.addView(urlInput)

        // Secret Key
        root.addView(createLabel("API / SECRET KEY"))
        val keyInput = createStyledInput("Enter Secret Key", true).apply {
            setText(prefs.getString("secret_key", ""))
        }
        root.addView(keyInput)

        // Allowed Senders
        root.addView(createLabel("ALLOWED SENDER NAMES / NUMBERS (কমা দিয়ে লিখুন)"))
        val sendersInput = createStyledInput("bKash, Nagad, Rocket, 16216, 017xxxxxxxx", false).apply {
            setText(prefs.getString("allowed_senders", "bKash, Nagad, Rocket, 16216"))
        }
        root.addView(sendersInput)

        val note = TextView(this).apply {
            text = "উদাহরণ: bKash, Nagad, 01800000000 (এখানে যে নাম বা নম্বরগুলো থাকবে শুধু সেগুলোর এসএমএস ফরওয়ার্ড হবে)"
            textSize = 12f
            setTextColor(Color.parseColor("#64748B"))
            setPadding(0, 8, 0, 30)
        }
        root.addView(note)

        val saveBtn = Button(this).apply {
            text = "Save & Activate"
            setTextColor(Color.WHITE)
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#0EA5E9"))
                cornerRadius = 14f
            }
            setOnClickListener {
                val url = urlInput.text.toString().trim()
                val key = keyInput.text.toString().trim()
                val senders = sendersInput.text.toString().trim()

                if (url.isEmpty() || key.isEmpty() || senders.isEmpty()) {
                    showDialog("সতর্কতা", "সবগুলো ফিল্ড সঠিকভাবে পূরণ করুন।")
                } else {
                    prefs.edit()
                        .putString("server_url", url)
                        .putString("secret_key", key)
                        .putString("allowed_senders", senders)
                        .apply()

                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("সফল হয়েছে!")
                        .setMessage("কনফিগারেশন সেভ হয়েছে। সার্ভিস সক্রিয় রয়েছে।")
                        .setPositiveButton("ঠিক আছে") { _, _ ->
                            showActiveRunningView()
                        }
                        .setCancelable(false)
                        .show()
                }
            }
        }
        root.addView(saveBtn)

        setContentView(scrollView)
    }

    // ২. ড্যাশবোর্ড ও সিস্টেম লগ পেজ
    private fun showActiveRunningView() {
        val scrollView = ScrollView(this).apply {
            setBackgroundColor(Color.parseColor("#0B0F19"))
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 50, 40, 50)
        }
        scrollView.addView(root)

        // সার্ভিস স্ট্যাটাস হেডার কার্ড
        val statusCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#111827"))
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#111827"))
                cornerRadius = 20f
                setStroke(2, Color.parseColor("#1F2937"))
            }
            setPadding(40, 40, 40, 40)
        }

        val iconBadge = TextView(this).apply {
            text = "✓"
            textSize = 36f
            setTextColor(Color.parseColor("#22C55E"))
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#14532D"))
                setStroke(4, Color.parseColor("#22C55E"))
            }
            layoutParams = LinearLayout.LayoutParams(140, 140).apply {
                setMargins(0, 0, 0, 20)
            }
        }
        statusCard.addView(iconBadge)

        val statusText = TextView(this).apply {
            text = "Service is Running"
            textSize = 18f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
        }
        statusCard.addView(statusText)

        root.addView(statusCard)

        // অ্যাকশন বাটন সেকশন (Settings & Refresh)
        val btnRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 25, 0, 30)
            weightSum = 2f
        }

        val editBtn = Button(this).apply {
            text = "Edit Settings"
            setTextColor(Color.parseColor("#38BDF8"))
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#1E293B"))
                cornerRadius = 12f
            }
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginEnd = 10
            }
            setOnClickListener { showSettingsView() }
        }
        btnRow.addView(editBtn)

        val refreshBtn = Button(this).apply {
            text = "Clear Logs"
            setTextColor(Color.parseColor("#EF4444"))
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#1E293B"))
                cornerRadius = 12f
            }
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = 10
            }
            setOnClickListener {
                prefs.edit().putString("system_logs", "[]").apply()
                showActiveRunningView()
            }
        }
        btnRow.addView(refreshBtn)

        root.addView(btnRow)

        // সিস্টেম লগ হেডার
        val logHeader = TextView(this).apply {
            text = "SYSTEM LOGS"
            textSize = 14f
            setTextColor(Color.parseColor("#94A3B8"))
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.05f
            setPadding(0, 10, 0, 15)
        }
        root.addView(logHeader)

        // লগ আইটেমসমূহ রেন্ডার করা
        val logsJson = prefs.getString("system_logs", "[]") ?: "[]"
        val logArray = JSONArray(logsJson)

        if (logArray.length() == 0) {
            val emptyLog = TextView(this).apply {
                text = "এখনও কোনো এসএমএস লেনদেন লগ তৈরি হয়নি।"
                setTextColor(Color.parseColor("#475569"))
                textSize = 13f
                setPadding(0, 20, 0, 0)
            }
            root.addView(emptyLog)
        } else {
            for (i in 0 until logArray.length()) {
                val logObj = logArray.getJSONObject(i)
                val logTime = logObj.optString("time", "")
                val logSender = logObj.optString("sender", "")
                val logStatus = logObj.optString("status", "FAILED")
                val logMsg = logObj.optString("message", "")

                val logCard = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    background = GradientDrawable().apply {
                        setColor(Color.parseColor("#161F30"))
                        cornerRadius = 12f
                        setStroke(1, Color.parseColor("#1E293B"))
                    }
                    setPadding(30, 25, 30, 25)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, 0, 0, 18)
                    }
                }

                // স্ট্যাটাস ও টাইম রো
                val topRow = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                }

                val statusBadge = TextView(this).apply {
                    text = logStatus
                    textSize = 11f
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(Color.WHITE)
                    background = GradientDrawable().apply {
                        setColor(if (logStatus == "SUCCESS") Color.parseColor("#15803D") else Color.parseColor("#B91C1C"))
                        cornerRadius = 8f
                    }
                    setPadding(16, 6, 16, 6)
                }
                topRow.addView(statusBadge)

                val timeView = TextView(this).apply {
                    text = "  $logTime"
                    textSize = 12f
                    setTextColor(Color.parseColor("#94A3B8"))
                }
                topRow.addView(timeView)

                logCard.addView(topRow)

                val senderView = TextView(this).apply {
                    text = "প্রেরক: $logSender"
                    setTextColor(Color.parseColor("#E2E8F0"))
                    textSize = 13f
                    typeface = Typeface.DEFAULT_BOLD
                    setPadding(0, 12, 0, 4)
                }
                logCard.addView(senderView)

                val msgView = TextView(this).apply {
                    text = logMsg
                    setTextColor(Color.parseColor("#94A3B8"))
                    textSize = 12f
                }
                logCard.addView(msgView)

                root.addView(logCard)
            }
        }

        setContentView(scrollView)
    }

    private fun createLabel(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 11f
            setTextColor(Color.parseColor("#94A3B8"))
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 20, 0, 8)
        }
    }

    private fun createStyledInput(hintText: String, isPassword: Boolean): EditText {
        return EditText(this).apply {
            hint = hintText
            setHintTextColor(Color.parseColor("#475569"))
            setTextColor(Color.WHITE)
            textSize = 14f
            if (isPassword) {
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#1E293B"))
                cornerRadius = 12f
                setStroke(2, Color.parseColor("#334155"))
            }
            setPadding(30, 28, 30, 28)
        }
    }

    private fun showDialog(title: String, msg: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(msg)
            .setPositiveButton("ঠিক আছে", null)
            .show()
    }
}
