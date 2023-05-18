package cn.xanderye.android.deepalwidget.util;

import android.util.Log;
import cn.xanderye.android.deepalwidget.constant.Constants;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @author XanderYe
 * @description:
 * @date 2023/3/8 13:44
 */
public class DeepalUtil {
    private static final String TAG = DeepalUtil.class.getSimpleName();

    private static final String DEEPAL_ORIGIN = "https://app-api.deepal.com.cn";
    private static final String CHANGAN_ORIGIN = "https://m.iov.changan.com.cn";


    /**
     * 用accessToken获取车辆信息
     * @param accessToken
     * @return com.alibaba.fastjson.JSONObject
     * @author XanderYe
     * @date 2023/3/16
     */
    public static JSONObject getCarByToken(String token, String accessToken) throws IOException {
        String url = DEEPAL_ORIGIN + "/appapi/v1/message/msg/cars";
        Map<String, Object> headerMap = getHeaderMap();
        headerMap.put("authorization", token);
        JSONObject params = new JSONObject();
        params.put("token", accessToken);
        params.put("type", 1);
        params.put("vcs-app-id", "inCall");
        HttpUtil.ResEntity resEntity = HttpUtil.doPostJSON(url, headerMap, null, params.toJSONString());
        Log.d(TAG, "获取车辆列表结果：" + resEntity.getResponse());
        return JSON.parseObject(resEntity.getResponse());
    }

    /**
     * 刷新token
     * @param refreshToken
     * @return com.alibaba.fastjson.JSONObject
     * @author XanderYe
     * @date 2023/3/16
     */
    public static JSONObject refreshCacToken(String refreshToken) throws IOException {
        String url = DEEPAL_ORIGIN + "/appapi/v1/member/ms/refreshCacToken";
        Map<String, Object> headerMap = getHeaderMap();
        JSONObject params = new JSONObject();
        params.put("refreshToken", refreshToken);
        Log.d(TAG, "刷新token，refreshToken：" + refreshToken);
        HttpUtil.ResEntity resEntity = HttpUtil.doPostJSON(url, headerMap, null, params.toJSONString());
        Log.d(TAG, "刷新token结果：" + resEntity.getResponse());
        return JSON.parseObject(resEntity.getResponse());
    }

    /**
     * 获取车控token
     * @param authToken
     * @return com.alibaba.fastjson.JSONObject
     * @author XanderYe
     * @date 2023/3/16
     */
    public static JSONObject getCacTokenByAuthToken(String authToken) throws IOException {
        String url = DEEPAL_ORIGIN + "/appapi/v1/member/ms/cacToken";
        Map<String, Object> headerMap = getHeaderMap();
        headerMap.put("Authorization", authToken);
        HttpUtil.ResEntity resEntity = HttpUtil.doGet(url, headerMap, null, null);
        Log.d(TAG, "获取token结果：" + resEntity.getResponse());
        return JSON.parseObject(resEntity.getResponse());
    }

    /**
     * 获取配置信息
     * @param cacToken
     * @return com.alibaba.fastjson.JSONObject
     * @author XanderYe
     * @date 2023/4/4
     */
    public static JSONObject getBaseConfig(String cacToken) throws IOException {
        String url = CHANGAN_ORIGIN + "/appserver/api/config/getBaseConfig?token=" + cacToken;
        Map<String, Object> headerMap = getHeaderMap();
        Log.d(TAG, "获取配置信息：" + cacToken);
        HttpUtil.ResEntity resEntity = HttpUtil.doPost(url, headerMap, null, null);
        Log.d(TAG, "获取配置信息结果：" + resEntity.getResponse());
        return JSON.parseObject(resEntity.getResponse());
    }

    /**
     * 获取车辆信息
     * @param accessToken
     * @param carId
     * @return com.alibaba.fastjson.JSONObject
     * @author XanderYe
     * @date 2023/3/16
     */
    public static JSONObject getCarData(String accessToken, String carId) throws IOException {
        String url = CHANGAN_ORIGIN + "/app2/api/car/data";
        Map<String, Object> headerMap = getHeaderMap();
        JSONObject params = new JSONObject();
        params.put("carId", carId);
        params.put("keys", "*");
        params.put("token", accessToken);
        Log.d(TAG, "获取Car信息，请求体：" + params.toJSONString());
        HttpUtil.ResEntity resEntity = HttpUtil.doPost(url, headerMap, null, params);
        Log.d(TAG, "获取Car信息结果：" + resEntity.getResponse());
        return JSON.parseObject(resEntity.getResponse());
    }

    /**
     * 获取车辆定位
     * @param accessToken
     * @param carId
     * @return com.alibaba.fastjson.JSONObject
     * @author XanderYe
     * @date 2023/4/24
     */
    public static JSONObject getCarLocation(String accessToken, String carId) throws IOException {
        String url = CHANGAN_ORIGIN + "/appserver/api/cardata/getCarLocation";
        Map<String, Object> headerMap = getHeaderMap();
        JSONObject params = new JSONObject();
        params.put("carId", carId);
        params.put("mapType", "GCJ02");
        params.put("token", accessToken);
        Log.d(TAG, "获取定位，请求体：" + params.toJSONString());
        HttpUtil.ResEntity resEntity = HttpUtil.doPost(url, headerMap, null, params);
        Log.d(TAG, "获取定位信息结果：" + resEntity.getResponse());
        return JSON.parseObject(resEntity.getResponse());
    }

