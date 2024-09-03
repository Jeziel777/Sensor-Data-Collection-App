package com.example.pav_analytics.GoPro

import androidx.annotation.RequiresPermission
import com.example.pav_analytics.AppContainer
import com.example.pav_analytics.util.GOPRO_BASE_URL
import com.example.pav_analytics.util.prettyJson
import io.ktor.util.InternalAPI
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import timber.log.Timber
import java.io.File

class GoProMediaList (){

    @OptIn(InternalAPI::class)
    @RequiresPermission(allOf = ["android.permission.BLUETOOTH_SCAN", "android.permission.BLUETOOTH_CONNECT"])
    suspend fun perform(appContainer: AppContainer): File? {

        val wifi = appContainer.wifi

        // Get the media list
        val response = wifi.get(GOPRO_BASE_URL + "gopro/media/list")
        Timber.i("Complete media list: ${prettyJson.encodeToString(response)}")

        // Get a list of file names from the media list JSON response
        val fileList =
            response["media"]?.jsonArray?.first()?.jsonObject?.get("fs")?.jsonArray?.map { mediaEntry ->
                mediaEntry.jsonObject["n"]
            }?.map { it.toString().replace("\"", "") }
        Timber.i("Files in media list: ${prettyJson.encodeToString(fileList)}")

        // Find a .jpg
        val photo = fileList?.firstOrNull { it.endsWith(ignoreCase = true, suffix = "jpg") }
            ?: throw Exception("Not able to find a .jpg in the media list")
        Timber.i("Found a photo: $photo")

        // Download the photo
        Timber.i("Downloading photo: $photo...")
        return wifi.getFile(
            GOPRO_BASE_URL + "videos/DCIM/100GOPRO/$photo", appContainer.applicationContext
        )
    }

    @OptIn(InternalAPI::class)
    suspend fun getGoProMediaList(appContainer: AppContainer): List<String>? {

        val wifi = appContainer.wifi

        val response = wifi.get(GOPRO_BASE_URL + "gopro/media/list") // Get the media list

        // Get a list of file names from the media list JSON response
        val fileList =
            response["media"]?.jsonArray?.first()?.jsonObject?.get("fs")?.jsonArray?.map { mediaEntry ->
                mediaEntry.jsonObject["n"]
            }?.map { it.toString().replace("\"", "") }

        return fileList
    }

    @OptIn(InternalAPI::class)
    @RequiresPermission(allOf = ["android.permission.BLUETOOTH_SCAN", "android.permission.BLUETOOTH_CONNECT"])
    suspend fun getFileByName(fileName: String, appContainer: AppContainer) {
        val wifi = appContainer.wifi
        //Timber.i("Downloading file: $fileName...")
        wifi.getLargerFile(
            GOPRO_BASE_URL + "videos/DCIM/100GOPRO/$fileName", appContainer.applicationContext
        )
    }

    @OptIn(InternalAPI::class)
    @RequiresPermission(allOf = ["android.permission.BLUETOOTH_SCAN", "android.permission.BLUETOOTH_CONNECT"])
    suspend fun getLargerFileByName(fileName: String, appContainer: AppContainer) {
        val wifi = appContainer.wifi
        //Timber.i("Downloading file: $fileName...")
        wifi.getFileEfficiently(
            GOPRO_BASE_URL + "videos/DCIM/100GOPRO/$fileName", appContainer.applicationContext
        )
    }

    @OptIn(InternalAPI::class)
    suspend fun deleteFileByName(fileName: String, appContainer: AppContainer): String {
        val wifi = appContainer.wifi
        val response = wifi.get(GOPRO_BASE_URL + "gopro/media/delete/file?path=105GOPRO/$fileName")
        return prettyJson.encodeToString(response)
    }


}