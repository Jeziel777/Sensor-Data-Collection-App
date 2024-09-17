package com.example.pav_analytics.FileManager


class VideoFile(
    fileName: String,
    isFileSent: FileState,
    private var videoDevice: VideoDevice? = null
) : MediaFile(fileName, isFileSent) {

    override fun uploadMediaFile() {
    }

    fun getVideoDevice(): VideoDevice? {
        return videoDevice
    }

    fun setVideoDevice(videoDevice: VideoDevice) {
        this.videoDevice = videoDevice
    }
}