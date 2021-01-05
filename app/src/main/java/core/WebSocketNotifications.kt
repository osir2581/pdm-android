package ro.ubbcluj.cs.ilazar.myapp2.core

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import okhttp3.*
import okio.ByteString
import ro.ubbcluj.cs.ilazar.myapp2.auth.data.TokenHolder

data class TokenNotification(val payload: TokenHolder, val type:String){}

object WebSocketNotifications {

    private const val url = "ws://192.168.56.1:3000";
    val channel = Channel<Notification>()
    private var token = ""
    private lateinit var webSocket: WebSocket
    fun initializeWebSocket(token:String){
        this.token = token
        val request = Request.Builder().url(url).build()
        webSocket = OkHttpClient().newWebSocket(request, MyWebSocketListener())
    }

    fun sendToken(){
        webSocket.send(getJson().toJson(TokenNotification(TokenHolder(token),"authorization")))
        Log.d("WebSocket", "token was sent:$token")
    }
    private class MyWebSocketListener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d("WebSocket", "onOpen")
            sendToken()
        }

        override fun onMessage(webSocket: WebSocket, text:String) {
            val notif = getJson().fromJson(text,Notification::class.java)
            Log.d("WebSocket", "onMessage:${getJson().toJson(notif)}")
            runBlocking { channel.send(notif) }
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            Log.d("WebSocket", "onMessage bytes")
            output("Receiving bytes : " + bytes!!.hex())
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Log.e("WebSocket", "onClosing")
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e("WebSocket", "onFailure", t)
            t.printStackTrace()
        }

        private fun output(txt: String) {
            Log.d("WebSocket", txt)
        }
    }
}