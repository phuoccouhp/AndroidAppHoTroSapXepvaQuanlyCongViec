package com.example.login_signup;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.Vibrator;
import android.text.TextUtils;

public class AlarmService extends Service {

    private Ringtone ringtone;
    private Vibrator vibrator;

    @Override
    public void onCreate() {
        super.onCreate();
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // --- CRITICAL: Defensive Null & Empty Checks ---
        if (intent == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        // The taskId is used as the notification ID. It CANNOT be null or empty.
        // An empty string's hashcode is 0, which is an invalid ID for startForeground.
        String taskId = intent.getStringExtra("taskId");
        if (TextUtils.isEmpty(taskId)) {
            stopSelf();
            return START_NOT_STICKY;
        }

        // --- Start Sound and Vibration Immediately ---
        String vibrationPattern = intent.getStringExtra("vibration");
        String ringtoneUriString = intent.getStringExtra("ringtone");

        Uri alarmUri = (ringtoneUriString != null) ? Uri.parse(ringtoneUriString) : RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmUri == null) {
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }
        ringtone = RingtoneManager.getRingtone(this, alarmUri);
        if (ringtone != null) {
            ringtone.setLooping(true);
            ringtone.play();
        }

        long[] pattern = getVibrationPattern(vibrationPattern);
        vibrator.vibrate(pattern, 0);

        // --- Create and Show Full-Screen Notification ---
        String title = intent.getStringExtra("title");
        int notificationId = taskId.hashCode(); // This is now safe

        Notification notification = NotificationHelper.buildDueTimeNotification(this, "Tới giờ làm rồi!", title, notificationId, intent);

        startForeground(notificationId, notification);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }
        if (vibrator != null) {
            vibrator.cancel();
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private long[] getVibrationPattern(String patternName) {
        if (patternName == null) return new long[]{0, 1000, 1000};
        switch (patternName) {
            case "Short":
                return new long[]{0, 500, 500};
            case "Long":
                return new long[]{0, 1500, 1000};
            case "Heartbeat":
                return new long[]{0, 200, 100, 200, 500};
            default:
                return new long[]{0, 1000, 1000};
        }
    }
}
