package com.example.pav_analytics.FileManager

import com.example.pav_analytics.util.FileState

class PictureFile(
    fileName: String,
    isFileSent: FileState,
    private var visualDistress: VisualDistress? = null,
    private var gpsLocation: String? = ""
) : MediaFile(fileName, isFileSent) {

    override fun uploadMediaFile() {
        // Implement your upload logic here
    }

    fun getVisualStress(): VisualDistress? {
        return visualDistress
    }

    fun setVisualStress(visualDistress: VisualDistress) {
        this.visualDistress = visualDistress
    }

    fun setGpsLocation(gpsLocation: String) {
        this.gpsLocation = gpsLocation
    }

    fun getGpsLocation(): String? {
        return gpsLocation
    }

}

enum class VisualDistress {
    POTHOLE,
    EDGE_CRACK,
    LINEAR_CRACK,
    DEBRIS,
    INVADING_VEGETATION,
    OTHER
}