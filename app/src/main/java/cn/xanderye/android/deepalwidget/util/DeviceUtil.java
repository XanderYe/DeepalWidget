package cn.xanderye.android.deepalwidget.util;

import android.content.Context;
import android.provider.Settings;

import java.lang.reflect.Method;

/**
 * @author XanderYe
 * @description:
 * @date 2023/3/22 20:10
 */
public class DeviceUtil {

    public static final String PRODUCT_NAME = "ro.product.name";

    public static String getAndroidId(Context context) {
        return Settings.System.getString(context.getContentResolver(), Settings.System.ANDROID_ID);
    }

    public static String getDeviceName() {
        return getProperty(PRODUCT_NAME, "");
    }

    /**
     * 获取系统配置
     * @param key
     * @param defaultValue
     * @return java.lang.String
     * @author XanderYe
     * @date 2023/3/10
     */
    public static String getProperty(String key, String defaultValue) {
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String.class);
            String value = (String) get.invoke(c, key, defaultValue);
            return value;
        } catch (Exception e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

}
