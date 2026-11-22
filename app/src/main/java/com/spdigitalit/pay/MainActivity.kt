package com.spdigitalit.pay

import android.Manifest
import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tv = TextView(this).apply {
            text = "SpDigitalPay Service is Active!\nListening for SMS payments."
            textSize = 18f
            setPadding(40, 60, 40, 60)
        }
        setContentView(tv)

        val permissions = arrayOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.POST_NOTIFICATIONS
        )
        requestPermissions(permissions, 101)
    }
}
