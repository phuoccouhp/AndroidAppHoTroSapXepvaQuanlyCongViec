package com.example.login_signup;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class TaskReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            rescheduleAlarms(context);
        } else {
            handleAlarm(context, intent);
        }
    }

    private void handleAlarm(Context context, Intent sourceIntent) {
        boolean isAdvance = sourceIntent.getBooleanExtra("isAdvance", false);
        String taskId = sourceIntent.getStringExtra("taskId");

        if (isAdvance) {
            String title = sourceIntent.getStringExtra("title");
            String dueTimeString = sourceIntent.getStringExtra("due_time_string");
            String taskInfo = "Sắp tới: " + title + "\nLúc: " + dueTimeString;
            NotificationHelper.showAdvanceNotification(context, "Công việc sắp tới", taskInfo, taskId.hashCode());
        } else {
            // FIX: Create a clean Intent for the service.
            // Blindly copying extras from the AlarmManager's intent can cause a crash due to unparcelable data.
            Intent serviceIntent = new Intent(context, AlarmService.class);
            serviceIntent.putExtra("taskId", sourceIntent.getStringExtra("taskId"));
            serviceIntent.putExtra("title", sourceIntent.getStringExtra("title"));
            serviceIntent.putExtra("note", sourceIntent.getStringExtra("note"));
            serviceIntent.putExtra("category", sourceIntent.getStringExtra("category"));
            serviceIntent.putExtra("ringtone", sourceIntent.getStringExtra("ringtone"));
            serviceIntent.putExtra("vibration", sourceIntent.getStringExtra("vibration"));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
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
        scheduleNotification(context, task, dueTime, false);

        long twentyFourHoursInMillis = 24 * 60 * 60 * 1000;
        long advanceTime = dueTime - twentyFourHoursInMillis;
        if (advanceTime > System.currentTimeMillis()) {
            scheduleNotification(context, task, advanceTime, true);
        }
    }

    private void scheduleNotification(Context context, Task task, long time, boolean isAdvance) {
        String taskId = isAdvance ? task.getId() + "_advance" : task.getId();

        Intent intent = new Intent(context, TaskReminderReceiver.class);
        intent.putExtra("taskId", taskId);
        intent.putExtra("title", task.getTitle());
        intent.putExtra("note", task.getNote());
        intent.putExtra("category", task.getCategory());
        intent.putExtra("isAdvance", isAdvance);
        if (task.getRingtone() != null) {
            intent.putExtra("ringtone", task.getRingtone());
        }
        if (task.getVibration() != null) {
            intent.putExtra("vibration", task.getVibration());
        }
        if (isAdvance) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm, dd/MM", Locale.getDefault());
            intent.putExtra("due_time_string", sdf.format(task.getTaskDate()));
        }

        int requestCode = taskId.hashCode();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            try {
                if (!isAdvance) {
                    Intent showTaskIntent = new Intent(context, HomeActivity.class);
                    PendingIntent showTaskPendingIntent = PendingIntent.getActivity(context, requestCode, showTaskIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                    AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(time, showTaskPendingIntent);
                    alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent);
                }
            } catch (SecurityException se) {
                // Permission not granted
            }
        }
    }
}
