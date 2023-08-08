package cn.xanderye.android.deepalwidget.constant;

import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

/**
 * @author yezhendong
 * @description:
 * @date 2023/3/16 11:03
 */
public class Constants {
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static final String TOKEN_KEY = "token";
    public static final String DEVICE_ID_KEY = "deviceId";
    public static final String REFRESH_TOKEN_KEY = "refreshToken";
    public static final String CAC_TOKEN_KEY = "cacToken";
    public static final String CAR_TYPE_KEY = "carType";
    public static final String CAR_DATA_KEY = "carData";
    public static final String MAX_OIL_KEY = "maxOil";
    public static final String DEEPAL_PACKAGE_NAME = "deepal.com.cn.app";
    public static final String LAST_UPDATE_KEY = "lastUpdate";
    public static final String OFFSET_MILE_KEY = "offsetMile";

    public static final String[] CAR_TYPE = new String[]{"SL03", "S7"};
    public static final String[] SL03_CAR_COLOR = new String[]{"星云青", "月岩灰", "彗星白", "星矿黑", "天河蓝"};
    public static final String[] S7_CAR_COLOR = new String[]{"月岩灰","星云青","冷星白","星耀黑", "极宇黄", "炽云橙"};

    public static final int DEFAULT_EXT_POWER_MILE = 186;
    public static final int DEFAULT_MILE_MILE = 846;

    public static final String ALERT_MESSAGE_KEY = "alertMessage";

    public static final String CHECK_APPLY_KEY = "checkApply";

    public static final String WECHAT_PACKAGE_NAME = "com.tencent.mm";
}
