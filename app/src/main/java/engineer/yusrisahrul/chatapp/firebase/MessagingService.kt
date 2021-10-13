package engineer.yusrisahrul.chatapp.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import engineer.yusrisahrul.chatapp.R
import engineer.yusrisahrul.chatapp.activites.ChatActivity
import engineer.yusrisahrul.chatapp.models.User
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_FCM_TOKEN
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_MESSAGE
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_NAME
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_USER
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_USER_ID
import kotlin.random.Random

class MessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Token: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM", "Message: ${remoteMessage.notification?.body}")
        val user = User(
            remoteMessage.data[KEY_NAME],
            "",
            "",
            remoteMessage.data[KEY_FCM_TOKEN],
            remoteMessage.data[KEY_USER_ID] ?: ""
        )

        val notificationId = Random.nextInt()
        val channelId = "chat_message"

        val intent = Intent(this, ChatActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            it.putExtra(KEY_USER, user)
        }
        val builder = NotificationCompat.Builder(
            this,
            channelId
        )
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle(user.name)
            .setContentText(remoteMessage.data[KEY_MESSAGE])
            .setStyle(object : NotificationCompat.BigTextStyle(){}.bigText(
                remoteMessage.data[KEY_MESSAGE]
            ))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT))
            .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Chat Message"
            val channelDescription = "This notification channel is used for chat message notification"
            val importance = NotificationManager.IMPORTANCE_DEFAULT

            val channel = NotificationChannel(channelId, channelName, importance)
            channel.description = channelDescription
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        NotificationManagerCompat.from(this).also {
            it.notify(notificationId, builder.build())
        }
    }
}