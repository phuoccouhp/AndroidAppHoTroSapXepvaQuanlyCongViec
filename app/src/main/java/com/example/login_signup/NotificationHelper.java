package com.example.login_signup;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import com.example.login_signup.alarm.AlarmActivity;

public class NotificationHelper {

    public static final String CHANNEL_ID_REMINDER = "task_reminder_channel";
    private static final String CHANNEL_NAME_REMINDER = "Task Due Alarms";
    private static final String CHANNEL_DESC_REMINDER = "Channel for high-priority full-screen alarms";

    public static final String CHANNEL_ID_ADVANCE = "task_advance_channel";
    private static final String CHANNEL_NAME_ADVANCE = "Advance Task Reminders";
    private static final String CHANNEL_DESC_ADVANCE = "Channel for standard advance task reminders";

    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);

            // A high-priority channel MUST have sound and/or vibration to be considered for a full-screen intent.
            NotificationChannel reminderChannel = new NotificationChannel(
                    CHANNEL_ID_REMINDER, CHANNEL_NAME_REMINDER, NotificationManager.IMPORTANCE_HIGH);
            reminderChannel.setDescription(CHANNEL_DESC_REMINDER);
            reminderChannel.enableLights(true);
            reminderChannel.setLightColor(Color.RED);
            reminderChannel.enableVibration(true);
            reminderChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            
            // Allow lock screen visibility
            reminderChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            manager.createNotificationChannel(reminderChannel);

            NotificationChannel advanceChannel = new NotificationChannel(
                    CHANNEL_ID_ADVANCE, CHANNEL_NAME_ADVANCE, NotificationManager.IMPORTANCE_DEFAULT);
            advanceChannel.setDescription(CHANNEL_DESC_ADVANCE);
            manager.createNotificationChannel(advanceChannel);
        }
    }

    public static Notification buildDueTimeNotification(Context context, String title, String content, int notificationId, Intent sourceIntent) {
        Intent fullScreenIntent = new Intent(context, AlarmActivity.class);
        fullScreenIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        fullScreenIntent.putExtra("taskId", sourceIntent.getStringExtra("taskId"));
        fullScreenIntent.putExtra("title", sourceIntent.getStringExtra("title"));
        fullScreenIntent.putExtra("note", sourceIntent.getStringExtra("note"));
        fullScreenIntent.putExtra("category", sourceIntent.getStringExtra("category"));
        fullScreenIntent.putExtra("ringtone", sourceIntent.getStringExtra("ringtone"));
        fullScreenIntent.putExtra("vibration", sourceIntent.getStringExtra("vibration"));

        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(context, notificationId,
                fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(context, CHANNEL_ID_REMINDER)
                .setSmallIcon(R.drawable.baseline_check_circle_24)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .setAutoCancel(true)
                .build();
    }

    public static void showDueNotification(Context context, Intent sourceIntent) {
        String title = sourceIntent.getStringExtra("title");
        String taskId = sourceIntent.getStringExtra("taskId");
        int notificationId = (taskId != null) ? taskId.hashCode() : 1;

        // 1. Create the intent that leads to AlarmActivity
        Intent activityIntent = new Intent(context, AlarmActivity.class);
        // Pass all the extras from the receiver to the activity
        activityIntent.putExtras(sourceIntent.getExtras());
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_USER_ACTION);

        // 2. Wrap it in a PendingIntent
        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                activityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // 3. Build the notification
        // Use setFullScreenIntent to ensure the activity opens automatically or shows as a high-priority notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_REMINDER)
                .setSmallIcon(R.drawable.baseline_check_circle_24)
                .setContentTitle("Task Due: " + title)
                .setContentText("Tap to stop alarm")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(notificationId, builder.build());
    }

    public static void showAdvanceNotification(Context context, String taskTitle, String taskInfo, int notificationId) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_ADVANCE)
                .setSmallIcon(R.drawable.baseline_notifications_24)
                .setContentTitle(taskTitle)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(taskInfo))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(notificationId, builder.build());
    }
}
