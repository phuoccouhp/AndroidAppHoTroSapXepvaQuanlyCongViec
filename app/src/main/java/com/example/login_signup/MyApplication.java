package com.example.login_signup;

import android.app.Application;

// This class is used to perform one-time initialization when the app starts.
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        // By creating the notification channels here, we ensure they are always
        // available before any notification is sent, which is required on Android 8+.
        NotificationHelper.createNotificationChannels(this);
    }
}