    /**
     * 获取流量信息
     * @param accessToken
     * @param carId
     * @return com.alibaba.fastjson.JSONObject
     * @author XanderYe
     * @date 2023/4/24
     */
    public static JSONObject getCellularData(String accessToken, String carId) throws IOException {
        String url = CHANGAN_ORIGIN + "/appserver/api/huservice/balanceInfo";
        Map<String, Object> headerMap = getHeaderMap();
        headerMap.put("X-Requested-With", "deepal.com.cn.app");
        String paramStr = "?carId=" + carId + "&token=" + accessToken;
        url += paramStr;
        Log.d(TAG, "获取流量，请求体：" + paramStr);
        HttpUtil.ResEntity resEntity = HttpUtil.doPost(url, headerMap, null, null);
        Log.d(TAG, "获取流量信息结果：" + resEntity.getResponse());
        return JSON.parseObject(resEntity.getResponse());
    }

    /**
     * 获取充电信息
     * @param accessToken
     * @param carId
     * @return com.alibaba.fastjson.JSONObject
     * @author yezhendong
     * @date 2023/4/24
     */
    public static JSONObject getChargeInfo(String accessToken, String carId) throws IOException {
        String url = CHANGAN_ORIGIN + "/appserver/api/charge/info";
        Map<String, Object> headerMap = getHeaderMap();
        headerMap.put("X-Requested-With", "deepal.com.cn.app");
        String paramStr = "?carId=" + carId + "&token=" + accessToken;
        url += paramStr;
        Log.d(TAG, "获取充电状态，请求体：" + paramStr);
        HttpUtil.ResEntity resEntity = HttpUtil.doPost(url, headerMap, null, null);
        Log.d(TAG, "获取充电状态信息结果：" + resEntity.getResponse());
        return JSON.parseObject(resEntity.getResponse());
    }

    public static JSONObject getControlActionHistory(String accessToken, String carId) throws IOException {
        String url = CHANGAN_ORIGIN + "/appserver/api/car/getControlActionHistory";
        Map<String, Object> headerMap = getHeaderMap();
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusMonths(6);
        Map<String, Object> params = new HashMap<>();
        params.put("carId", carId);
        params.put("pageSize", 20);
        params.put("page", 0);
        params.put("startTime", startTime.format(Constants.DATE_FORMAT));
        params.put("endTime", endTime.format(Constants.DATE_FORMAT));
        params.put("toast", false);
        params.put("ErrorAutoProjectile", false);
        params.put("token", accessToken);
        params.put("isNev", 0);
        params.put("type", 0);
        params.put("deviceId", "deviceId");
        Log.d(TAG, "获取车控历史，请求体：" + params);
        HttpUtil.ResEntity resEntity = HttpUtil.doPost(url, headerMap, null, params);
        Log.d(TAG, "获取车控历史结果：" + resEntity.getResponse());
        return JSON.parseObject(resEntity.getResponse());
    }

    public static Map<String, Object> getHeaderMap() {
        Map<String, Object> headerMap = new HashMap<>();
        String appVer = "1.1.2";
        headerMap.put("User-Agent", "CAQC/" + appVer + " (iPhone; iOS 15.6; Scale/2.00)");
        headerMap.put("Application-Version-Secret", "1661530310.000000");
        headerMap.put("appId", "sl_ios");
        headerMap.put("appType", "ios");
        headerMap.put("version", appVer);
        headerMap.put("appVersion", appVer);
        headerMap.put("deviceId", "deviceId");
        headerMap.put("environment", "dev");
        headerMap.put("packageName", "deepal.com.cn.app");
        return headerMap;
    }

    /**
     * 注册设备
     * @param androidId
     * @param deviceName
     * @return void
     * @author XanderYe
     * @date 2023/3/22
     */
    public static void register(String androidId, String deviceName, String version) {
        String url = "https://tool.xanderye.cn/api/deepal/register";
        Map<String, Object> params = new HashMap<>();
        params.put("androidId", androidId);
        params.put("name", deviceName);
        params.put("version", version);
        Log.d(TAG, "注册设备：" + params);
        try {
            HttpUtil.ResEntity resEntity = HttpUtil.doPost(url, params);
            Log.d(TAG, "注册设备返回结果：" + resEntity.getResponse());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 检查更新
     * @return void
     * @author XanderYe
     * @date 2023/3/22
     */
    public static JSONObject checkUpdate() throws IOException {
        String url = "https://tool.xanderye.cn/api/deepal/checkUpdate";
        HttpUtil.ResEntity resEntity = HttpUtil.doGet(url, null);
        return JSON.parseObject(resEntity.getResponse());
    }

    public static JSONObject apply(String carId, String note) throws IOException {
        String url = "https://tool.xanderye.cn/api/deepal/apply";
        Map<String, Object> params = new HashMap<>();
        params.put("carId", carId);
        params.put("note", note);
        Log.d(TAG, "申请权限：" + carId);
        HttpUtil.ResEntity resEntity = HttpUtil.doPost(url, params);
        return JSON.parseObject(resEntity.getResponse());
    }

    public static JSONObject checkApply(String carId) throws IOException {
        String url = "https://tool.xanderye.cn/api/deepal/checkApply?carId=" + carId;
        Log.d(TAG, "检查权限：" + carId);
        HttpUtil.ResEntity resEntity = HttpUtil.doGet(url, null);
        return JSON.parseObject(resEntity.getResponse());
    }
}
