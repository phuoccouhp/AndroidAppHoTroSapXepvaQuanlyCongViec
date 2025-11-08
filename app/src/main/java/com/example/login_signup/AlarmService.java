package com.example.login_signup;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.Vibrator;

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
        String vibrationPattern = intent.getStringExtra("vibration");
        String ringtoneUriString = intent.getStringExtra("ringtone");

        
        Uri alarmUri = (ringtoneUriString != null) ? Uri.parse(ringtoneUriString) : RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmUri == null) {
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }
        ringtone = RingtoneManager.getRingtone(this, alarmUri);
        ringtone.setLooping(true);

        
        long[] pattern = getVibrationPattern(vibrationPattern);

        
        ringtone.play();
        vibrator.vibrate(pattern, 0); 

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
