package com.pv.transport.network

import android.util.Log
import com.google.gson.Gson
import com.pv.transport.auth.AuthPrefs
import com.pv.transport.data.SocketResponse
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebSocketManager @Inject constructor(
    private val authPrefs: AuthPrefs
) {
    private lateinit var socket: Socket
    private val gson = Gson()

    private val _socketState = MutableStateFlow<SocketResponse?>(null)
    val socketState = _socketState.asStateFlow()

    fun connect() {

        try {
            val options = IO.Options()
            options.reconnection = true
            options.forceNew = true

            socket = IO.socket("https://uat.pvmyanmar.com", options)
            socket.connect()
            socket.on(Socket.EVENT_CONNECT) {
                Log.d("SOCKET", "Connected")
            }

            socket.on("qr-verify-approve") { args ->

                try {
                    val data = args[0] as JSONObject

                    val response = gson.fromJson(data.toString(), SocketResponse::class.java)
                    _socketState.value = response
                    Log.d("SOCKET", response.toString())

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            socket.on(Socket.EVENT_DISCONNECT) {

                Log.d("SOCKET", "Disconnected")
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun disconnect() {
        socket.disconnect()
        socket.off()
    }
    fun clearSocketState() {
        _socketState.value = null // သို့မဟုတ် initial state
    }
}
