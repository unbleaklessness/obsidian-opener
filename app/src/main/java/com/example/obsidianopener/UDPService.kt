package com.example.obsidianopener

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import org.json.JSONObject

class UDPService : Service() {

    private var running: Boolean = true

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startNotification()
        startUDP()
        return START_STICKY
    }

    private fun startNotification() {
        val channelID = "Obsidian Opener Channel"
        val channelName = "Obsidian Opener Channel"
        val notificationID = 6060

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelID,
                channelName,
                NotificationManager.IMPORTANCE_MIN,
            )
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()

        startForeground(notificationID, notification)
    }

    private fun startUDP() {
        Thread {
            while (running) {
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
                            val openMode = json.optString("openMode", "true")
                            val line = json.optInt("line", 1)
                            val viewMode = json.optString("viewMode", "live")
                            val encoded: String = Uri.encode(it)
                            Handler(Looper.getMainLooper()).post {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("obsidian://advanced-uri?filepath=${encoded}&openmode=${openMode}&viewmode=${viewMode}&line=${line}"))
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }.start()
    }

    override fun onDestroy() {
        running = false
        super.onDestroy()
    }
}
