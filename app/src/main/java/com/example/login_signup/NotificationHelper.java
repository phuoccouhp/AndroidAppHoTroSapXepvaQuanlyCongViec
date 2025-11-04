package com.example.login_signup;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class NotificationHelper {

    public static final String CHANNEL_ID_REMINDER = "task_reminder_channel";
    private static final String CHANNEL_NAME_REMINDER = "Task Due Alarms";
    private static final String CHANNEL_DESC_REMINDER = "Channel for high-priority task alarms";

    public static final String CHANNEL_ID_ADVANCE = "task_advance_channel";
    private static final String CHANNEL_NAME_ADVANCE = "Advance Task Reminders";
    private static final String CHANNEL_DESC_ADVANCE = "Channel for advance task reminders (1 day before)";

    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);

            NotificationChannel reminderChannel = new NotificationChannel(
                    CHANNEL_ID_REMINDER, CHANNEL_NAME_REMINDER, NotificationManager.IMPORTANCE_HIGH);
            reminderChannel.setDescription(CHANNEL_DESC_REMINDER);
            reminderChannel.enableVibration(true);
            reminderChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            reminderChannel.setSound(alarmSound, null);
            manager.createNotificationChannel(reminderChannel);

            NotificationChannel advanceChannel = new NotificationChannel(
                    CHANNEL_ID_ADVANCE, CHANNEL_NAME_ADVANCE, NotificationManager.IMPORTANCE_DEFAULT);
            advanceChannel.setDescription(CHANNEL_DESC_ADVANCE);
            manager.createNotificationChannel(advanceChannel);
        }
    }

    public static void showDueTimeNotification(Context context, String title, String content, int notificationId, Intent intent) {
        Intent fullScreenIntent = new Intent(context, AlarmActivity.class);
        fullScreenIntent.putExtras(intent.getExtras());
        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(context, notificationId, 
                fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        String ringtoneUriString = intent.getStringExtra("ringtone");
        if (ringtoneUriString != null) {
            alarmSound = Uri.parse(ringtoneUriString);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_REMINDER)
                .setSmallIcon(R.drawable.baseline_notifications_24)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(fullScreenPendingIntent, true) // This is the key line
                .setSound(alarmSound)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(notificationId, builder.build());
    }

    public static void showAdvanceNotification(Context context, String taskTitle, String taskInfo, int notificationId) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_ADVANCE)
                .setSmallIcon(R.drawable.baseline_notifications_24)
                .setContentTitle("There is one task incoming!")
                .setContentText(taskTitle)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(taskInfo))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(notificationId, builder.build());
    }
}
