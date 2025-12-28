package com.example.login_signup.task;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.RemoteViews;

import com.example.login_signup.HomeActivity;
import com.example.login_signup.R;
import com.example.login_signup.classes.FirebaseRepo;
import com.example.login_signup.classes.Task;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskWidgetProvider extends AppWidgetProvider {

    public static final String ACTION_AUTO_UPDATE = "com.example.login_signup.ACTION_AUTO_UPDATE";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_AUTO_UPDATE.equals(intent.getAction()) || AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(intent.getAction())) {
            ComponentName thisAppWidget = new ComponentName(context.getPackageName(), TaskWidgetProvider.class.getName());
            int[] appWidgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(thisAppWidget);
            onUpdate(context, AppWidgetManager.getInstance(context), appWidgetIds);
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_task);

        Intent intent = new Intent(context, HomeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        views.setOnClickPendingIntent(R.id.layoutTaskContent, pendingIntent);
        views.setOnClickPendingIntent(R.id.tvWidgetTime, pendingIntent);

        fetchUpcomingTask(context, views, appWidgetManager, appWidgetId);
    }

    private static void fetchUpcomingTask(Context context, RemoteViews views, AppWidgetManager appWidgetManager, int appWidgetId) {
        FirebaseRepo fbRepo = new FirebaseRepo();

        if (fbRepo.getCurrentUser() == null) {
            showCurrentTimeState(context, views);
            appWidgetManager.updateAppWidget(appWidgetId, views);
            return;
        }

        Date currentTime = new Date();

        fbRepo.loadTasksForUser(new FirebaseRepo.OnTasksLoadedListener() {
            @Override
            public void onTasksLoaded(List<Task> tasks) {
                String title = "";
                String category = "";
                boolean hasTask = false;
                Date nextTaskTime = null;
                long minDiff = Long.MAX_VALUE;

                for (Task task : tasks) {
                    Date taskDate = task.getTaskDate();

                    if (taskDate == null) continue;

                    if (isSameDay(taskDate, currentTime) && taskDate.after(currentTime)) {
                        long diff = taskDate.getTime() - currentTime.getTime();

                        if (diff < minDiff) {
                            minDiff = diff;
                            title = task.getTitle();
                            category = task.getCategory();
                            nextTaskTime = taskDate;
                            hasTask = true;
                        }
                    }
                }

                SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
                SimpleDateFormat sdfDate = new SimpleDateFormat("EEE, dd/MM/yyyy", Locale.getDefault());

                if (hasTask) {
                    views.setTextViewText(R.id.tvWidgetTime, sdfTime.format(nextTaskTime));
                    views.setTextViewText(R.id.tvWidgetDate, sdfDate.format(nextTaskTime));
                    views.setTextViewText(R.id.tvTaskTitle, title);
                    views.setTextViewText(R.id.tvWidgetCategory, category);

                    int iconResId = getIconForCategory(category);
                    int colorResId = getColorForCategory(category);

                    views.setImageViewResource(R.id.imgTags, iconResId);
                    views.setInt(R.id.imgTags, "setColorFilter", colorResId);
                    views.setViewVisibility(R.id.layoutTaskContent, View.VISIBLE);

                    scheduleNextUpdate(context, nextTaskTime);

                } else {
                    views.setTextViewText(R.id.tvWidgetTime, sdfTime.format(currentTime));
                    views.setTextViewText(R.id.tvWidgetDate, sdfDate.format(currentTime));
                    views.setViewVisibility(R.id.layoutTaskContent, View.GONE);

                    scheduleNextUpdate(context, new Date());
                }

                appWidgetManager.updateAppWidget(appWidgetId, views);
            }

            @Override
            public void onError(Exception e) {
                showCurrentTimeState(context, views);
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
        });
    }

    private static void showCurrentTimeState(Context context, RemoteViews views) {
        Date now = new Date();
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
        SimpleDateFormat sdfDate = new SimpleDateFormat("EEE, dd/MM/yyyy", Locale.getDefault());

        views.setTextViewText(R.id.tvWidgetTime, sdfTime.format(now));
        views.setTextViewText(R.id.tvWidgetDate, sdfDate.format(now));
        views.setViewVisibility(R.id.layoutTaskContent, View.GONE);

        scheduleNextUpdate(context, now);
    }

    private static boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private static int getIconForCategory(String category) {
        if (category == null) return R.drawable.baseline_radio_button_unchecked_24;
        switch (category) {
            case "Work": return R.drawable.baseline_work_24;
            case "Personal": return R.drawable.baseline_person_24;
            case "Health": return R.drawable.baseline_health_24;
            case "Shopping": return R.drawable.baseline_shopping_cart_24;
            default: return R.drawable.baseline_radio_button_unchecked_24;
        }
    }

    private static int getColorForCategory(String category) {
        if (category == null) return Color.parseColor("#64748B");
        switch (category) {
            case "Work": return Color.parseColor("#FF9800");
            case "Personal": return Color.parseColor("#2196F3");
            case "Health": return Color.parseColor("#E91E63");
            case "Shopping": return Color.parseColor("#4CAF50");
            default: return Color.parseColor("#64748B");
        }
    }

    private static void scheduleNextUpdate(Context context, Date taskDate) {
        if (taskDate == null) return;

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, TaskWidgetProvider.class);
        intent.setAction(ACTION_AUTO_UPDATE);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                1001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long triggerTime = taskDate.getTime() + 60000;

        if (alarmManager != null) {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                    } else {
                        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                    }
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                }
            } catch (SecurityException e) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            }
        }
    }
}