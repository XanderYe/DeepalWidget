package cn.xanderye.android.deepalwidget.service;

import android.app.Notification;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import cn.xanderye.android.deepalwidget.R;
import cn.xanderye.android.deepalwidget.provider.CarWidgetProvider;
import cn.xanderye.android.deepalwidget.util.AndroidUtil;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author yezhendong
 * @description:
 * @date 2023/3/16 10:25
 */
public class CarDataTimeService extends Service {

    private static final String TAG = CarDataTimeService.class.getSimpleName();

    private static ScheduledExecutorService scheduledExecutorService;

    private static final int PERIOD_TIME = 30;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "启动定时任务");
        startServiceForeground();
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdownNow();
        }
        scheduledExecutorService = Executors.newScheduledThreadPool(1);
        Context context = getApplicationContext();
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.car_data_widget);
            boolean carData = CarWidgetProvider.getCarData(context, remoteViews);
            updateView(remoteViews);
            String msg = "定时刷新成功";
            if (!carData) {
                msg = "定时刷新失败，请检查配置";
            }
            Log.d(TAG, msg);
        }, 0, PERIOD_TIME, TimeUnit.MINUTES);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "结束定时任务");
        scheduledExecutorService.shutdownNow();
        super.onDestroy();
    }

    private void updateView(RemoteViews remoteViews) {
        Log.d(TAG, "updateView");
        AppWidgetManager manager = AppWidgetManager.getInstance(getApplicationContext());
        ComponentName componentName = new ComponentName(getApplicationContext(), CarWidgetProvider.class);
        manager.updateAppWidget(componentName, remoteViews);
    }

    private void startServiceForeground() {
        String title = "深蓝小组件正在运行中";
        String text = "";
        Notification notification = AndroidUtil.getNotification(getApplicationContext(), R.drawable.icon, title, text);
        startForeground(110, notification);
    }
}
