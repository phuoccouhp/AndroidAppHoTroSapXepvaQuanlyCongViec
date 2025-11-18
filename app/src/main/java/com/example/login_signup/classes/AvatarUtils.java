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

public class AvatarUtils {
    private static int TARGET_IMAGE_WIDTH = 500;
    private static int TARGET_IMAGE_HEIGHT = 500;

    private AvatarUtils (){

    }

    public static int getAvatarResourceId(Context context, String avatarId) {
        if (avatarId == null || avatarId.isEmpty()) {

            return R.drawable.anh1;
        }

        return context.getResources().getIdentifier(avatarId, "drawable", context.getPackageName());
    }

    public static String convertImageToBase64Resized(Context context, Uri imageUri) throws IOException {
        ContentResolver contentResolver = context.getContentResolver();
        Bitmap bitmap = decodeUriToBitmap(contentResolver, imageUri);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();

        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private static Bitmap decodeUriToBitmap(ContentResolver cr, Uri uri) throws FileNotFoundException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream inputStream = cr.openInputStream(uri);
        BitmapFactory.decodeStream(inputStream, null, options);
        try {
            if (inputStream != null) inputStream.close();
        } catch (IOException e) { e.printStackTrace(); }

        int originalWidth = options.outWidth;
        int originalHeight = options.outHeight;

        // 2. Calculate the optimal 'inSampleSize'
        int inSampleSize = calculateInSampleSize(originalWidth, originalHeight);

        // 3. Decode the image file into a Bitmap, scaling it down
        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
        decodeOptions.inSampleSize = inSampleSize;

        InputStream decodeInputStream = cr.openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(decodeInputStream, null, decodeOptions);
        try {
            if (decodeInputStream != null) decodeInputStream.close();
        } catch (IOException e) { e.printStackTrace(); }

        return bitmap;
    }

    private static int calculateInSampleSize(int originalWidth, int originalHeight) {
        int inSampleSize = 1;

        if (originalHeight > TARGET_IMAGE_HEIGHT || originalWidth > TARGET_IMAGE_WIDTH) {
            final int halfHeight = originalHeight / 2;
            final int halfWidth = originalWidth / 2;

            while ((halfHeight / inSampleSize) >= TARGET_IMAGE_HEIGHT
                    && (halfWidth / inSampleSize) >= TARGET_IMAGE_WIDTH) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public Bitmap convertBase64ToBitmap(String base64String) {
        try {
            // 1. Decode the Base64 string into a byte array
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);

            // 2. Use BitmapFactory to convert the byte array into a Bitmap
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

        } catch (IllegalArgumentException e) {
            // This can happen if the string is not valid Base64
            e.printStackTrace();
            return null;
        }
    }
}
