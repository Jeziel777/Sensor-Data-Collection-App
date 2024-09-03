package com.example.pav_analytics.restApp

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.pav_analytics.FileManager.getMediaStorageDirectory
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.File
import java.io.IOException
import java.net.URLConnection
import java.util.concurrent.TimeUnit

/*
fun uploadFile(context: Context, file: File, url: String) {
    val client = getSecureOkHttpClient(context)
    val CHUNK_SIZE = 2 * 1024 * 1024 // 2MB chunk size
    val totalParts = (file.length() + CHUNK_SIZE - 1) / CHUNK_SIZE

    for (part in 0 until totalParts) {
        val requestBody = object : RequestBody() {
            override fun contentType() = file.toMediaTypeOrNull()

            override fun writeTo(sink: BufferedSink) {
                val offset = part * CHUNK_SIZE
                val buffer = ByteArray(CHUNK_SIZE.toInt())
                val source = file.source().buffer()

                // Skip the bytes up to the offset
                source.skip(offset)

                // Read and write the chunk
                var bytesRead: Long
                var totalBytesRead: Long = 0
                while (totalBytesRead < CHUNK_SIZE) {
                    bytesRead = source.read(buffer, 0, buffer.size).toLong()
                    if (bytesRead == -1L) break
                    sink.write(buffer, 0, bytesRead.toInt())
                    totalBytesRead += bytesRead
                }
            }

            override fun contentLength() = (file.length() - part * CHUNK_SIZE).coerceAtMost(
                CHUNK_SIZE.toLong()
            )
        }

        val request = Request.Builder()
            .url(url)
            .post(MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.name, requestBody)
                .addFormDataPart("part", part.toString())
                .addFormDataPart("total_parts", totalParts.toString())
                .build())
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle failure
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    // Handle success
                    println("Part $part upload successful!")
                } else {
                    // Handle error
                    println("Part $part upload failed: ${response.message}")
                }
            }
        })
    }
}
*/

/*
fun uploadFile(context: Context, fileName: String, url: String) {
    //val folder = getExternalStorageDirectory(context)
    //val file = File(folder, fileName)
    val file = File(fileName)
    if (!file.exists()) {
        Log.e("FileUpload", "File does not exist: ${file.absolutePath}")
        (context as Activity).runOnUiThread {
            Toast.makeText(context, "File does not exist: ${file.name}", Toast.LENGTH_LONG).show()
        }
        return
    }

    val client = getSecureOkHttpClient(context)
    val mimeType = URLConnection.guessContentTypeFromName(file.name) ?: "application/octet-stream"
    val requestBody = file.asRequestBody(mimeType.toMediaTypeOrNull())

    val request = Request.Builder()
        .url(url)
        .post(MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, requestBody)
            .build())
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
            (context as Activity).runOnUiThread {
                Toast.makeText(context, "Upload failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        override fun onResponse(call: Call, response: Response) {
            (context as Activity).runOnUiThread {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Upload successful!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Upload failed: ${response.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    })
}
 */

