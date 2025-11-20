package com.example.normalizemaps

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class ShareReceiverActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val text = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""

        // EDITED: extract location from any link
        val regex = Regex("""(-?\d+\.\d+),\s*(-?\d+\.\d+)""")
        val match = regex.find(text)

        val finalUrl = if (match != null) {
            val lat = match.groupValues[1]
            val lon = match.groupValues[2]
            "https://maps.google.com/?q=$lat,$lon"
        } else {
            // fallback: maybe a direct Google Maps link
            text
        }

        val i = Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl)).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            addCategory(Intent.CATEGORY_BROWSABLE)
            setPackage("com.google.android.apps.maps")  // mandatory: always opens Google Maps
        }

        startActivity(i)
/*
            if (intent.action == Intent.ACTION_SEND && intent.type == "text/plain") {
                val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
                if (sharedText != null) {
                    val regex = Regex("""(-?\d{1,3}\.\d+)[, ]+(-?\d{1,3}\.\d+)""")
                    val match = regex.find(sharedText)
                    if (match != null) {
                        val lat = match.groupValues[1]
                        val lng = match.groupValues[2]
                        val uri = Uri.parse("http://maps.google.com/?q=$lat,$lng")
                        val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                        mapIntent.setPackage("com.google.android.apps.maps")
                        mapIntent.resolveActivity(packageManager)?.let {
                            startActivity(mapIntent)
                        }
                    }
                }
            }

*/        finish()
    }
}