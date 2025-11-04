package com.example.login_signup;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class TaskReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            rescheduleAlarms(context);
        } else {
            handleAlarm(context, intent);
        }
    }

    private void handleAlarm(Context context, Intent intent) {
        boolean isAdvance = intent.getBooleanExtra("isAdvance", false);
        String taskId = intent.getStringExtra("taskId");

        if (isAdvance) {
            String title = intent.getStringExtra("title");
            String note = intent.getStringExtra("note");
            String category = intent.getStringExtra("category");
            String taskInfo = "Title: " + title + "\nCategory: " + category + "\nNote: " + note;
            NotificationHelper.showAdvanceNotification(context, title, taskInfo, taskId.hashCode());
        } else {
            Intent alarmIntent = new Intent(context, AlarmActivity.class);
            alarmIntent.putExtras(intent.getExtras());
            alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(alarmIntent);
        }
    }

    private void rescheduleAlarms(Context context) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("tasks")
                .whereEqualTo("reminder", true)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Task t = document.toObject(Task.class);
                            t.setId(document.getId());

                            if (t.getTaskDate() != null && t.getTaskDate().getTime() > System.currentTimeMillis()) {
                                scheduleAlarmsForTask(context, t);
                            }
                        }
                    }
                });
    }

    private void scheduleAlarmsForTask(Context context, Task task) {
        long dueTime = task.getTaskDate().getTime();
        String taskId = task.getId();
        String title = task.getTitle();
        String note = task.getNote();
        String category = task.getCategory();
        String vibration = task.getVibration();
        String ringtone = task.getRingtone();

        // Reschedule the main due-time alarm
        scheduleNotification(context, taskId, title, note, category, dueTime, false, vibration, ringtone);

        // Reschedule the advance notification
        long twentyFourHoursInMillis = 24 * 60 * 60 * 1000;
        long advanceTime = dueTime - twentyFourHoursInMillis;
        if (advanceTime > System.currentTimeMillis()) {
            scheduleNotification(context, taskId + "_advance", title, note, category, advanceTime, true, vibration, ringtone);
        }
    }

    private void scheduleNotification(Context context, String taskId, String title, String note, String category, long time, boolean isAdvance, String vibration, String ringtone) {
        Intent intent = new Intent(context, TaskReminderReceiver.class);
        intent.putExtra("taskId", taskId);
        intent.putExtra("title", title);
        intent.putExtra("note", note);
        intent.putExtra("category", category);
        intent.putExtra("isAdvance", isAdvance);
        if (ringtone != null) {
            intent.putExtra("ringtone", ringtone);
        }
        if (vibration != null) {
            intent.putExtra("vibration", vibration);
        }

        int requestCode = taskId.hashCode();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent);
        }
    }
}
