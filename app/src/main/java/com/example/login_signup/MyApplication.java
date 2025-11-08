package com.example.login_signup;

import android.app.Application;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        NotificationHelper.createNotificationChannels(this);
    }
}
