package com.app.service.reviewaza

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.app.service.reviewaza.call.AlertDetails
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        if(message.notification != null) {
            val userId = message.data["myUserId"].toString()
            val userImage = message.data["myUserImage"].toString()
            val myUserName = message.data["myUserName"].toString()

            showNotification(myUserName, message.notification!!.body!!, userId, userImage)
            Log.e("notificationset", "${message.notification?.title}")
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    private fun showNotification(userId: String?, message: String, userName:String, userImage:String) {

        val intent = Intent(this, AlertDetails::class.java)
        //val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        intent.putExtra("isFCM", true)
        intent.putExtra("userId", userId)
        intent.putExtra("userName", userName)
        intent.putExtra("userImage", userImage)
        intent.putExtra("userMessage", message)
        startActivity(intent.addFlags(FLAG_ACTIVITY_NEW_TASK))

        val pendingIntent: PendingIntent
        pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
        } else {
            PendingIntent.getActivity(
                this,
                0, intent, PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
        val title = userId
        val descriptionText = "택시 알림입니다."
        val importance = NotificationManager.IMPORTANCE_DEFAULT

        val mChannel = NotificationChannel(getString(R.string.default_notification_channel_id), title, importance)
        mChannel.description = descriptionText

        val notificationBuilder = NotificationCompat.Builder(applicationContext, getString(R.string.default_notification_channel_id))
            .setSmallIcon(R.drawable.ic_baseline_local_taxi_24)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)

        // 오레오 버전 예외처리
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(getString(R.string.default_notification_channel_id),
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }


}

