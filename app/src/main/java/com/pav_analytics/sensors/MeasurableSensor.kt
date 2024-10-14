package com.pav_analytics.sensors

abstract class MeasurableSensor(protected val sensorType: Int) {

    abstract val sensorDataState: String
    protected var onSensorValueChanged: ((List<Float>) -> Unit)? = null

    abstract val doesSensorExist: Boolean

    abstract fun startListening()
    abstract fun stopListening()

    fun setOnSensorValuesChangedListener(listener: (List<Float>) -> Unit) {
        onSensorValueChanged = listener
    }
}