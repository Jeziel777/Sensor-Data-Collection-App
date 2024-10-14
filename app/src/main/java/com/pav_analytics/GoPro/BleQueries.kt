package com.pav_analytics.GoPro

import android.annotation.SuppressLint
import androidx.annotation.RequiresPermission
import com.pav_analytics.AppContainer
import com.pav_analytics.DataStore
import com.pav_analytics.network.BleEventListener
import com.pav_analytics.network.Bluetooth
import com.pav_analytics.util.GoProUUID
import io.ktor.util.InternalAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.util.UUID

private const val RESOLUTION_ID: UByte = 2U

class BleQueries() {
    private enum class Resolution(val value: UByte) {
        RES_4K(1U),
        RES_2_7K(4U),
        RES_2_7K_4_3(6U),
        RES_1440(7U),
        RES_1080(9U),
        RES_4K_4_3(18U),
        RES_5K(24U);

        companion object {
            private val valueMap: Map<UByte, Resolution> by lazy { values().associateBy { it.value } }

            fun fromValue(value: UByte) = valueMap.getValue(value)
        }
    }

    private lateinit var resolution: Resolution
    private lateinit var receivedResponses: Channel<Response>
    private val responsesByUuid = GoProUUID.mapByUuid { Response.muxByUuid(it) }

    @OptIn(ExperimentalUnsignedTypes::class)
    private fun notificationHandler(characteristic: UUID, data: UByteArray) {
        // Get the UUID (assuming it is a GoPro UUID)
        val uuid = GoProUUID.fromUuid(characteristic) ?: return
        Timber.d("Received response on $uuid")

        responsesByUuid[uuid]?.let { response ->
            response.accumulate(data)
            if (response.isReceived) {
                when (uuid) {
                    GoProUUID.CQ_QUERY_RSP -> {
                        Timber.d("Received Query Response")
                        CoroutineScope(Dispatchers.IO).launch {
                            receivedResponses.send(
                                response
                            )
                        }
                    }

                    GoProUUID.CQ_SETTING_RSP -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            receivedResponses.send(
                                response
                            )
                        }
                        Timber.d("Received set setting response.")
                    }

                    else -> Timber.e("Unexpected Response")
                }
                responsesByUuid[uuid] = Response.muxByUuid(uuid)
            }
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    val bleListeners by lazy {
        BleEventListener().apply {
            onNotification = ::notificationHandler
        }
    }

    @SuppressLint("NewApi")
    @RequiresPermission(allOf = ["android.permission.BLUETOOTH_SCAN", "android.permission.BLUETOOTH_CONNECT"])
    private suspend fun performPollingTutorial(ble: Bluetooth, goproAddress: String) {
        // Flush the channel. Just create a new one.
        receivedResponses = Channel()

        // Write to query BleUUID to poll the current resolution
        Timber.i("Polling the current resolution")
        val pollResolution = ubyteArrayOf(0x02U, 0x12U, RESOLUTION_ID)
        ble.writeCharacteristic(goproAddress, GoProUUID.CQ_QUERY.uuid, pollResolution)
        val queryResponse = (receivedResponses.receive() as Response.Query).apply { parse() }
        resolution = Resolution.fromValue(queryResponse.data.getValue(RESOLUTION_ID).first())
        Timber.i("Camera resolution is $resolution")

        // Write to command request BleUUID to change the video resolution (either to 1080 or 2.7K)
        val targetResolution =
            if (resolution == Resolution.RES_2_7K) Resolution.RES_1080 else Resolution.RES_2_7K
        Timber.i("Changing the resolution to $targetResolution")
        val setResolution = ubyteArrayOf(0x03U, RESOLUTION_ID, 0x01U, targetResolution.value)
        ble.writeCharacteristic(goproAddress, GoProUUID.CQ_SETTING.uuid, setResolution)
        val setResolutionResponse = (receivedResponses.receive() as Response.Tlv).apply { parse() }
        if (setResolutionResponse.status == 0) {
            Timber.i("Resolution successfully changed")
        } else {
            Timber.e("Failed to set resolution to to $targetResolution. Ensure camera is in a valid state to allow this resolution.")
            return
        }

        // Now let's poll until an update occurs
        Timber.i("Polling the resolution until it changes")
        while (resolution != targetResolution) {
            ble.writeCharacteristic(goproAddress, GoProUUID.CQ_QUERY.uuid, pollResolution)
            val queryNotification =
                (receivedResponses.receive() as Response.Query).apply { parse() }
            resolution =
                Resolution.fromValue(queryNotification.data.getValue(RESOLUTION_ID).first())
            Timber.i("Camera resolution is currently $resolution")
        }
    }

