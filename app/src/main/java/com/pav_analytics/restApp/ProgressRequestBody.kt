package com.pav_analytics.restApp

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.Buffer
import okio.BufferedSink
import okio.source
import java.io.File
import java.io.IOException

class ProgressRequestBody(
    private val file: File,
    private val contentType: MediaType?,
    private val progressListener: ProgressListener
) : RequestBody() {

    interface ProgressListener {
        fun onProgressUpdate(percentage: Int)
    }

    override fun contentType(): MediaType? = contentType

    override fun contentLength(): Long = file.length()

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        val fileLength = file.length()
        val buffer = Buffer()
        file.source().use { source ->
            var totalBytesRead: Long = 0
            var read: Long
            while (source.read(buffer, SEGMENT_SIZE).also { read = it } != -1L) {
                totalBytesRead += read
                sink.write(buffer, read)
                val percentage = (totalBytesRead * 100 / fileLength).toInt()
                progressListener.onProgressUpdate(percentage)
            }
        }
    }

    companion object {
        private const val SEGMENT_SIZE = 2048L // 2 KB
    }
}
