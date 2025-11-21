package com.example.normalizemaps

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class ShareReceiverActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var text = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""

        // ----- 1. چند مرحله URL-decode (بعضی لینک‌ها دوبار encode شده‌اند) -----
        try {
            // try up to 3 decodes (safe)
            for (i in 0 until 3) {
                val decoded = URLDecoder.decode(text, StandardCharsets.UTF_8.name())
                if (decoded == text) break
                text = decoded
            }
        } catch (e: Exception) {
            // ignore decode errors, ادامه با متن خام
        }

        // برخی لینک‌ها بجای %2C از ٪252C (double-encoded) یا از escapeهای دیگر استفاده کرده‌اند.
        // جایگزینی صِرف برای اطمینان از کاماهای قابل شناسایی
        text = text.replace("%2C", ",").replace("%252C", ",").replace("%20", " ")

        // ----- 2. الگوهای قوی برای استخراج مختصات -----
        val patterns = listOf(
            // simple lat,lon
            Regex("""(-?\d+\.\d+)\s*,\s*(-?\d+\.\d+)"""),

            // Balad: latitude=...&longitude=...
            Regex("""latitude=([-0-9.]+)[^0-9.-]+longitude=([-0-9.]+)""", RegexOption.IGNORE_CASE),

            // Neshan: @lat,lon,...
            Regex("""@(-?\d+\.\d+),\s*(-?\d+\.\d+)"""),

            // Neshan or others with /.../lat,lon/... (origin/destination)
            Regex("""/(-?\d+\.\d+),\s*(-?\d+\.\d+)(?:/|$)"""),

            // Waze: to=ll.lat,lon  or to=ll.lat%2Clon (after decode should be ll.lat,lon)
            Regex("""ll\.(-?\d+\.\d+),\s*(-?\d+\.\d+)""", RegexOption.IGNORE_CASE),

            // Waze alternative: to=...lllat%2Clon or to=lllat,lon pattern
            Regex("""to=.*?(-?\d+\.\d+)[,%20]+(-?\d+\.\d+)""", RegexOption.IGNORE_CASE),

            // Neshan with zoom Format: #c{lat}-{lon}-{zoom}z-{pitch}p
            Regex("""#c(-?\d+\.\d+)-(-?\d+\.\d+)-""")
        )

        var lat: String? = null
        var lon: String? = null

        for (p in patterns) {
            val m = p.find(text)
            if (m != null) {
                lat = m.groupValues[1]
                lon = m.groupValues[2]
                break
            }
        }

        // ----- 3. ساخت URL نهایی -----
        val finalUrl = if (lat != null && lon != null) {
            // use https to be safe
            "https://maps.google.com/?q=$lat,$lon"
        } else {
            // fallback: اگر چیزی نبود، سعی کن از متن خام یک URL بسازی (در صورت معتبر بودن)
            // اگر متن خالی یا غیرقابل‌استفاده بود، نمایش خطا مناسب یا باز کردن chooser
            val maybeUrl = extractAnyUrl(text)
            if (maybeUrl != null) maybeUrl else "https://maps.google.com"
        }

        // ----- 4. سعی کن اول گوگل‌مپ رو باز کنی؛ در صورت fail، fallback به chooser -----
        val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl)).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            addCategory(Intent.CATEGORY_BROWSABLE)
            // setPackage می‌تواند باعث شود که اگر Google Maps نصب نبود، هیچ Activityی resolve نشود
            setPackage("com.google.android.apps.maps")
        }

        try {
            // اگر گوگل‌مپ نصب و resolve شد، باز شود
            if (mapIntent.resolveActivity(packageManager) != null) {
                startActivity(mapIntent)
            } else {
                // fallback: بدون setPackage — اجازه بده کاربر اپی را انتخاب کند یا مرورگر بشود
                val chooser = Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl)).apply {
                    addCategory(Intent.CATEGORY_BROWSABLE)
                }
                startActivity(Intent.createChooser(chooser, "Open map with"))
            }
        } catch (ex: ActivityNotFoundException) {
            // نهایی‌ترین fallback: باز کردن در مرورگر
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl)))
            } catch (_: Exception) {
                // اگر حتی این هم fail شد، فقط finish کن
            }
        }

        finish()
    }

    private fun extractAnyUrl(text: String): String? {
        // سریع URL extractor: دنبال http/https/... بگرد
        val urlRegex = Regex("""https?://[^\s'"]+""", RegexOption.IGNORE_CASE)
        val m = urlRegex.find(text)
        return m?.value
    }
}