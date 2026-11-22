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
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("SpPayConfig", Context.MODE_PRIVATE)

        // পারমিশন চাওয়া
        val permissions = arrayOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.POST_NOTIFICATIONS
        )
        requestPermissions(permissions, 101)

        // যদি পূর্বে ইউআরএল ও কি সেভ করা থাকে, সরাসরি রানিং ভিউ দেখাবে
        val savedUrl = prefs.getString("server_url", null)
        val savedKey = prefs.getString("secret_key", null)

        if (!savedUrl.isNullOrEmpty() && !savedKey.isNullOrEmpty()) {
            showActiveRunningView()
        } else {
            showSettingsView()
        }
    }

    // ১. কনফিগারেশন পেজ
    private fun showSettingsView() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#121824"))
            setPadding(60, 80, 60, 60)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        val title = TextView(this).apply {
            text = "SpDigitalPay Settings"
            textSize = 24f
            setTextColor(Color.parseColor("#38BDF8"))
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 0, 0, 40)
        }
        root.addView(title)

        val urlLabel = TextView(this).apply {
            text = "SERVER WEBHOOK URL"
            textSize = 12f
            setTextColor(Color.parseColor("#94A3B8"))
            letterSpacing = 0.05f
            setPadding(0, 10, 0, 10)
        }
        root.addView(urlLabel)

        val urlInput = createStyledInput("https://spdigitalit.xyz/api/verify.php", false).apply {
            setText(prefs.getString("server_url", "https://spdigitalit.xyz/api/verify.php"))
        }
        root.addView(urlInput)

        val keyLabel = TextView(this).apply {
            text = "API / SECRET KEY"
            textSize = 12f
            setTextColor(Color.parseColor("#94A3B8"))
            letterSpacing = 0.05f
            setPadding(0, 30, 0, 10)
        }
        root.addView(keyLabel)

        val keyInput = createStyledInput("Enter Secret Key", true).apply {
            setText(prefs.getString("secret_key", ""))
        }
        root.addView(keyInput)

        val saveBtn = Button(this).apply {
            text = "Save & Activate"
            setTextColor(Color.WHITE)
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#0EA5E9"))
                cornerRadius = 16f
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 50, 0, 0)
            }
            setOnClickListener {
                val url = urlInput.text.toString().trim()
                val key = keyInput.text.toString().trim()

                if (url.isEmpty() || key.isEmpty()) {
                    showDialog("সতর্কতা", "URL এবং Secret Key দুটিই পূরণ করুন।")
                } else {
                    prefs.edit()
                        .putString("server_url", url)
                        .putString("secret_key", key)
                        .apply()

                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("সফল হয়েছে!")
                        .setMessage("কনফিগারেশন সংরক্ষিত হয়েছে। সার্ভিস ব্যাকগ্রাউন্ডে সক্রিয়।")
                        .setPositiveButton("ঠিক আছে") { _, _ ->
                            showActiveRunningView()
                        }
                        .setCancelable(false)
                        .show()
                }
            }
        }
        root.addView(saveBtn)

        setContentView(root)
    }

    // ২. সার্ভিস রানিং স্ট্যাটাস পেজ
    private fun showActiveRunningView() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#0F172A"))
            setPadding(60, 60, 60, 60)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        val iconContainer = ImageView(this).apply {
            setImageResource(android.R.drawable.stat_sys_upload_done)
            setColorFilter(Color.parseColor("#22C55E"))
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#14532D"))
                setStroke(4, Color.parseColor("#22C55E"))
            }
            setPadding(40, 40, 40, 40)
            layoutParams = LinearLayout.LayoutParams(220, 220).apply {
                setMargins(0, 0, 0, 40)
            }
        }
        root.addView(iconContainer)

        val statusTitle = TextView(this).apply {
            text = "Service is Running"
            textSize = 22f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }
        root.addView(statusTitle)

        val statusDesc = TextView(this).apply {
            text = "SpDigitalPay ব্যাকগ্রাউন্ডে সক্রিয় রয়েছে।\nইনকামিং এসএমএস স্বয়ংক্রিয়ভাবে সার্ভারে পাঠানো হবে।"
            textSize = 14f
            setTextColor(Color.parseColor("#94A3B8"))
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 50)
            lineSpacingMultiplier = 1.3f
        }
        root.addView(statusDesc)

        val editBtn = Button(this).apply {
            text = "Edit Settings"
            setTextColor(Color.parseColor("#38BDF8"))
            textSize = 14f
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#1E293B"))
                cornerRadius = 14f
                setStroke(2, Color.parseColor("#334155"))
            }
            setPadding(40, 20, 40, 20)
            setOnClickListener {
                showSettingsView()
            }
        }
        root.addView(editBtn)

        setContentView(root)
    }

    private fun createStyledInput(hintText: String, isPassword: Boolean): EditText {
        return EditText(this).apply {
            hint = hintText
            setHintTextColor(Color.parseColor("#475569"))
            setTextColor(Color.WHITE)
            textSize = 15f
            if (isPassword) {
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#1E293B"))
                cornerRadius = 14f
                setStroke(2, Color.parseColor("#334155"))
            }
            setPadding(35, 35, 35, 35)
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
