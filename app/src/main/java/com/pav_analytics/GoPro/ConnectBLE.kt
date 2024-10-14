package com.pav_analytics.GoPro

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanFilter
import android.os.Build
import android.os.ParcelUuid
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import com.pav_analytics.AppContainer
import com.pav_analytics.DataStore
import com.pav_analytics.util.GOPRO_UUID
import com.pav_analytics.util.GoProUUID
import com.pav_analytics.util.isNotifiable
import io.ktor.util.InternalAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

@InternalAPI
class ConnectBLE() {

    private val scanFilters = listOf<ScanFilter>(
        ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(GOPRO_UUID)).build()
    )


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @OptIn(ExperimentalUnsignedTypes::class)
    @RequiresPermission(allOf = ["android.permission.BLUETOOTH_SCAN", "android.permission.BLUETOOTH_CONNECT"])
    suspend fun perform(appContainer: AppContainer): File? {
        val ble = appContainer.ble
        lateinit var goproAddress: String

        // Scan for and find a gopro devices
        ble.startScan(scanFilters).onSuccess { scanResults ->
            //Timber.i("Scanning for GoPro's")
            val deviceChannel: Channel<BluetoothDevice> = Channel()
            // Collect scan results
            CoroutineScope(Dispatchers.IO).launch {
                scanResults.collect { scanResult ->
                    //Timber.i("Found GoPro: ${scanResult.device.name}")
                    //We will take the first discovered gopro
                    deviceChannel.send(scanResult.device)
                }
            }
            // Wait to receive the scan result
            goproAddress = deviceChannel.receive().address
            // We're done with scanning now
            ble.stopScan(scanResults)
        }.onFailure { throw it }

        // Now that we found a gopro, connect to it
        Timber.i("Connecting to $goproAddress")
        ble.connect(goproAddress).onFailure { throw it }

        // Store connected device for other tutorials to use
        DataStore.connectedGoPro = goproAddress

        /**
         * Perform initial BLE setup
         */
        // Discover all characteristics
        Timber.i("Discovering characteristics")
        ble.discoverCharacteristics(goproAddress).onFailure { throw it }
        // Read a known encrypted characteristic to trigger pairing
        Timber.i("Pairing")
        ble.readCharacteristic(goproAddress, GoProUUID.WIFI_AP_PASSWORD.uuid, 30000)
            .onFailure { throw it }
        Timber.i("Enabling notifications")
        // Now that we're paired, for each characteristic that is notifiable...
        ble.servicesOf(goproAddress).fold(onFailure = { throw it }, onSuccess = { services ->
            services.forEach { service ->
                service.characteristics.forEach { char ->
                    if (char.isNotifiable()) {
                        // Enable notifications for this characteristic
                        ble.enableNotification(goproAddress, char.uuid).onFailure { throw it }
                    }
                }
            }
        })
        //Timber.i("Bluetooth is ready for communication!")
        return null
    }
}