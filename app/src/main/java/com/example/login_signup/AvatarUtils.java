package com.example.login_signup;

import android.content.Context;

public class AvatarUtils {

    public static int getAvatarResourceId(Context context, String avatarId) {
        if (avatarId == null || avatarId.isEmpty()) {
            // THAY ĐỔI: Trả về avatar mặc định là "anh1"
            return R.drawable.anh1;
        }
        // Tìm và trả về ID tài nguyên dựa trên tên
        return context.getResources().getIdentifier(avatarId, "drawable", context.getPackageName());
    }
}
