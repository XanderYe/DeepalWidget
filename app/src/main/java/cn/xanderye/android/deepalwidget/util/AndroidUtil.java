package cn.xanderye.android.deepalwidget.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author XanderYe
 * @description:
 * @date 2023/3/18 10:07
 */
public class AndroidUtil {
    /**
     * 创建通知
     * @param context
     * @param icon
     * @param title
     * @param content
     * @return android.app.Notification
     * @author XanderYe
     * @date 2023/2/4
     */
    public static Notification getNotification(Context context, int icon, String title, String content) {
        String channelId = context.getPackageName();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId);
        builder.setSmallIcon(icon)
                .setContentTitle(title)
                .setContentText(content)
                .setWhen(System.currentTimeMillis());
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(channelId,
                    "后台服务", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.enableLights(false);//是否在桌面icon右上角展示小圆点
            notificationChannel.setLightColor(Color.BLUE); //小圆点颜色
            notificationChannel.setShowBadge(true); //是否在久按桌面图标时显示此渠道的通知
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        return builder.build();
    }

    /**
     * 服务保活
     * @param context
     * @return void
     * @author XanderYe
     * @date 2023/2/4
     */
    public static void startAliveService(Context context, Class<?> clazz) {
        Intent startIntent = new Intent(context, clazz);
        startIntent.setAction("android.intent.action.RESPOND_VIA_MESSAGE");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(startIntent);
        } else {
            context.startService(startIntent);
        }
    }

    /**
     * 结束服务
     * @param context
     * @return void
     * @author XanderYe
     * @date 2023/2/4
     */
    public static void stopAliveService(Context context, Class<?> clazz) {
        Intent stopIntent = new Intent(context, clazz);
        context.stopService(stopIntent);
    }

    public static Bitmap getImageFromAssetsFile(Context context, String fileName) {
        Bitmap image = null;
        AssetManager am = context.getResources().getAssets();
        if (am == null) {
            return null;
        }
        try {
            InputStream is = am.open(fileName);
            image = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }
}
