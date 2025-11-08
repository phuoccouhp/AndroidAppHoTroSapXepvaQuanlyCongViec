package com.example.login_signup;

import android.content.Context;

public class AvatarUtils {

    public static int getAvatarResourceId(Context context, String avatarId) {
        if (avatarId == null || avatarId.isEmpty()) {
            
            return R.drawable.anh1;
        }
        
        return context.getResources().getIdentifier(avatarId, "drawable", context.getPackageName());
    }
}
