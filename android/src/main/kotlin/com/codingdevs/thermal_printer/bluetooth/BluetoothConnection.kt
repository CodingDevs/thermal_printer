/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.codingdevs.thermal_printer.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.codingdevs.thermal_printer.R
import io.flutter.plugin.common.MethodChannel
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
class BluetoothConnection constructor(handler: Handler) : IBluetoothConnection {


    // Member fields
    private val mAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private val mHandler: Handler
    private var mConnectThread: ConnectThread? = null
    private var mConnectedThread: ConnectedThread? = null
    private var mState: Int

    /**
     * Constructor. Prepares a new BluetoothChat session.
     */
    init {
        mState = BluetoothConstants.STATE_NONE
        mHandler = handler
    }

    /**
     * Return the current connection state.
     * Set the current state of the chat connection
     *
     * state An integer defining the current connection state
     */
    @get:Synchronized
    @set:Synchronized
    override var state: Int
        get() = mState
        set(state) {
//          Log.d(TAG, "setState() " + mState + " -> " + state);
            if (state != BluetoothConstants.STATE_FAILED && state != BluetoothConstants.STATE_CONNECTED)
            // Give the new state to the Handler so the UI Activity can update
                mHandler.obtainMessage(BluetoothConstants.MESSAGE_STATE_CHANGE, state, -1).sendToTarget()
            if (state == BluetoothConstants.STATE_FAILED) mState = BluetoothConstants.STATE_NONE
            mState = state
        }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param address The BluetoothDevice address to connect
     */
    @Synchronized
    override fun connect(address: String, result: MethodChannel.Result) {
        if (!address.matches(Regex(BluetoothConstants.BLUETOOTH_REGEX))) return
        Log.d(TAG, "connect to: $address")
        val device = mAdapter.getRemoteDevice(address)

        // Cancel any thread attempting to make a connection
        if (mState == BluetoothConstants.STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread!!.cancel()
                mConnectThread = null
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread!!.cancel()
            mConnectedThread = null
        }

        // Start the thread to connect with the given device
        mConnectThread = ConnectThread(device, result)
        mConnectThread!!.start()
        state = BluetoothConstants.STATE_CONNECTING

    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    @Synchronized
    private fun connected(socket: BluetoothSocket, device: BluetoothDevice, result: MethodChannel.Result) {
//        Log.d(TAG, "connected");

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread!!.cancel()
            mConnectThread = null
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread!!.cancel()
            mConnectedThread = null
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = ConnectedThread(socket)
        mConnectedThread!!.start()

        // Send the name of the connected device back to the UI Activity
        val msg = mHandler.obtainMessage(BluetoothConstants.MESSAGE_DEVICE_NAME)
        val bundle = Bundle()
        bundle.putString(BluetoothConstants.DEVICE_NAME, device.name)
        msg.data = bundle
        mHandler.sendMessage(msg)
        state = BluetoothConstants.STATE_CONNECTED

        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(BluetoothConstants.MESSAGE_STATE_CHANGE, state, -1, result).sendToTarget()
    }

    /**
     * Stop all threads
     */
    @Synchronized
    override fun stop() {
//        Log.d(TAG, "stop");
        if (mConnectThread != null) {
            mConnectThread!!.cancel()
            mConnectThread = null
        }
        if (mConnectedThread != null) {
            mConnectedThread!!.cancel()
            mConnectedThread = null
        }
        state = BluetoothConstants.STATE_NONE
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread.write
     */
    override fun write(out: ByteArray?) {
        // Create temporary object
        var r: ConnectedThread?
        // Synchronize a copy of the ConnectedThread
        synchronized(this) {
            if (mState != BluetoothConstants.STATE_CONNECTED) return
            r = mConnectedThread
        }
        // Perform the write unsynchronized
        r!!.write(out)

//        Log.d(BluetoothConnection.TAG, "envia: " + new String(out));
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private fun connectionFailed(result: MethodChannel.Result) {
        // Send a failure message back to the Activity
//        Log.e(TAG, "Connection Error");
        val msg = mHandler.obtainMessage(BluetoothConstants.MESSAGE_TOAST)
        val bundle = Bundle()
        bundle.putInt(TOAST, R.string.fail_connect_bt)
        msg.data = bundle
        mHandler.sendMessage(msg)
        state = BluetoothConstants.STATE_FAILED

        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(BluetoothConstants.MESSAGE_STATE_CHANGE, state, -1, result).sendToTarget()

        state = BluetoothConstants.STATE_NONE
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private fun connectionLost() {
        // Send a failure message back to the Activity
        val msg = mHandler.obtainMessage(BluetoothConstants.MESSAGE_TOAST)
        val bundle = Bundle()
        bundle.putInt(TOAST, R.string.lost_connection_bt)
        msg.data = bundle
        mHandler.sendMessage(msg)
        state = BluetoothConstants.STATE_NONE
    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private inner class ConnectThread(
        private val mmDevice: BluetoothDevice,
        private val mmResult: MethodChannel.Result,
    ) : Thread() {
        private val mmSocket: BluetoothSocket?
        override fun run() {
//            Log.i(TAG, "BEGIN mConnectThread");
            name = "ConnectThread"
            if (mmSocket == null) {

                // Reset the ConnectThread because we're done
                synchronized(this@BluetoothConnection) { mConnectThread = null }
                connectionFailed(mmResult)
                return
            }

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery()

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect()
            } catch (e: IOException) {

                // Close the socket
                try {
                    mmSocket.close()
                } catch (e2: IOException) {
                    Log.e(TAG, "unable to close() socket during connection failure")
                }

                // Reset the ConnectThread because we're done
                synchronized(this@BluetoothConnection) { mConnectThread = null }
                connectionFailed(mmResult)
                return
            } catch (e: NullPointerException) {
                try {
                    mmSocket.close()
                } catch (e2: IOException) {
                }
                synchronized(this@BluetoothConnection) { mConnectThread = null }
                connectionFailed(mmResult)
                return
            }

            // Reset the ConnectThread because we're done
            synchronized(this@BluetoothConnection) { mConnectThread = null }

            // Start the connected thread
            connected(mmSocket, mmDevice, mmResult)
        }

        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "close() of connect socket failed")
            }
        }

        init {
            var tmp: BluetoothSocket? = null

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                tmp = mmDevice.createInsecureRfcommSocketToServiceRecord(SampleGattAttributes.SPP_UUID)
            } catch (e: IOException) {
                Log.e(TAG, "Socket: create() failed")
            }
            mmSocket = tmp

        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private inner class ConnectedThread(private val mmSocket: BluetoothSocket) : Thread() {

        private val mmInStream: InputStream? = mmSocket.inputStream
        private val mmOutStream: OutputStream? = mmSocket.outputStream
        private val mmBuffer: ByteArray = ByteArray(1024) // mmBuffer store for the stream

        override fun run() {
            var numBytes: Int // bytes returned from read()

            // Keep listening to the InputStream while connected
            while (true) {

                // Read from the InputStream.
                numBytes = try {
                    mmInStream?.read(mmBuffer) ?: 0
                } catch (e: IOException) {
//                    Log.d(TAG, "Input stream was disconnected", e)
                    connectionLost()
                    break
                }
                // Send the obtained bytes to the UI Activity
                val readMsg = mHandler.obtainMessage(BluetoothConstants.MESSAGE_READ, numBytes, -1, mmBuffer)
                readMsg.sendToTarget()

            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param bytes The bytes to write
         */
        fun write(bytes: ByteArray?) {
//            Log.d(TAG, "envia ConnectedThread MESSAGE_SEND_BT_CMD " + String(bytes!!))
            try {
                mmOutStream?.write(bytes)
            } catch (e: IOException) {
//                Log.e(TAG, "Exception during write", e)
                // Send a failure message back to the activity.
                val writeErrorMsg = mHandler.obtainMessage(BluetoothConstants.MESSAGE_TOAST)
                val bundle = Bundle().apply {
                    putInt(TOAST, R.string.fail_write_data)
                }
                writeErrorMsg.data = bundle
                mHandler.sendMessage(writeErrorMsg)
                return
            }

            // Share the sent message back to the UI Activity
            val writtenMsg = mHandler.obtainMessage(BluetoothConstants.MESSAGE_WRITE, -1, -1, mmBuffer)
            writtenMsg.sendToTarget()
        }

        fun cancel() {
//            try {
//                mmInStream.close()
//            } catch (ignored: Exception) {
//            }
//            try {
//                mmOutStream.close()
//            } catch (ignored: Exception) {
//            }
            try {
                mmSocket.close()
            } catch (e: IOException) {
                Log.e(TAG, "close() of connect socket failed")
            }
        }


    }

    companion object {


        // Key names received from the BluetoothChatService Handler
        const val TOAST = "toast"

        //Intent data
        //    public static final String SERVICE_BT_CHANGE_STATE = "com.ospinn.gettze.BT_CHG_STATE";
        // Debugging
        private const val TAG = "BluetoothConnection"


    }


}