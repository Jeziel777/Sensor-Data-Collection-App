/* Wifi.kt/Open GoPro, Version 2.0 (C) Copyright 2021 GoPro, Inc. (http://gopro.com/OpenGoPro). */
/* This copyright was auto-generated on Mon Mar  6 17:45:14 UTC 2023 */

package com.pav_analytics.network

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import com.pav_analytics.FileManager.getMediaStorageDirectory
import com.pav_analytics.util.prettyJson
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.timeout
import io.ktor.client.request.headers
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpMethod
import io.ktor.util.InternalAPI
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyAndClose
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import timber.log.Timber
import java.io.File
import java.io.RandomAccessFile
import java.lang.ref.WeakReference
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class WifiEventListener {
    var onDisconnect: ((Network) -> Unit)? = null
    var onConnect: ((Network) -> Unit)? = null
}

@InternalAPI
@SuppressLint("WifiManagerPotentialLeak")
class Wifi(private val context: Context) {
    lateinit var continuation: Continuation<Unit>
    private val listeners: MutableSet<WeakReference<WifiEventListener>> = mutableSetOf()

    private val wifiManager: WifiManager by lazy {
        context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }
    private val connectivityManager by lazy {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun enableAdapter() {
        if (!wifiManager.isWifiEnabled) {
            val panelIntent = Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
            panelIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(panelIntent)
        }
    }

    private val client by lazy {
        HttpClient(CIO) {
            install(HttpTimeout)
        }
    }

    private val callback = object : ConnectivityManager.NetworkCallback() {
        @RequiresApi(Build.VERSION_CODES.M)
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            // Note!! this prevents us from using LTE / the internet
            connectivityManager.bindProcessToNetwork(network)
            this@Wifi.continuation.resume(Unit)
            listeners.forEach { it.get()?.onConnect?.run { this(network) } }
        }

        @RequiresApi(Build.VERSION_CODES.M)
        override fun onLost(network: Network) {
            Timber.d("Lost network $network")
            super.onLost(network)
            connectivityManager.bindProcessToNetwork(null)
            connectivityManager.unregisterNetworkCallback(this)
            listeners.forEach { it.get()?.onDisconnect?.run { this(network) } }
        }
    }

    fun registerListener(listener: WifiEventListener) {
        listeners.add(WeakReference(listener))
        Timber.d("Registered new listener: $listener")
    }

    fun unregisterListener(listener: WifiEventListener) {
        listeners.removeAll { it.get() == listener || it.get() == null }
        Timber.d("Unregistered listener: $listener")
    }

    // TODO unregister and handle removing cleaned up weak references

    @RequiresApi(Build.VERSION_CODES.Q)
    suspend fun connect(ssid: String, password: String) {
        val wifiNetworkSpecifier =
            WifiNetworkSpecifier.Builder().setSsid(ssid).setWpa2Passphrase(password).build()

        val networkRequest =
            NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .setNetworkSpecifier(wifiNetworkSpecifier).build()

        val connectivityManager =
            context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?

        Timber.d("Connecting to Wifi...")
        suspendCoroutine { cont ->
            this.continuation = cont
            connectivityManager?.requestNetwork(networkRequest, callback)
        }
        Timber.d("Wifi connected")
    }

    suspend fun get(endpoint: String, timeoutMs: Long = 5000L): JsonObject {
        val response = client.request(endpoint) {
            timeout {
                requestTimeoutMillis = timeoutMs
            }
        }
        val bodyAsString: String = response.body()
        return prettyJson.parseToJsonElement(bodyAsString).jsonObject
    }

    suspend fun delete(endpoint: String, timeoutMs: Long = 5000L): JsonObject {
        // Timber.d("DELETE request to: $endpoint")
        val response = client.request(endpoint) {
            method = HttpMethod.Delete
            timeout {
                requestTimeoutMillis = timeoutMs
            }
        }
        val bodyAsString: String = response.body()
        return prettyJson.parseToJsonElement(bodyAsString).jsonObject
    }

    suspend fun getLargerFile(
        endpoint: String,
        context: Context,
        file: File? = null,
        timeoutMs: Long = 3600000L / 4 // 1/4 hour
    ): File {
        val destination =
            file ?: File(getMediaStorageDirectory(context), endpoint.split("/").last())

        try {
            val response: HttpResponse = client.request(endpoint) {
                timeout {
                    requestTimeoutMillis = timeoutMs
                }
                // You might also want to consider setting socketTimeoutMillis and connectTimeoutMillis if necessary
            }

            withContext(Dispatchers.IO) {
                response.content.copyAndClose(destination.writeChannel())
            }

            Timber.d("File downloaded successfully to: ${destination.absolutePath}")
        } catch (e: Exception) {
            Timber.e(e, "Failed to download file from: $endpoint")
            // Handle the error appropriately
        }

        return destination
    }

    suspend fun getFileEfficiently(
        endpoint: String, context: Context, file: File? = null, timeoutMs: Long = 3600000L // 1 hour
    ): File {
        val destination =
            file ?: File(getMediaStorageDirectory(context), endpoint.split("/").last())
        val tempFile = File(destination.absolutePath + ".tmp")

        try {
            // Open the file in read/write mode and seek to the end of the current content
            val raf = RandomAccessFile(tempFile, "rw")
            val existingFileSize = tempFile.length()
            raf.seek(existingFileSize)

            val response: HttpResponse = client.request(endpoint) {
                timeout {
                    requestTimeoutMillis = timeoutMs
                }
                if (existingFileSize > 0) {
                    headers {
                        append("Range", "bytes=$existingFileSize-")
                    }
                }
            }

            withContext(Dispatchers.IO) {
                val byteChannel = response.bodyAsChannel()
                val buffer = ByteArray(1024 * 1024) // 1 MB buffer
                var bytesRead: Int
                while (byteChannel.readAvailable(buffer).also { bytesRead = it } > 0) {
                    raf.write(buffer, 0, bytesRead)
                }
            }

            raf.close()
            tempFile.renameTo(destination)
            Timber.d("File downloaded successfully to: ${destination.absolutePath}")
        } catch (e: Exception) {
            Timber.e(e, "Failed to download file from: $endpoint")
            // Handle the error appropriately
        }

        return destination
    }

    suspend fun getFile(
        endpoint: String, context: Context, file: File? = null, timeoutMs: Long = 100000L
    ): File {
        val destination =
            file ?: File(getMediaStorageDirectory(context), endpoint.split("/").last())
        //val destination = file ?: File(context.filesDir, endpoint.split("/").last())
        val response = client.request(endpoint) {
            timeout {
                requestTimeoutMillis = timeoutMs
            }
        }
        response.bodyAsChannel().copyAndClose(destination.writeChannel())
        return destination
    }

}