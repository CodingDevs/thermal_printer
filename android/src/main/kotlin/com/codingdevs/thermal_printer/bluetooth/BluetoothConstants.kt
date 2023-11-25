package com.codingdevs.thermal_printer.bluetooth

object BluetoothConstants {
    // Constants that indicate the current connection state
    const val STATE_NONE = 0 // we're doing nothing
    const val STATE_CONNECTING = 2 // now initiating an outgoing connection
    const val STATE_CONNECTED = 3 // now connected to a remote device
    const val STATE_FAILED = 4 // we're doing nothing

    // Message types sent from the BluetoothChatService Handler
    const val MESSAGE_STATE_CHANGE = 1
    const val MESSAGE_READ = 2
    const val MESSAGE_WRITE = 3
    const val MESSAGE_DEVICE_NAME = 4
    const val MESSAGE_TOAST = 5
    const val MESSAGE_REGISTER_CLIENT = 6
    const val MESSAGE_GET_AVAILABLE_DATA = 7
    const val MESSAGE_EVENT = 8
    const val MESSAGE_SEND_BT_CMD = 9
    const val MESSAGE_FREQUENTLY = 10
    const val MESSAGE_START_SCANNING = 11
    const val MESSAGE_STOP_SCANNING = 12

    const val BLUETOOTH_REGEX = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$"
    const val DEVICE_NAME = "device_name"

}