fun uploadFile(context: Context, fileName: String, url: String) {
    // Initialize the file object
    val file = File(fileName)

    // Check if the file exists
    if (!file.exists()) {
        Log.e("FileUpload", "File does not exist: ${file.absolutePath}")
        (context as Activity).runOnUiThread {
            Toast.makeText(context, "File does not exist: ${file.name}", Toast.LENGTH_LONG).show()
        }
        return
    }

    // Create an OkHttpClient with timeout settings
    val client = getSecureOkHttpClient(context).newBuilder()
        .connectTimeout(3600, TimeUnit.SECONDS)
        .writeTimeout(3600, TimeUnit.SECONDS)
        .readTimeout(3600, TimeUnit.SECONDS)
        .build()

    // Determine the MIME type of the file
    val mimeType = URLConnection.guessContentTypeFromName(file.name) ?: "application/octet-stream"
    //val requestBody = file.asRequestBody(mimeType.toMediaTypeOrNull())

    val progressRequestBody = ProgressRequestBody(file, mimeType.toMediaTypeOrNull(), object : ProgressRequestBody.ProgressListener {
        override fun onProgressUpdate(percentage: Int) {
            (context as Activity).runOnUiThread {
                onProgressUpdate(percentage)
            }
        }
    })

    // Create the request for file upload
    val request = Request.Builder()
        .url(url)
        .post(MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, progressRequestBody)
            .build())
        .build()

    // Enqueue the request to be executed asynchronously
    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
            (context as Activity).runOnUiThread {
                Toast.makeText(context, "Upload failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        override fun onResponse(call: Call, response: Response) {
            (context as Activity).runOnUiThread {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Upload successful!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Upload failed: ${response.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    })
}

fun uploadFileWithProgress(context: Context, fileName: String, url: String, onProgressUpdate: (Int) -> Unit) {
    val file = File(fileName)
    if (!file.exists()) {
        Log.e("FileUpload", "File does not exist: ${file.absolutePath}")
        (context as Activity).runOnUiThread {
            Toast.makeText(context, "File does not exist: ${file.name}", Toast.LENGTH_LONG).show()
        }
        return
    }

    val client = getSecureOkHttpClient(context).newBuilder()
        .connectTimeout(300, TimeUnit.SECONDS)
        .writeTimeout(300, TimeUnit.SECONDS)
        .readTimeout(300, TimeUnit.SECONDS)
        .build()

    val mimeType = URLConnection.guessContentTypeFromName(file.name) ?: "application/octet-stream"

    val progressRequestBody = ProgressRequestBody(file, mimeType.toMediaTypeOrNull(), object : ProgressRequestBody.ProgressListener {
        override fun onProgressUpdate(percentage: Int) {
            //Log.d("FileUpload", "Upload progress: $percentage%")
            (context as Activity).runOnUiThread {
                onProgressUpdate(percentage)
            }
        }
    })

    val request = Request.Builder()
        .url(url)
        .post(MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, progressRequestBody)
            .build())
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
            (context as Activity).runOnUiThread {
                Toast.makeText(context, "Upload failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        override fun onResponse(call: Call, response: Response) {
            (context as Activity).runOnUiThread {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Upload successful!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Upload failed: ${response.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    })
}

// Extension function to get the MIME type of the file
fun File.toMediaTypeOrNull(): MediaType? {
    val extension = this.extension
    return when (extension) {
        "jpg", "jpeg" -> "image/jpeg".toMediaTypeOrNull()
        "png" -> "image/png".toMediaTypeOrNull()
        "mp4" -> "video/mp4".toMediaTypeOrNull()
        "mp3" -> "audio/mpeg".toMediaTypeOrNull()
        "pdf" -> "application/pdf".toMediaTypeOrNull()
        else -> "application/octet-stream".toMediaTypeOrNull()
    }
}

fun uploadVideoWithProgress(
    context: Context,
    fileName: String,
    url: String,
    uid: String,
    onProgressUpdate: (Int) -> Unit) {

    val file = File(fileName)
    if (!file.exists()) {
        Log.e("FileUpload", "File does not exist: ${file.absolutePath}")
        (context as Activity).runOnUiThread {
            Toast.makeText(context, "File does not exist: ${file.name}", Toast.LENGTH_LONG).show()
        }
        return
    }

    val client = getSecureOkHttpClient(context).newBuilder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    val mimeType = URLConnection.guessContentTypeFromName(file.name) ?: "application/octet-stream"

    val progressRequestBody = ProgressRequestBody(file, mimeType.toMediaTypeOrNull(), object : ProgressRequestBody.ProgressListener {
        override fun onProgressUpdate(percentage: Int) {
            (context as Activity).runOnUiThread {
                onProgressUpdate(percentage)
            }
        }
    })

    val requestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("uid", uid)  // Add UID to the form data
        .addFormDataPart("file", file.name, progressRequestBody)
        .build()

    val request = Request.Builder()
        .url(url)
        .post(requestBody)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
            (context as Activity).runOnUiThread {
                Toast.makeText(context, "Upload failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        override fun onResponse(call: Call, response: Response) {
            (context as Activity).runOnUiThread {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Upload successful!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Upload failed: ${response.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    })
}

fun uploadPhotoWithProgress(
    context: Context,
    fileName: String,
    url: String,
    uid: String,
    visualDistress: String,
    onProgressUpdate: (Int) -> Unit) {

    val mediaFolder = getMediaStorageDirectory(context)
    val file = File(mediaFolder,fileName)
    if (!file.exists()) {
        Log.e("FileUpload", "File does not exist: ${file.absolutePath}")
        (context as Activity).runOnUiThread {
            Toast.makeText(context, "File does not exist: ${file.name}", Toast.LENGTH_LONG).show()
        }
        return
    }

    val client = getSecureOkHttpClient(context).newBuilder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(300, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    val mimeType = URLConnection.guessContentTypeFromName(file.name) ?: "application/octet-stream"

    val progressRequestBody = ProgressRequestBody(file, mimeType.toMediaTypeOrNull(), object : ProgressRequestBody.ProgressListener {
        override fun onProgressUpdate(percentage: Int) {
            (context as Activity).runOnUiThread {
                onProgressUpdate(percentage)
            }
        }
    })

    val requestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("uid", uid)  // Add UID to the form data
        .addFormDataPart("type", visualDistress)  // Add additional text to the form data
        .addFormDataPart("comment", "")  // Add additional text to the form data
        .addFormDataPart("image", file.name, progressRequestBody)
        .build()

    val request = Request.Builder()
        .url(url)
        .post(requestBody)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
            (context as Activity).runOnUiThread {
                Toast.makeText(context, "Upload failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        override fun onResponse(call: Call, response: Response) {
            (context as Activity).runOnUiThread {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Upload successful!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Upload failed: ${response.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    })
}