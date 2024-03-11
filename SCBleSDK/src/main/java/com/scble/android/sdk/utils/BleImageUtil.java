package com.scble.android.sdk.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Keep
public class BleImageUtil {
    @Keep
    public static Bitmap base64ToBitmap(String base64) {
        byte[] decodedBytes = Base64.decode(base64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    public static String convertImageUriToBase64(@NonNull Context context, @NonNull Uri imageUri, int reqWidth, int reqHeight) throws IOException {
        Bitmap bitmap = decodeSampledBitmapFromUri(context, imageUri, reqWidth, reqHeight);
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteStream);
        byte[] bytes = byteStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private static Bitmap decodeSampledBitmapFromUri(Context context, Uri imageUri, int reqWidth, int reqHeight) {
        // 处理图片横向问题
        ExifInterface exif = null;
        int rotationDegrees = 0;
        try {
            exif = new ExifInterface(getPath(context,imageUri));
            int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            rotationDegrees = getRotationDegrees(rotation);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try (InputStream inputStream = context.getContentResolver().openInputStream(imageUri)) {
            BitmapFactory.decodeStream(inputStream, null, options);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        // 计算合适的inSampleSize值
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // 使用计算出的inSampleSize解码图片
        options.inJustDecodeBounds = false;
        Bitmap bitmap = null;
        try (InputStream inputStream = context.getContentResolver().openInputStream(imageUri)) {
            bitmap = BitmapFactory.decodeStream(inputStream, null, options);
            // 旋转图片
            if (rotationDegrees != 0) {
                Matrix matrix = new Matrix();
                matrix.postRotate(rotationDegrees);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }


    private static int getRotationDegrees(int rotation) {
        switch (rotation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return 90;
            case ExifInterface.ORIENTATION_ROTATE_180:
                return 180;
            case ExifInterface.ORIENTATION_ROTATE_270:
                return 270;
            default:
                return 0;
        }
    }



    public static String getPath(Context context, Uri uri) {
        Cursor cursor = null;
        String path = null;
        try {
            // 获取ContentResolver对象
            ContentResolver contentResolver = context.getContentResolver();
            // 获取MIME类型
            String mimeType = contentResolver.getType(uri);
            // 如果是图片或视频等，则可能包含数据
            if (mimeType != null && mimeType.startsWith("image/")) {
                String[] projection = {MediaStore.Images.Media.DATA};
                // 查询数据
                cursor = contentResolver.query(uri, projection, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    // 获取数据的索引
                    int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                    if (columnIndex != -1) {
                        // 获取数据
                        path = cursor.getString(columnIndex);
                    }
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return path;
    }

}
