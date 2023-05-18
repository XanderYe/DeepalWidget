package cn.xanderye.android.deepalwidget.provider;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import cn.xanderye.android.deepalwidget.R;
import cn.xanderye.android.deepalwidget.constant.Constants;
import cn.xanderye.android.deepalwidget.entity.CarData;
import cn.xanderye.android.deepalwidget.service.DeepalService;
import cn.xanderye.android.deepalwidget.util.AndroidUtil;
import com.alibaba.fastjson.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author yezhendong
 * @description:
 * @date 2023/3/16 9:59
 */
public class CarWidgetProvider extends AppWidgetProvider {

    private static final String TAG = CarWidgetProvider.class.getSimpleName();

    public static final String OPEN_DEEPAL_WIDGET = "cn.xanderye.android.OPEN_DEEPAL_WIDGET";

    public static final String OPEN_AMAP_WIDGET = "cn.xanderye.android.OPEN_AMAP_WIDGET";

    public static final String REFRESH_WIDGET = "cn.xanderye.android.REFRESH_WIDGET";

    @Override
    public void onEnabled(Context context) {
        Log.d(TAG, "widget onEnabled");
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        Log.d(TAG, "widget onDisabled");
        super.onDisabled(context);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        Log.d(TAG, "widget onDeleted");
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "widget onReceive, action: " + action);
        switch (action) {
            case OPEN_DEEPAL_WIDGET:
                PackageManager packageManager = context.getPackageManager();
                Intent appIntent = packageManager.getLaunchIntentForPackage(Constants.DEEPAL_PACKAGE_NAME);
                if (appIntent != null) {
                    try {
                        context.startActivity(appIntent);
                    } catch (Exception e) {
                        Log.d(TAG, "深蓝打开失败，原因：" + e.getMessage());
                        Toast.makeText(context, "打开失败", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "请先安装深蓝app", Toast.LENGTH_SHORT).show();
                }
                break;
            case REFRESH_WIDGET:
                refresh(context, false);
                break;
            case AppWidgetManager.ACTION_APPWIDGET_UPDATE:
                Log.d(TAG, "触发系统刷新");
                refresh(context, true);
                break;
            case OPEN_AMAP_WIDGET:
                ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
                singleThreadExecutor.execute(() -> {
                    Looper.prepare();
                    Toast.makeText(context, "更新定位信息并导航", Toast.LENGTH_SHORT).show();
                    RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.car_data_widget);
                    getCarLocation(context, remoteViews);
                    refreshWidget(context, remoteViews);
                    Looper.loop();
                });
                singleThreadExecutor.shutdown();
                break;
        }
        RemoteViews remoteViews = bindButton(context);
        refreshWidget(context, remoteViews);
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "widget onUpdate");

        for (int appWidgetId : appWidgetIds) {
            Log.d(TAG, "widget id: " + appWidgetId);
            RemoteViews remoteViews = bindButton(context);
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        Log.d(TAG, "onAppWidgetOptionsChanged");
        refresh(context, true);
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    private void refresh(Context context, boolean auto) {
        ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
        singleThreadExecutor.execute(() -> {
            Looper.prepare();
            if (!auto) {
                Toast.makeText(context, "刷新...", Toast.LENGTH_SHORT).show();
            }
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.car_data_widget);
            boolean hasCarData = getCarData(null, context, remoteViews);
            refreshWidget(context, remoteViews);
            if (!auto) {
                String msg = "刷新成功";
                if (!hasCarData) {
                    msg = "刷新失败，请检查配置";
                }
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            }
            Looper.loop();
        });
        singleThreadExecutor.shutdown();

    }

    public static RemoteViews bindButton(Context context) {
        int flag;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            flag = PendingIntent.FLAG_IMMUTABLE;
        } else {
            flag = PendingIntent.FLAG_UPDATE_CURRENT;
        }
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.car_data_widget);
        Intent btIntent = new Intent(context, CarWidgetProvider.class).setAction(REFRESH_WIDGET);
        PendingIntent btPendingIntent = PendingIntent.getBroadcast(context, 0, btIntent, flag);
        remoteViews.setOnClickPendingIntent(R.id.iv_refresh, btPendingIntent);

        Intent appIntent = new Intent(context, CarWidgetProvider.class).setAction(OPEN_DEEPAL_WIDGET);
        PendingIntent appPendingIntent = PendingIntent.getBroadcast(context, 0, appIntent, flag);
        remoteViews.setOnClickPendingIntent(R.id.carImg, appPendingIntent);
        Intent amapIntent = new Intent(context, CarWidgetProvider.class).setAction(OPEN_AMAP_WIDGET);
        PendingIntent amapPendingIntent = PendingIntent.getBroadcast(context, 0, amapIntent, flag);
        remoteViews.setOnClickPendingIntent(R.id.location_layout, amapPendingIntent);
        return remoteViews;
    }

    public static boolean getCarData(CarData carData, Context context, RemoteViews remoteViews) {
        if (carData == null) {
            DeepalService deepalService = DeepalService.getInstance();
            deepalService.setContext(context);
            carData = deepalService.getCarData();
        }
        if (carData != null) {
            Log.d(TAG, "车辆信息：" + carData);
            if (carData.getColor() != null) {
                Bitmap bitmap =  AndroidUtil.getImageFromAssetsFile(context, carData.getColor() + ".png");
                if (bitmap != null) {
                    remoteViews.setImageViewBitmap(R.id.carImg, bitmap);
                }
            }
            remoteViews.setTextViewText(R.id.carNameText, carData.getCarName());
            remoteViews.setTextViewText(R.id.plateNumberText, carData.getPlateNumber());
            remoteViews.setTextViewText(R.id.locationText, carData.getLocation());
            remoteViews.setTextViewText(R.id.terminalTimeText, carData.getTerminalTime());
            remoteViews.setTextViewText(R.id.totalOdometerText, carData.getTotalOdometer() + "km");

            int powerPercent = Double.valueOf(carData.getRemainPower()).intValue();

            boolean isOil = carData.getRemainedOilMile() != null;

            if (isOil) {
                // 增程版
                remoteViews.setViewVisibility(R.id.power_layout, View.GONE);
                remoteViews.setViewVisibility(R.id.oil_layout, View.VISIBLE);

                int oilPercent = 0;
                try {
                    oilPercent = (int) Double.parseDouble(carData.getRemainedOil());
                } catch (Exception ignored) {}

                remoteViews.setTextViewText(R.id.oil_remainedPowerMileText, carData.getRemainedPowerMile() + "km");
                remoteViews.setProgressBar(R.id.oil_powerProgress, 100, powerPercent, false);
                remoteViews.setTextViewText(R.id.oil_remainedOilMileText, carData.getRemainedOilMile() + "km");
                remoteViews.setProgressBar(R.id.oil_oilProgress, 100, oilPercent, false);
            } else {
                // 纯电版
                remoteViews.setViewVisibility(R.id.power_layout, View.VISIBLE);
                remoteViews.setViewVisibility(R.id.oil_layout, View.GONE);

                remoteViews.setTextViewText(R.id.remainedPowerText, powerPercent + "%");
                remoteViews.setTextViewText(R.id.remainedPowerMileText, carData.getRemainedPowerMile() + "km");
                remoteViews.setProgressBar(R.id.powerProgress, 100, powerPercent, false);
            }

            // 车锁状态
            if (carData.getLeftFrontDoorLock() == 0) {
                remoteViews.setTextViewText(R.id.lockStatusText, "已闭锁");
                remoteViews.setTextColor(R.id.lockStatusText, ContextCompat.getColor(context, R.color.lock_color));
                remoteViews.setViewVisibility(R.id.lockImg, View.VISIBLE);
                remoteViews.setViewVisibility(R.id.unlockImg, View.GONE);
            } else {
                remoteViews.setTextViewText(R.id.lockStatusText, "已解锁");
                remoteViews.setTextColor(R.id.lockStatusText, ContextCompat.getColor(context, R.color.unlock_color));
                remoteViews.setViewVisibility(R.id.lockImg, View.GONE);
                remoteViews.setViewVisibility(R.id.unlockImg, View.VISIBLE);
            }

            Bitmap bitmap;
            int chargeIconId = isOil ? R.id.oil_charge_icon : R.id.charge_icon;
            if (carData.getBatteryChargeStatus() != null && carData.getBatteryChargeStatus() == 1) {
                bitmap = AndroidUtil.getImageFromAssetsFile(context, "charge_icon.png");
                if (bitmap != null) {
                    remoteViews.setImageViewBitmap(chargeIconId, bitmap);
                }
            } else {
                bitmap = AndroidUtil.getImageFromAssetsFile(context, "power_icon.png");
                if (bitmap != null) {
                    remoteViews.setImageViewBitmap(chargeIconId, bitmap);
                }
            }
            return true;
        } else {
            Log.d(TAG, "车辆信息为null");
        }
        return false;
    }

    public static void getCarLocation(Context context, RemoteViews remoteViews) {
        DeepalService deepalService = DeepalService.getInstance();
        deepalService.setContext(context);
        JSONObject res = deepalService.getCarLocation(null);
        if (res.getInteger("code") == 0) {
            JSONObject data = res.getJSONObject("data");
            String lat = data.getString("lat");
            String lng = data.getString("lng");
            String location = data.getString("addrDesc");
            remoteViews.setTextViewText(R.id.locationText, location);
            try {
                Uri uri = Uri.parse("amapuri://route/plan/?dlat=" + lat + "&dlon=" + lng + "&dname=" + location + "&dev=0&t=0");
                Intent amapIntent = new Intent("android.intent.action.VIEW", uri);
                amapIntent.addCategory("android.intent.category.DEFAULT");
                amapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(amapIntent);
            } catch (Exception e) {
                Log.d(TAG, "高德打开失败，原因：" + e.getMessage());
                Toast.makeText(context, "请先安装高德app", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "未获取到定位信息", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 显示加载loading
     *
     */
    private void showLoading(Context context) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.car_data_widget);
        remoteViews.setViewVisibility(R.id.iv_refresh, View.VISIBLE);
        remoteViews.setTextViewText(R.id.iv_refresh, "正在刷新...");
        refreshWidget(context, remoteViews);
    }

    /**
     * 隐藏加载loading
     */
    private void hideLoading(Context context) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.car_data_widget);
        remoteViews.setTextViewText(R.id.iv_refresh, "刷新");
        refreshWidget(context, remoteViews);
    }

    private void refreshWidget(Context context, RemoteViews remoteViews) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName componentName = new ComponentName(context, CarWidgetProvider.class);
        appWidgetManager.updateAppWidget(componentName, remoteViews);
    }
}
