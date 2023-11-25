package com.codingdevs.thermal_printer.bluetooth

import java.util.*
import kotlin.collections.HashMap

class SampleGattAttributes {
    // Sample Characteristics.
    init {
        attributes["0000180d-0000-1000-8000-00805f9b34fb"] = "Heart Rate Service"
        attributes["0000180a-0000-1000-8000-00805f9b34fb"] = "Device Information Service"
        attributes[HEART_RATE_MEASUREMENT] = "Heart Rate Measurement"
        attributes["00002a29-0000-1000-8000-00805f9b34fb"] = "Manufacturer Name String"
    }


    companion object {
        const val HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb"
        const val CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb"
        // Unique UUID for this application
        val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
        private val attributes: HashMap<String, String> = HashMap()

        fun lookup(uuid: String, defaultName: String?): String? {
            val name = attributes[uuid]
            return name ?: defaultName
        }
    }
}