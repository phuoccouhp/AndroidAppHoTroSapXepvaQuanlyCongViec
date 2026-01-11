package com.example.login_signup.classes;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;

import com.example.login_signup.R;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

// Lớp Avatar xử lý các thao tác liên quan đến ảnh đại diện.
public class Avatar {
    // Kích thước mục tiêu để nén ảnh
    private static int TARGET_IMAGE_WIDTH = 500;
    private static int TARGET_IMAGE_HEIGHT = 500;

    private Avatar() {

    }

    // Lấy Resource ID của ảnh trong thư mục drawable từ tên chuỗi
    public static int getAvatarResourceId(Context context, String avatarId) {
        if (avatarId == null || avatarId.isEmpty()) {
            // Trả về ảnh mặc định nếu không có ID
            return R.drawable.anh1;
        }

        // Tìm ID dựa trên tên file trong drawable
        return context.getResources().getIdentifier(avatarId, "drawable", context.getPackageName());
    }

    // Chuyển đổi ảnh từ Uri (thư viện của điện thoại) sang chuỗi Base64
    public static String convertImageToBase64Resized(Context context, Uri imageUri) throws IOException {
        // Lấy ContentResolver để truy cập tài nguyên trong ứng dụng
        ContentResolver contentResolver = context.getContentResolver();

        // Giải mã Uri thành Bitmap
        Bitmap bitmap = decodeUriToBitmap(contentResolver, imageUri);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        // Nén ảnh sang định dạng JPEG với chất lượng 80% để giảm dung lượng
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();

        // Mã hóa mảng byte thành chuỗi Base64 để lưu trữ
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    // Giải mã Uri thành Bitmap
    private static Bitmap decodeUriToBitmap(ContentResolver cr, Uri uri) throws FileNotFoundException {
        BitmapFactory.Options options = new BitmapFactory.Options();

        // Bước 1: Chỉ đọc thông tin kích thước ảnh mà không nạp toàn bộ ảnh vào bộ nhớ
        options.inJustDecodeBounds = true;
        InputStream inputStream = cr.openInputStream(uri);
        BitmapFactory.decodeStream(inputStream, null, options);
        try {
            if (inputStream != null) inputStream.close();
        } catch (IOException e) { e.printStackTrace(); }

        int originalWidth = options.outWidth;
        int originalHeight = options.outHeight;

        // Bước 2: Tính toán tỷ lệ thu nhỏ (inSampleSize)
        int inSampleSize = calculateInSampleSize(originalWidth, originalHeight);

        // Bước 3: Giải mã thực sự với tỷ lệ đã tính
        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
        decodeOptions.inSampleSize = inSampleSize;

        InputStream decodeInputStream = cr.openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(decodeInputStream, null, decodeOptions);
        try {
            if (decodeInputStream != null) decodeInputStream.close();
        } catch (IOException e) { e.printStackTrace(); }

        return bitmap;
    }

    // Tính toán tỷ lệ lấy mẫu để thu nhỏ hình ảnh về gần kích thước mục tiêu
    private static int calculateInSampleSize(int originalWidth, int originalHeight) {
        int inSampleSize = 1;

        if (originalHeight > TARGET_IMAGE_HEIGHT || originalWidth > TARGET_IMAGE_WIDTH) {
            final int halfHeight = originalHeight / 2;
            final int halfWidth = originalWidth / 2;

            // Tăng inSampleSize theo lũy thừa của 2 để đạt được kích thước mong muốn
            while ((halfHeight / inSampleSize) >= TARGET_IMAGE_HEIGHT
                    && (halfWidth / inSampleSize) >= TARGET_IMAGE_WIDTH) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    // Chuyển đổi chuỗi Base64 ngược lại thành đối tượng Bitmap để hiển thị.
    public static Bitmap convertBase64ToBitmap(String base64String) {
        try {
            // Giải mã chuỗi Base64 thành mảng byte
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            // Chuyển mảng byte thành Bitmap
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
    }
}
