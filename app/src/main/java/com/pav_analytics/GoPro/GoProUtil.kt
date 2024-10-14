package com.pav_analytics.GoPro

import com.pav_analytics.AppContainer
import io.ktor.util.InternalAPI
import java.io.File

abstract class GoProUtil {
    enum class Prerequisite {
        BLE_CONNECTED, WIFI_CONNECTED
    }

    val requiresBle = Prerequisite.BLE_CONNECTED
    val requiresWifi = Prerequisite.WIFI_CONNECTED

    @OptIn(InternalAPI::class)
    abstract suspend fun perform(appContainer: AppContainer): File?
}