package cn.xanderye.android.deepalwidget.util;

/**
 * @author XanderYe
 * @description:
 * @date 2023/4/21 10:01
 */
public class CommonUtil {

    public static String formatTime(int time) {
        //  分
        int minute = time;
        //  小时
        int hour = 0;
        if (minute >= 60) {
            hour = minute / 60;
            minute = minute % 60;
        }

        String result = minute + "分";
        if (hour > 0) {
            result = hour + "小时" + result;
        }
        return result;
    }

    public static int parseVersion(String version) {
        String versionCode = version.replace(".", "");
        try {
            return Integer.parseInt(versionCode);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
