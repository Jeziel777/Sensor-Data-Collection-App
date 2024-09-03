package com.example.pav_analytics.FileManager

import com.example.pav_analytics.util.FileState

class VideoFile(fileName: String, isFileSent: FileState) : MediaFile(fileName, isFileSent) {

    override fun uploadMediaFile() {
    }

}