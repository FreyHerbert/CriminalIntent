package com.leiyun.criminalintent;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;

/**
 * Created by LeiYun on 2016/11/14 0014.
 */

public class PictureUtils {
    /**
     * 这个方法是用来解决Bitmap图片过大需要将图片缩小的这么一个方法
     * @param path 文件存放的位置
     * @param destWidth 需要的宽
     * @param destHeight 需要的高
     * @return
     */
    public static Bitmap getScaledBitmap(String path, int destWidth, int destHeight) {
        // 在磁盘上读取图片的尺寸
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        float srcWidth = options.outWidth;
        float srcHeight = options.outHeight;

        // 按比例下降
        int inSampleSize = 1;
        if (srcHeight > destHeight || srcWidth > destWidth) {
            if (srcWidth > srcHeight) {
                inSampleSize = Math.round(srcHeight / destHeight);
            }else {
                inSampleSize = Math.round(srcWidth / destWidth);
            }
        }

        options = new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;

        // 读取并最终创建bitmap
        return BitmapFactory.decodeFile(path, options);
    }

    /**
     * 方法使用来解决图片在初始化过程中，由于不能第一时间知道图片的大小，
     * 所以使用此方法进行图片大小的预估计，
     * 该方法先确定屏幕的尺寸，并由此来进行图片的缩放
     * @param path
     * @param activity
     * @return
     */
    public static Bitmap getScaledBitmap(String path, Activity activity) {
        Point size = new Point();
        activity.getWindowManager().getDefaultDisplay()
                .getSize(size);
        return getScaledBitmap(path, size.x, size.y);
    }
}
