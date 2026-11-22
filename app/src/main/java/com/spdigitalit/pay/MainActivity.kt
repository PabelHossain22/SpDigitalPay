package com.spdigitalit.pay

import android.Manifest
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("SpPayConfig", Context.MODE_PRIVATE)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 60, 50, 60)
        }

        val title = TextView(this).apply {
            text = "SpDigitalPay Settings"
            textSize = 22f
            setPadding(0, 0, 0, 30)
        }
        layout.addView(title)

        // সার্ভার লিংক ইনপুট
        val urlLabel = TextView(this).apply { text = "Server Webhook URL:" }
        layout.addView(urlLabel)

        val urlInput = EditText(this).apply {
            hint = "https://yourdomain.com/api/verify.php"
            setText(prefs.getString("server_url", "https://spdigitalit.xyz/api/verify.php"))
            setPadding(20, 20, 20, 30)
        }
        layout.addView(urlInput)

        // সিক্রেট কি ইনপুট
        val keyLabel = TextView(this).apply { text = "API / Secret Key:" }
        layout.addView(keyLabel)

        val keyInput = EditText(this).apply {
            hint = "Enter Secret Key"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            setText(prefs.getString("secret_key", "MY_SECURE_PAY_KEY_123"))
            setPadding(20, 20, 20, 30)
        }
        layout.addView(keyInput)

        // সেভ বাটন
        val saveBtn = Button(this).apply {
            text = "Save Configuration"
            setOnClickListener {
                val url = urlInput.text.toString().trim()
                val key = keyInput.text.toString().trim()

                if (url.isEmpty() || key.isEmpty()) {
                    Toast.makeText(this@MainActivity, "URL এবং Key দুটিই পূরণ করুন", Toast.LENGTH_SHORT).show()
                } else {
                    prefs.edit()
                        .putString("server_url", url)
                        .putString("secret_key", key)
                        .apply()
                    Toast.makeText(this@MainActivity, "সেটিংস সফলভাবে সংরক্ষিত হয়েছে!", Toast.LENGTH_LONG).show()
                }
            }
        }
        layout.addView(saveBtn)

        setContentView(layout)

        // পারমিশন চাওয়া
        val permissions = arrayOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.POST_NOTIFICATIONS
        )
        requestPermissions(permissions, 101)
    }
}
