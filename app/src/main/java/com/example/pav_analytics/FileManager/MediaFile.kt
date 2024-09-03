package com.example.pav_analytics.FileManager

import com.example.pav_analytics.util.FileState

abstract class MediaFile (
    private var fileName: String,
    private var isFileSent: FileState,
) {
    fun setFileState(state: FileState) {
        isFileSent = state
    }

    fun setFileName(name: String) {
        fileName = name
    }

    fun getFileName(): String {
        return fileName
    }

    fun getFileState(): FileState {
        return isFileSent
    }

    abstract fun uploadMediaFile()
}