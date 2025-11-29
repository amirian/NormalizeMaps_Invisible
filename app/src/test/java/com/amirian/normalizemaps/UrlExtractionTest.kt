package com.amirian.normalizemaps

import org.junit.Assert.assertEquals
import org.junit.Test

class UrlExtractionTest {

    @Test
    fun testBaladLocationWithLonLatQuery() {
        val url = "https://balad.ir/location?longitude=51.4368099812198240&latitude=35.6854533607580040&zoom=17.000000"
        val expected = "https://maps.google.com/?q=35.6854533607580040,51.4368099812198240"
        assertEquals(expected, extractGoogleMapsUrl(url))
    }

    @Test
    fun testBaladLocationWithLatLonQuery() {
        val url = "https://balad.ir/location?latitude=35.6854533607580040&longitude=51.4368099812198240&zoom=17.000000"
        val expected = "https://maps.google.com/?q=35.6854533607580040,51.4368099812198240"
        assertEquals(expected, extractGoogleMapsUrl(url))
    }

    @Test
    fun testBaladDirectionsWithDestination() {
        val url = "https://balad.ir/directions/driving?destination=51.403106%2C35.690335&destination_title=some_text"
        val expected = "https://maps.google.com/?q=35.690335,51.403106"
        assertEquals(expected, extractGoogleMapsUrl(url))
    }

    @Test
    fun testBaladDirectionsWithDestinationAndFragment() {
        // The destination= pattern should match first, ignoring the fragment
        val url = "https://balad.ir/directions/driving?destination=51.403106%2C35.690335&destination_title=some_text&destination_setter=POI#15/35.69033/51.40311"
        val expected = "https://maps.google.com/?q=35.690335,51.403106"
        assertEquals(expected, extractGoogleMapsUrl(url))
    }

    @Test
    fun testNeshanUrl() {
        val url = "https://neshan.org/maps/@35.801876,51.431299,17.0z,0.0p/routing/car/origin/35.801876,51.431299/destination/35.801876,51.431299,15.0z,0.0p"
        val expected = "https://maps.google.com/?q=35.801876,51.431299"
        assertEquals(expected, extractGoogleMapsUrl(url))
    }

    @Test
    fun testNeshanBriefUrl() {
        val url = "https://neshan.org/maps/places/_bvPrDWxMI7S#c35.706-51.352-17z-0p"
        val expected = "https://maps.google.com/?q=35.706,51.352"
        assertEquals(expected, extractGoogleMapsUrl(url))
    }

    @Test
    fun testWazeUrl() {
        val url = "https://waze.com/live-map/directions?to=ll.35.8018756656664800,51.4312988519668650"
        val expected = "https://maps.google.com/?q=35.8018756656664800,51.4312988519668650"
        assertEquals(expected, extractGoogleMapsUrl(url))
    }

    @Test
    fun testGoogleMapsUrl() {
        val url = "https://www.google.com/maps/search/?api=1&query=35.8018756656664800,51.4312988519668650"
        val expected = "https://maps.google.com/?q=35.8018756656664800,51.4312988519668650"
        assertEquals(expected, extractGoogleMapsUrl(url))
    }

    @Test
    fun testNullInput() {
        val expected = "https://maps.google.com"
        assertEquals(expected, extractGoogleMapsUrl(null))
    }

    @Test
    fun testEmptyInput() {
        val expected = "https://maps.google.com"
        assertEquals(expected, extractGoogleMapsUrl(""))
    }

    @Test
    fun testNoCoordinates() {
        val url = "Just some text without any coordinates"
        val expected = "https://maps.google.com"
        assertEquals(expected, extractGoogleMapsUrl(url))
    }

    @Test
    fun testFallbackToEmbeddedUrl() {
        val url = "Check out this cool place: https://example.com/some/path"
        val expected = "https://example.com/some/path"
        assertEquals(expected, extractGoogleMapsUrl(url))
    }

    //TODO Add online feature to support Short Links
    @Test
    fun testBaladShortLinkUrl() {
        val url = "https://balad.ir/p/6j3qGzR4cGgOVL"
        val expected = "https://balad.ir/p/6j3qGzR4cGgOVL"
        assertEquals(expected, extractGoogleMapsUrl(url))
    }

    @Test
    fun testNeshanShortLinkUrl() {
        val url = "https://nshn.ir/_bvPrDWxMI7S"
        val expected = "https://nshn.ir/_bvPrDWxMI7S"
        assertEquals(expected, extractGoogleMapsUrl(url))
    }

    @Test
    fun testGoogleMapsShortLinkUrl1() {
        val url = "https://maps.app.goo.gl/Shw7bB5i83g9mDeV6"
        val expected = "https://maps.app.goo.gl/Shw7bB5i83g9mDeV6"
        assertEquals(expected, extractGoogleMapsUrl(url))
    }

    @Test
    fun testGoogleMapsShortLinkUrl2() {
        val url = "https://maps.app.goo.gl/Rzv95fWPzd4T2QLq5"
        val expected = "https://maps.app.goo.gl/Rzv95fWPzd4T2QLq5"
        assertEquals(expected, extractGoogleMapsUrl(url))
    }

}
