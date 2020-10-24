package com.cloq.loonki

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.Query
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class MyFirebaseMessagingService : FirebaseMessagingService() {
    val uid = App.prefs.getPref("UID", "no user")
    private val TAG = "FirebaseService"

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // ...

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            val token = App.prefs.getPref("FCM_TOKEN", "")
            if (token != "" ) {
                fs.collection("tokens").document(token).get().addOnSuccessListener { ds1 ->
                    if (ds1.get("uid") == uid) {
                        when (remoteMessage.data["type"]) {
                            "chat" -> {
                                fs.collection("users").document(uid).collection("rooms")
                                    .document(remoteMessage.data["room"].toString()).get()
                                    .addOnSuccessListener { ds2 ->
                                        if (!(ds2["alert"] as Boolean)) return@addOnSuccessListener
                                        else chatNotify(remoteMessage)
                                    }
                            }
                            else -> {
                                sendNotification(remoteMessage)
                            }
                        }
                    }
                }
            }
        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        App.prefs.setPref("FCM_TOKEN", token)

    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun sendNotification(m: RemoteMessage) {
        val intent = Intent(this, AuthActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("Notification", m.data.toString())
        }

        val CHANNEL_ID = "channel_id"
        val CHANNEL_NAME = "channel_name"
        val description = "description"
        val importance = NotificationManager.IMPORTANCE_HIGH

        val notificationManager: NotificationManager =
            this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance)
            channel.description = description
            channel.enableLights(true)
            channel.lightColor = Color.RED
            channel.enableVibration(true)
            channel.setShowBadge(false)
            notificationManager.createNotificationChannel(channel)
        }

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        val notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(m.data["title"].toString())
            .setContentText(m.data["body"].toString())
            .setAutoCancel(true)
            .setSound(notificationSound)
            .setContentIntent(pendingIntent)

        notificationManager.notify(0, notificationBuilder.build())
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun chatNotify(m: RemoteMessage) {

        //val title = m.data["title"].toString()
        val title = "메세지가 도착했습니다."
        val content = m.data["body"].toString()

        val CHANNEL_ID = "chat"
        val CHANNEL_NAME = "채팅"
        val description = "description"
        val importance = NotificationManager.IMPORTANCE_HIGH

        val roomRef = fs.collection("rooms").document(m.data["room"].toString())
        roomRef.collection("chats")
            .orderBy("time", Query.Direction.DESCENDING).limit(5).get()
            .addOnSuccessListener { querySnapshot ->

                val inboxStyle = NotificationCompat.InboxStyle()

                querySnapshot.documents.forEach { snapshot ->
                    if (!(snapshot.get("read") as Boolean) && snapshot.get("sender") != uid) inboxStyle.addLine(
                        snapshot.get("message").toString()
                    )
                }

                val notificationManager: NotificationManager =
                    this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance)
                    channel.description = description
                    channel.enableLights(true)
                    channel.enableVibration(true)
                    notificationManager.createNotificationChannel(channel)
                }

                val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.users_1))
                    .setContentTitle(title)
                    .setContentText(content)
                    .setShowWhen(true)
                    .setColor(ContextCompat.getColor(this, R.color.color2))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setStyle(inboxStyle)
                    .build()

                notificationManager.apply {
                    notify(1, builder)
                }
            }
    }
}
