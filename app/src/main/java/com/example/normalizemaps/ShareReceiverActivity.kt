\
    package com.example.normalizemaps

    import android.app.Activity
    import android.content.Intent
    import android.net.Uri
    import android.os.Bundle

    class ShareReceiverActivity : Activity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            val intent = intent
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
            finish()
        }
    }
