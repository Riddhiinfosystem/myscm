package support;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.CursorLoader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;


import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.riddhi.myscm.MainActivity;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Vinayak on 3/8/2017.
 */

public class ImageResizer {

    Context context;
    private static final int REQUEST_READ_PERMISSION = 123;

    public ImageResizer(){}
    public ImageResizer(Context mContext)
    {
        context = mContext;
    }

    @SuppressLint("NewApi")
    public static String getRealPathFromURI_API11to18(Context context, Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        String result = null;

        CursorLoader cursorLoader = new CursorLoader(
                context,
                contentUri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();

        if(cursor != null){
            int column_index =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            result = cursor.getString(column_index);
        }
        return result;
    }

    public boolean resizeImageBitmap(String destImagePath,Uri uri,Context context) throws IOException
    {

        int width = 0; int height = 0; int minWidth = 1024; int minHeight = 768;
        boolean isResized = false;

        Bitmap bmp=null;

        double ratio = 5;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        if ((options.outWidth > 3400)) {
            ratio = 5;
        } else if ((options.outWidth > 2400)) {
            ratio = 4;
        } else if ((options.outWidth > 1600)) {
            ratio = 3;
        } else if ((options.outWidth > 1200)) {
            ratio = 2;
        } else if ((options.outWidth > 800)) {
            ratio = 1;
        }


        try {
            BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options);
            //options.inSampleSize = calculateInSampleSize(options, 100, 100);
            options.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
            options.inJustDecodeBounds = false;
            bmp = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (bmp != null)
        {
            /*if (bmp.getWidth() > minWidth && bmp.getWidth() > bmp.getHeight())
            {
                width = minWidth;
                height = Math.round(((float) bmp.getHeight() / (float) bmp.getWidth()) * minWidth);
            }
            else
            {
                height = minHeight;
                width = Math.round(((float) bmp.getWidth() / (float) bmp.getHeight()) * minHeight);
            }

            Bitmap bScaled = Bitmap.createScaledBitmap(bmp,width, height,true);*/

            try {
                if (CheckPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                {
                    if (CheckPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        FileOutputStream fout = new FileOutputStream(destImagePath, true);
                        bmp.compress(Bitmap.CompressFormat.JPEG, 100, fout);
                        //bScaled.compress(Bitmap.CompressFormat.JPEG, 100, fout);
                        //bmp.recycle();
                        fout.flush();
                        fout.close();
                        isResized = true;
                    } else
                    {
                        RequestPermission(((MainActivity) context), Manifest.permission.READ_EXTERNAL_STORAGE, REQUEST_READ_PERMISSION);
                    }
                }
                else
                {
                    RequestPermission(((MainActivity) context),Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_READ_PERMISSION);
                }
            }catch (Exception e)
            {
                e.printStackTrace();
            }

        }
        return isResized;


    }

    private static int getPowerOfTwoForSampleRatio(double ratio) {
        int k = Integer.highestOneBit((int) Math.floor(ratio));
        if (k == 0)
            return 1;
        else
            return k;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public boolean resizeImage(String sourceImagePath, String destImagePath) throws IOException {

        double ratio = 5;
        Bitmap bmp = null;
        try {

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            if ((options.outWidth > 3400)) {
                ratio = 5;
            } else if ((options.outWidth > 2400)) {
                ratio = 4;
            } else if ((options.outWidth > 1600)) {
                ratio = 3;
            } else if ((options.outWidth > 1200)) {
                ratio = 2;
            } else if ((options.outWidth > 800)) {
                ratio = 1;
            }

            options.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
            options.inJustDecodeBounds = false;

            bmp = BitmapFactory.decodeFile(sourceImagePath,options);


        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        int width = 0; int height = 0; int minWidth = 1024; int minHeight = 768;
        boolean isResized = false;

        if (bmp != null)
        {

            /*if (bmp.getWidth() > minWidth && bmp.getWidth() > bmp.getHeight())
            {
                width = minWidth;
                height = Math.round(((float) bmp.getHeight() / (float) bmp.getWidth()) * minWidth);
            }
            else
            {
                height = minHeight;
                width = Math.round(((float) bmp.getWidth() / (float) bmp.getHeight()) * minHeight);
            }

            Bitmap bScaled = Bitmap.createScaledBitmap(bmp,width, height,true);*/

            try {
                if (CheckPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                {
                    if (CheckPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        FileOutputStream fout = new FileOutputStream(destImagePath, true);
                        bmp.compress(Bitmap.CompressFormat.JPEG, 100, fout);
                        //bScaled.compress(Bitmap.CompressFormat.JPEG, 100, fout);
                        //bmp.recycle();
                        fout.flush();
                        fout.close();
                        isResized = true;
                    } else
                    {
                        RequestPermission(((MainActivity) context), Manifest.permission.READ_EXTERNAL_STORAGE, REQUEST_READ_PERMISSION);
                    }
                }
                else
                {
                    RequestPermission(((MainActivity) context),Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_READ_PERMISSION);
                }
            }catch (Exception e)
            {
                e.printStackTrace();
            }

        }
        return isResized;
    }

    public static boolean CheckPermission(Context context, String Permission) {
        if (ContextCompat.checkSelfPermission(context,
                Permission) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public static void RequestPermission(Activity thisActivity, String Permission, int Code) {
        if (ContextCompat.checkSelfPermission(thisActivity,
                Permission)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(thisActivity,
                    Permission)) {
            } else {
                ActivityCompat.requestPermissions(thisActivity,
                        new String[]{Permission},
                        Code);
            }
        }
    }
}
