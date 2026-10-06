package com.example.data.bunny

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.Buffer
import okio.BufferedSink
import okio.ForwardingSink
import okio.Sink
import okio.buffer
import java.io.File
import java.io.InputStream
import java.util.concurrent.TimeUnit

object BunnyStorageHelper {
    private const val TAG = "BunnyStorageHelper"

    const val STORAGE_ZONE_NAME = "drikq-news"
    const val ACCESS_KEY = "8acf1dfd-20d8-4cde-bfb7ee476c77-a29d-48c3"
    const val STORAGE_UPLOAD_ENDPOINT = "https://sg.storage.bunnycdn.com"
    const val PUBLIC_CDN_BASE_URL = "https://Drikq-news.b-cdn.net"

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Uploads a file (Uri or raw byte stream) to Bunny.net Edge Storage via HTTP PUT.
     * URL Format: https://sg.storage.bunnycdn.com/drikq-news/{folder}/{fileName}
     * Header: AccessKey: 8acf1dfd-20d8-4cde-bfb7ee476c77-a29d-48c3
     * Header: Content-Type: appropriate mimeType or application/octet-stream
     *
     * @param context Android context to resolve ContentResolver
     * @param fileUri Device Uri of the selected photo or video
     * @param folder Folder name (e.g., "posts" or "profiles")
     * @param fileName Unique file name (e.g., "1696500000_abc123.mp4")
     * @param mimeType Optional MIME type
     * @param onProgress Callback receiving percentage 0..100
     * @return Public CDN URL (https://Drikq-news.b-cdn.net/{folder}/{fileName})
     */
    suspend fun uploadFile(
        context: Context,
        fileUri: Uri,
        folder: String,
        fileName: String,
        mimeType: String? = null,
        onProgress: (Int) -> Unit
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val resolvedMimeType = mimeType ?: contentResolver.getType(fileUri) ?: "application/octet-stream"

            val inputStream: InputStream = contentResolver.openInputStream(fileUri)
                ?: return@withContext Result.failure(Exception("Cannot open stream for Uri: $fileUri"))

            val totalBytes = contentResolver.openFileDescriptor(fileUri, "r")?.statSize ?: -1L

            // Read bytes into memory or stream directly
            val bytes = inputStream.use { it.readBytes() }
            val actualLength = if (totalBytes > 0) totalBytes else bytes.size.toLong()

            val rawRequestBody = RequestBody.create(resolvedMimeType.toMediaTypeOrNull(), bytes)

            val progressRequestBody = ProgressRequestBody(rawRequestBody) { bytesWritten ->
                if (actualLength > 0) {
                    val progress = ((bytesWritten * 100) / actualLength).toInt().coerceIn(0, 100)
                    onProgress(progress)
                }
            }

            // Construct Bunny.net Storage Upload URL
            val cleanFolder = folder.trim('/').ifEmpty { "posts" }
            val uploadUrl = "$STORAGE_UPLOAD_ENDPOINT/$STORAGE_ZONE_NAME/$cleanFolder/$fileName"

            val request = Request.Builder()
                .url(uploadUrl)
                .put(progressRequestBody)
                .addHeader("AccessKey", ACCESS_KEY)
                .addHeader("Content-Type", resolvedMimeType)
                .build()

            Log.d(TAG, "Starting Bunny PUT upload: $uploadUrl (size: $actualLength bytes)")

            client.newCall(request).execute().use { response ->
                val code = response.code
                val responseBody = response.body?.string() ?: ""

                if (response.isSuccessful || code == 200 || code == 201) {
                    val publicCdnUrl = "$PUBLIC_CDN_BASE_URL/$cleanFolder/$fileName"
                    Log.d(TAG, "Bunny PUT upload successful: $publicCdnUrl")
                    onProgress(100)
                    Result.success(publicCdnUrl)
                } else {
                    val errorMsg = "Bunny upload failed with HTTP $code: $responseBody"
                    Log.e(TAG, errorMsg)
                    Result.failure(Exception(errorMsg))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Upload exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Uploads in-memory byte array directly to Bunny.net (used for profile pictures or captured assets)
     */
    suspend fun uploadBytes(
        bytes: ByteArray,
        folder: String,
        fileName: String,
        mimeType: String = "image/jpeg",
        onProgress: (Int) -> Unit = {}
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val cleanFolder = folder.trim('/').ifEmpty { "posts" }
            val uploadUrl = "$STORAGE_UPLOAD_ENDPOINT/$STORAGE_ZONE_NAME/$cleanFolder/$fileName"

            val rawRequestBody = RequestBody.create(mimeType.toMediaTypeOrNull(), bytes)
            val progressRequestBody = ProgressRequestBody(rawRequestBody) { bytesWritten ->
                val progress = ((bytesWritten * 100) / bytes.size).toInt().coerceIn(0, 100)
                onProgress(progress)
            }

            val request = Request.Builder()
                .url(uploadUrl)
                .put(progressRequestBody)
                .addHeader("AccessKey", ACCESS_KEY)
                .addHeader("Content-Type", mimeType)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful || response.code == 200 || response.code == 201) {
                    val publicCdnUrl = "$PUBLIC_CDN_BASE_URL/$cleanFolder/$fileName"
                    onProgress(100)
                    Result.success(publicCdnUrl)
                } else {
                    Result.failure(Exception("HTTP ${response.code}: ${response.body?.string()}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Deletes a file from Bunny Edge Storage using HTTP DELETE request with AccessKey.
     * Works with either full CDN URL or relative path:
     * e.g., "https://Drikq-news.b-cdn.net/posts/file.mp4" -> "/drikq-news/posts/file.mp4"
     */
    suspend fun deleteFile(pathOrCdnUrl: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val relativePath = when {
                pathOrCdnUrl.startsWith(PUBLIC_CDN_BASE_URL, ignoreCase = true) -> {
                    pathOrCdnUrl.removePrefix(PUBLIC_CDN_BASE_URL).trimStart('/')
                }
                pathOrCdnUrl.contains(STORAGE_ZONE_NAME) -> {
                    pathOrCdnUrl.substringAfter(STORAGE_ZONE_NAME).trimStart('/')
                }
                else -> pathOrCdnUrl.trimStart('/')
            }

            val deleteUrl = "$STORAGE_UPLOAD_ENDPOINT/$STORAGE_ZONE_NAME/$relativePath"
            Log.d(TAG, "Sending Bunny DELETE request to: $deleteUrl")

            val request = Request.Builder()
                .url(deleteUrl)
                .delete()
                .addHeader("AccessKey", ACCESS_KEY)
                .build()

            client.newCall(request).execute().use { response ->
                Log.d(TAG, "Bunny DELETE response code: ${response.code}")
                response.isSuccessful || response.code == 200
            }
        } catch (e: Exception) {
            Log.e(TAG, "Bunny DELETE exception: ${e.message}", e)
            false
        }
    }

    /**
     * Helper to wrap a RequestBody and track written bytes for 0%..100% progress.
     */
    private class ProgressRequestBody(
        private val delegate: RequestBody,
        private val onBytesWritten: (Long) -> Unit
    ) : RequestBody() {

        override fun contentType() = delegate.contentType()

        override fun contentLength() = delegate.contentLength()

        override fun writeTo(sink: BufferedSink) {
            val countingSink = object : ForwardingSink(sink) {
                var bytesWritten = 0L

                override fun write(source: Buffer, byteCount: Long) {
                    super.write(source, byteCount)
                    bytesWritten += byteCount
                    onBytesWritten(bytesWritten)
                }
            }
            val bufferedSink = countingSink.buffer()
            delegate.writeTo(bufferedSink)
            bufferedSink.flush()
        }
    }
}
