package com.example.samparka;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

public class RealPathUtil {

    public static String getRealPath(Context context, Uri uri) {

        String filePath = null;

        // For Android 11+ (API 30+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return copyFileToInternalStorage(context, uri, "samparka_images");
        }

        // Older versions
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(uri, proj,
                null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            filePath = cursor.getString(column_index);
            cursor.close();
        }

        return filePath;
    }

    private static String copyFileToInternalStorage(Context context, Uri uri, String newDirName) {
        String filePath = "";
        try {
            java.io.InputStream input = context.getContentResolver().openInputStream(uri);
            java.io.File directory = new java.io.File(context.getFilesDir() + "/" + newDirName);

            if (!directory.exists()) {
                directory.mkdir();
            }

            java.io.File file = new java.io.File(directory, "temp_image.jpg");
            java.io.OutputStream output = new java.io.FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            input.close();
            output.close();

            filePath = file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return filePath;
    }
}
