package com.mikhailgrigorev.photogallery

import android.net.Uri
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


class FlickrFetchr {
    @Throws(IOException::class)
    fun getUrlBytes(urlSpec: String): ByteArray? {
        val url = URL(urlSpec)
        val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
        return try {
            val out = ByteArrayOutputStream()
            val `in`: InputStream = connection.inputStream
            if (connection.getResponseCode() !== HttpURLConnection.HTTP_OK) {
                throw IOException(
                    connection.responseMessage.toString() +
                            ": with " +
                            urlSpec
                )
            }
            var bytesRead = 0
            val buffer = ByteArray(1024)
            while (`in`.read(buffer).also { bytesRead = it } > 0) {
                out.write(buffer, 0, bytesRead)
            }
            out.close()
            out.toByteArray()
        } finally {
            connection.disconnect()
        }
    }

    @Throws(IOException::class)
    fun getUrlString(urlSpec: String) : String? {
        return getUrlBytes(urlSpec)?.let { String(it) }
    }

    fun downloadGalleryItems(url: String): List<GalleryItem>{
        val items: MutableList<GalleryItem> = ArrayList()
        try{
            val jsonString = getUrlString(url)
            Log.i(TAG, "Received JSON: $jsonString")
            val jsonBody = JSONObject(jsonString)
            parseItems(items, jsonBody)
        } catch (ioe: IOException){
            Log.e(TAG, "Failed to fetch items", ioe)
        } catch (je: JSONException){
            Log.e(TAG, "Failed to parse JSON", je)
        }
        return items
    }

    @Throws(IOException::class, JSONException::class)
    private fun parseItems(
        items: MutableList<GalleryItem>,
        jsonBody: JSONObject
    ) {
        val photosJsonObject = jsonBody.getJSONObject("photos")
        val photoJsonArray = photosJsonObject.getJSONArray("photo")
        for (i in 0 until photoJsonArray.length()) {
            val photoJsonObject = photoJsonArray.getJSONObject(i)
            val item = GalleryItem()
            item.id = photoJsonObject.getString("id")
            item.caption = photoJsonObject.getString("title")
            if (!photoJsonObject.has("url_s")) {
                continue
            }
            item.url = photoJsonObject.getString("url_s")
            items.add(item)
        }
    }

    private fun buildUrl(method: String, query: String?): String? {
        val uriBuilder = ENDPOINT.buildUpon()
            .appendQueryParameter("method", method)
        if (method == SEARCH_METHOD) {
            uriBuilder.appendQueryParameter("text", query)
        }
        return uriBuilder.build().toString()
    }

    fun fetchRecentPhotos(): List<GalleryItem> {
        val url = buildUrl(FETCH_RECENTS_METHOD, null)
        return downloadGalleryItems(url!!)
    }

    fun searchPhotos(query: String?): List<GalleryItem> {
        val url = buildUrl(SEARCH_METHOD, query!!)
        return downloadGalleryItems(url!!)
    }


    companion object{
        private val TAG: String = "FlickrFetchr"
        private val API_KEY: String = "2868e249105627f7f8e3525e1565cc0e"
        private const val FETCH_RECENTS_METHOD = "flickr.photos.getRecent"
        private const val SEARCH_METHOD = "flickr.photos.search"
        private val ENDPOINT = Uri
            .parse("https://api.flickr.com/services/rest/")
            .buildUpon()
            .appendQueryParameter("api_key", API_KEY)
            .appendQueryParameter("format", "json")
            .appendQueryParameter("nojsoncallback", "1")
            .appendQueryParameter("extras", "url_s")
            .build()
    }
}

