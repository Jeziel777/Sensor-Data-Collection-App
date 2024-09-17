package com.example.pav_analytics.FileManager

//Enum class to represent the visual distress
enum class VisualDistress {
    POTHOLE,
    EDGE_CRACK,
    LINEAR_CRACK,
    DEBRIS,
    INVADING_VEGETATION,
    FLOODING,
    RUTS,
    ROAD_HEAVING,
    OTHER
}

//Enum class to represent the state of the file
enum class FileState {
    SENT,
    NOT_SENT
}

//Enum class to represent the recording video device
enum class VideoDevice {
    GOPRO,
    MOBILE
}