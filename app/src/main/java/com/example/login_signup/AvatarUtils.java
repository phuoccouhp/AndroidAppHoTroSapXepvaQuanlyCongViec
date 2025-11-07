package com.example.login_signup;

import android.content.Context;

public class AvatarUtils {

    public static int getAvatarResourceId(Context context, String avatarId) {
        if (avatarId == null || avatarId.isEmpty()) {
            // Trả về một avatar mặc định nếu chưa có
            return R.drawable.ic_avatar_1;
        }
        // Tìm và trả về ID tài nguyên dựa trên tên
        return context.getResources().getIdentifier(avatarId, "drawable", context.getPackageName());
    }
}
