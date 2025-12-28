package com.example.login_signup;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.login_signup.alarm.AlarmActivity;
import com.example.login_signup.classes.FirebaseRepo;
import com.example.login_signup.classes.Task;

import java.text.SimpleDateFormat;
import java.util.List;
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

        if (isAdvance) {
            String taskId = sourceIntent.getStringExtra("taskId");
            String title = sourceIntent.getStringExtra("title");
            String dueTimeString = sourceIntent.getStringExtra("due_time_string");
            String taskInfo = "Upcoming: " + title + "\nAt: " + dueTimeString;

            if (taskId != null) {
                NotificationHelper.showAdvanceNotification(context, "Upcoming Task", taskInfo, taskId.hashCode(), taskId);
            }
        } else {
            Intent alarmIntent = new Intent(context, AlarmActivity.class);
            alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            alarmIntent.putExtras(sourceIntent.getExtras());
            context.startActivity(alarmIntent);
        }
    }

    private void rescheduleAlarms(Context context) {
        FirebaseRepo fbRepo = new FirebaseRepo();

        fbRepo.loadReminders(new FirebaseRepo.OnTasksLoadedListener() {
            @Override
            public void onTasksLoaded(List<Task> tasks) {
                for (Task t : tasks) {
                    if (t.getTaskDate() != null && t.getTaskDate().getTime() > System.currentTimeMillis()) {
                        scheduleAlarmsForTask(context, t);
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("TaskReminderReceiver", "Error rescheduling alarms", e);
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
                Intent showTaskIntent = new Intent(context, HomeActivity.class);
                PendingIntent showTaskPendingIntent = PendingIntent.getActivity(context, requestCode, showTaskIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(time, showTaskPendingIntent);
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
            } catch (SecurityException se) {
                Log.e("TaskReminderReceiver", "Permission not granted for alarm", se);
            }
        }
    }
}