package com.skt.welfare

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.microsoft.windowsazure.messaging.notificationhubs.NotificationListener

class AzureNotificationListener : NotificationListener {

    override fun onPushNotificationReceived(context: Context?, message: RemoteMessage?) {
        val notification: RemoteMessage.Notification? = message!!.notification
        val title: String? = notification?.title
        val body: String? = notification?.body
        val data = message!!.data


        var m = "";

        FirebaseCrashlytics.getInstance().log("Azure onPushNotificationReceived")
        Log.d(TAG, "Message Notification ㅁㄴㅇㅁㄴㅇㅁㄴㅇ")

        if (message != null) {
            FirebaseCrashlytics.getInstance().log("Message Notification Title: $title")

            Log.d(TAG, "Message Notification Title: $title")
            Log.d(TAG, "Message Notification Body: $message")
        }

        if (data != null) {
            for ((key, value) in data) {
                Log.d(TAG, "key, $key value $value")
                m += value
            }


        }
        if(message != null){
            FireBaseMessagingService().sendNotification(title, m)
        }


    }





    companion object {
        private const val TAG = "AzureNtificationListner"
    }
}