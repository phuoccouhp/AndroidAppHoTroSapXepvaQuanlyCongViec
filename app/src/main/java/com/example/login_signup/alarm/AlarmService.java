package com.example.login_signup.alarm;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.Vibrator;
import android.text.TextUtils;

public class AlarmService extends Service {

    private Ringtone ringtone; // Đối tượng dùng để phát nhạc chuông báo thức
    private Vibrator vibrator; // Đối tượng dùng để điều khiển rung của thiết bị

    @Override
    public void onCreate() {
        super.onCreate();
        // Khởi tạo dịch vụ rung khi Service được tạo
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    // Khi Service được khởi động
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Kiểm tra nếu intent rỗng thì dừng service
        if (intent == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        // Kiểm tra ID công việc, nếu không có thì dừng service
        String taskId = intent.getStringExtra("taskId");
        if (TextUtils.isEmpty(taskId)) {
            stopSelf();
            return START_NOT_STICKY;
        }

        // Lấy kiểu rung và nhạc chuông được chọn từ Intent
        String vibrationPattern = intent.getStringExtra("vibration");
        String ringtoneUriString = intent.getStringExtra("ringtone");

        // Xác định URI của nhạc chuông, nếu không có thì dùng nhạc chuông báo thức mặc định
        Uri alarmUri = (ringtoneUriString != null) ? Uri.parse(ringtoneUriString) : RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmUri == null) {
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }

        // Khởi tạo và phát nhạc chuông
        ringtone = RingtoneManager.getRingtone(this, alarmUri);
        if (ringtone != null) {
            ringtone.setLooping(true); // Lặp lại nhạc chuông liên tục
            ringtone.play(); // Phát nhạc chuông
        }

        // Thiết lập kiểu rung và bắt đầu rung
        long[] pattern = getVibrationPattern(vibrationPattern);
        vibrator.vibrate(pattern, 0); // lặp lại kiểu rung từ đầu

        // START_STICKY giúp Service tự khởi động lại nếu hệ thống bị kill
        return START_STICKY;
    }

    // Giải phóng tài nguyên khi Service bị hủy
    @Override
    public void onDestroy() {
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop(); // Dừng phát nhạc chuông
        }
        if (vibrator != null) {
            vibrator.cancel(); // Dừng rung
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // Hàm lấy mảng kiểu rung dựa trên tên kiểu được truyền vào.
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
