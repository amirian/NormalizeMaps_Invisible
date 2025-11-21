package com.example.normalizemaps

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class ShareReceiverActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val text = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
        //Version 1.1
        // Type A: simple "lat,lon"
        val r1 = Regex("""(-?\d+\.\d+)\s*,\s*(-?\d+\.\d+)""")

        // Type B: Balad (latitude=..&longitude=..)
        val r2 = Regex("""latitude=([-0-9.]+).+longitude=([-0-9.]+)""")

        // Type C: Neshan (@lat,lon,zoom)
        val r3 = Regex("""@(-?\d+\.\d+),\s*(-?\d+\.\d+)""")

        // Type D: Neshan origin/destination/.../lat,lon
        val r4 = Regex("""/(-?\d+\.\d+),\s*(-?\d+\.\d+)""")

        // Type E: Waze (...to=ll.lat,lon)
        val r5 = Regex("""ll\.(-?\d+\.\d+),\s*(-?\d+\.\d+)""")

        val patterns = listOf(r1, r2, r3, r4, r5)

        var lat: String? = null
        var lon: String? = null

        // پیدا کردن اولین مختصات معتبر از تمام regexها
        for (p in patterns) {
            val m = p.find(text)
            if (m != null) {
                lat = m.groupValues[1]
                lon = m.groupValues[2]
                break
            }
        }

        // ===== 2. تبدیل نهایی =====

        val finalUrl =
            if (lat != null && lon != null)
                "https://maps.google.com/?q=$lat,$lon"
            else
                text // fallback بدون مختصات

        // ===== 3. اجرای Google Maps =====

/* Version 1.0
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
*/
        val i = Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl)).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            addCategory(Intent.CATEGORY_BROWSABLE)
            setPackage("com.google.android.apps.maps")  // mandatory: always opens Google Maps
        }

        startActivity(i)
/* Version 0
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