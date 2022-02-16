package com.skt.welfare

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.RemoteMessage
import com.microsoft.windowsazure.messaging.notificationhubs.NotificationListener

class AzureNotificationListener : NotificationListener {

    override fun onPushNotificationReceived(context: Context?, message: RemoteMessage?) {
        val notification: RemoteMessage.Notification? = message!!.notification
        val title: String? = notification?.title
        val body: String? = notification?.body
        val data = message!!.data

        if (message != null) {
            Log.d(TAG, "Message Notification Title: $title")
            Log.d(TAG, "Message Notification Body: $message")


        }

        if (data != null) {
            for ((key, value) in data) {
                Log.d(TAG, "key, $key value $value")
            }


        }


    }


    companion object {
        private const val TAG = "AzureNtificationListner"
    }
}