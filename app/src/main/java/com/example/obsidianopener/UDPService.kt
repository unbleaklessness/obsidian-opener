package com.example.obsidianopener

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import org.json.JSONObject

class UDPService : Service() {

    private var running: Boolean = true

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startUDP()
        return START_STICKY
    }

    private fun startUDP() {
        Thread {
            try {
                val bufferSize = 1024 * 5
                val buffer = ByteArray(bufferSize)
                val datagramSocket = java.net.DatagramSocket(6060)
                datagramSocket.broadcast = true
                while (running) {
                    val packet = java.net.DatagramPacket(buffer, buffer.size)
                    datagramSocket.receive(packet)
                    val message = String(packet.data, 0, packet.length)
                    val json = JSONObject(message)
                    json.getString("filePath").let {
                        val encoded: String = Uri.encode(it)
                        Handler(Looper.getMainLooper()).post {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("obsidian://advanced-uri?filepath=${encoded}"))
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    override fun onDestroy() {
        running = false
        super.onDestroy()
    }
}