    @SuppressLint("NewApi")
    @RequiresPermission(allOf = ["android.permission.BLUETOOTH_SCAN", "android.permission.BLUETOOTH_CONNECT"])
    private suspend fun performRegisteringForAsyncUpdatesTutorial(
        ble: Bluetooth, goproAddress: String
    ) {
        // Flush the channel. Just create a new one.
        receivedResponses = Channel()

        // Register with the GoPro for updates when resolution updates occur
        Timber.i("Registering for resolution value updates")
        val registerResolutionUpdates = ubyteArrayOf(0x02U, 0x52U, RESOLUTION_ID)
        ble.writeCharacteristic(goproAddress, GoProUUID.CQ_QUERY.uuid, registerResolutionUpdates)
        val queryResponse =
            (receivedResponses.receive() as Response.Query).apply { parse() }
        resolution =
            Resolution.fromValue(queryResponse.data.getValue(RESOLUTION_ID).first())
        Timber.i("Camera resolution is $resolution")

        // Write to command request BleUUID to change the video resolution (either to 1080 or 2.7K)
        val targetResolution =
            if (resolution == Resolution.RES_2_7K) Resolution.RES_1080 else Resolution.RES_2_7K
        Timber.i("Changing the resolution to $targetResolution")
        val setResolution = ubyteArrayOf(0x03U, RESOLUTION_ID, 0x01U, targetResolution.value)
        ble.writeCharacteristic(goproAddress, GoProUUID.CQ_SETTING.uuid, setResolution)
        val setResolutionResponse = (receivedResponses.receive() as Response.Tlv).apply { parse() }
        if (setResolutionResponse.status == 0) {
            Timber.i("Resolution successfully changed")
        } else {
            Timber.e("Failed to set resolution to to $targetResolution. Ensure camera is in a valid state to allow this resolution.")
            return
        }

        // Verify we receive the update from the camera when the resolution changes
        while (resolution != targetResolution) {
            Timber.i("Waiting for camera to inform us about the resolution change")
            val queryNotification =
                (receivedResponses.receive() as Response.Query).apply { parse() }
            resolution =
                Resolution.fromValue(queryNotification.data.getValue(RESOLUTION_ID).first())
            Timber.i("Camera resolution is $resolution")
        }

        Timber.i("Resolution Update Notification has been received.")

        // Unregister for notifications
        Timber.i("Unregistering for resolution value updates")
        val unregisterResolutionUpdates = ubyteArrayOf(0x02U, 0x72U, RESOLUTION_ID)
        ble.writeCharacteristic(goproAddress, GoProUUID.CQ_QUERY.uuid, unregisterResolutionUpdates)
        receivedResponses.receive()
    }

    @OptIn(InternalAPI::class)
    @SuppressLint("NewApi")
    @RequiresPermission(allOf = ["android.permission.BLUETOOTH_SCAN", "android.permission.BLUETOOTH_CONNECT"])
    suspend fun perform(appContainer: AppContainer): File? {
        val ble = appContainer.ble
        val goproAddress =
            DataStore.connectedGoPro ?: throw Exception("No GoPro is currently connected")

        ble.registerListener(goproAddress, bleListeners)

        performPollingTutorial(ble, goproAddress)

        performRegisteringForAsyncUpdatesTutorial(ble, goproAddress)

        // Other tutorials will use their own notification handler so unregister ours now
        ble.unregisterListener(bleListeners)

        return null
    }
}