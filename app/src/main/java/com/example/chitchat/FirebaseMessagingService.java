package com.example.chitchat;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;


public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        try{
            String notification_title = remoteMessage.getNotification().getTitle();
            String notification_message = remoteMessage.getNotification().getBody();
            String click_action = remoteMessage.getNotification().getClickAction();

            String from_user_id = remoteMessage.getData().get("from_user_id");

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(notification_title)
                    .setContentText(notification_message)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            Intent resultIntent = new Intent(click_action);
            resultIntent.putExtra("user_id", from_user_id);

            PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);


            int mNotificationId = (int)System.currentTimeMillis();
            NotificationManager mNotifyManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            mNotifyManager.notify(mNotificationId, mBuilder.build());

        }catch(Exception e) {
            Log.e("Exception is ", e.toString());
        }
    }
}
