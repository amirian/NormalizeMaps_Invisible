package com.amirian.normalizemaps

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

        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
        val finalUrl = extractGoogleMapsUrl(sharedText)

        // ----- 4. Launch the map intent -----
        val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl)).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            addCategory(Intent.CATEGORY_BROWSABLE)
            // Try to open with Google Maps specifically
            setPackage("com.google.android.apps.maps")
        }

        try {
            if (mapIntent.resolveActivity(packageManager) != null) {
                startActivity(mapIntent)
            } else {
                // Fallback: If Google Maps is not installed, let the user choose an app
                val chooser = Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl)).apply {
                    addCategory(Intent.CATEGORY_BROWSABLE)
                }
                startActivity(Intent.createChooser(chooser, "Open map with"))
            }
        } catch (ex: ActivityNotFoundException) {
            // Ultimate fallback: Open in any browser if no map app is available
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl)))
            } catch (_: Exception) {
                // If even this fails, just finish
            }
        }

        finish()
    }
}

// Helper data class to manage regex patterns and their capture groups for lat/lon
private data class CoordinatePattern(val regex: Regex, val latGroup: Int = 1, val lonGroup: Int = 2)

/**
 * Parses a string to find geographic coordinates and returns a Google Maps URL.
 *
 * @param text The raw text, which may contain a location URL or coordinates.
 * @return A fully-formed Google Maps URL (e.g., "https://maps.google.com/?q=lat,lon") or a fallback URL.
 */
fun extractGoogleMapsUrl(text: String?): String {
    var processedText = text ?: ""

    // ----- 1. Decode and clean the input text -----
    try {
        for (i in 0 until 3) {
            val decoded = URLDecoder.decode(processedText, StandardCharsets.UTF_8.name())
            if (decoded == processedText) break
            processedText = decoded
        }
    } catch (e: Exception) {
        // Ignore decode errors, continue with the text as is
    }
    processedText = processedText.replace("%2C", ",").replace("%252C", ",").replace("%20", " ")

    // ----- 2. Define robust regex patterns -----
    // In a regular Kotlin string, backslashes must be escaped (`\\`).
    val num = "-?\\d+\\.\\d+"

    val patterns = listOf(
        // Balad: destination=lon,lat
        CoordinatePattern(Regex("destination=($num)\\s*,\\s*($num)"), latGroup = 2, lonGroup = 1),
        // Balad with fragment: #.../lat/lon
        CoordinatePattern(Regex("#\\d+(?:\\.\\d+)?/($num)/($num)")),
        // lon=...&lat=... (order-agnostic, non-greedy)
        CoordinatePattern(Regex("(?:lon|longitude)=($num).*?(?:lat|latitude)=($num)", RegexOption.IGNORE_CASE), latGroup = 2, lonGroup = 1),
        // lat=...&lon=... (order-agnostic, non-greedy)
        CoordinatePattern(Regex("(?:lat|latitude)=($num).*?(?:lon|longitude)=($num)", RegexOption.IGNORE_CASE)),
        // Neshan brief: #c<lat>-<lon>-...
        CoordinatePattern(Regex("#c($num)-($num)")),
        // Neshan: @lat,lon,...
        CoordinatePattern(Regex("@($num),\\s*($num)")),
        // Google Maps: query=lat,lon
        CoordinatePattern(Regex("query=($num),\\s*($num)")),
        // Neshan/other path-based: /lat,lon/
        CoordinatePattern(Regex("/($num),\\s*($num)(?:/|\$)")),
        // Waze: to=ll.lat,lon
        CoordinatePattern(Regex("ll\\.($num),\\s*($num)", RegexOption.IGNORE_CASE)),
        // Generic lat,lon (fallback)
        CoordinatePattern(Regex("($num),\\s*($num)"))
    )

    var lat: String? = null
    var lon: String? = null

    for (p in patterns) {
        val m = p.regex.find(processedText)
        if (m != null) {
            lat = m.groupValues[p.latGroup]
            lon = m.groupValues[p.lonGroup]
            break
        }
    }

    // ----- 3. Build the final URL -----
    return if (lat != null && lon != null) {
        "https://maps.google.com/?q=$lat,$lon"
    } else {
        // Fallback: if no coordinates found, try to extract any valid URL from the text
        val urlRegex = Regex("https?://[^\\s'\"]+", RegexOption.IGNORE_CASE)
        urlRegex.find(processedText)?.value ?: "https://maps.google.com"
    }
}
