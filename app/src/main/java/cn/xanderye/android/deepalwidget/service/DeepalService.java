package cn.xanderye.android.deepalwidget.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import cn.xanderye.android.deepalwidget.MainActivity;
import cn.xanderye.android.deepalwidget.constant.Constants;
import cn.xanderye.android.deepalwidget.entity.CarData;
import cn.xanderye.android.deepalwidget.util.DeepalUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;

/**
 * @author XanderYe
 * @description:
 * @date 2023/3/16 13:53
 */
public class DeepalService {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Context context = null;

    private JSONObject carDataJSON;
    private String token = null;
    private String refreshToken = null;
    private String cacToken = null;
    private String carType = "SL03";
    private int maxOil;
    private int offsetMile;

    private static volatile DeepalService deepalService;

    public static DeepalService getInstance() {
        if (deepalService == null) {
            synchronized (DeepalService.class) {
                if (deepalService == null) {
                    deepalService = new DeepalService();
                }
            }
        }
        return deepalService;
    }

    public CarData getCarData() {
        if (carDataJSON == null || carDataJSON.getString("carId") == null) {
            return null;
        }
        String cacToken = getCacToken();
        if (cacToken == null) {
            return null;
        }
        try {
            String carId = carDataJSON.getString("carId");
            JSONObject res = DeepalUtil.getCarData(cacToken, carId);
            if (res.getInteger("code") == 0) {
                JSONObject data = res.getJSONObject("data");
                CarData carData = new CarData();
                carData.setType(carType);
                carData.setColor(carDataJSON.getString("color"));
                carData.setCarName(carDataJSON.getString("carName"));
                carData.setPlateNumber(carDataJSON.getString("plateNumber"));
                carData.setVehicleTemperature(data.getString("vehicleTemperature"));
                carData.setRemainedPowerMile(data.getString("remainedPowerMile"));
                carData.setRemainPower(data.getString("remainPower"));
                carData.setServerTime(data.getString("serverTime"));
                carData.setTerminalTime(data.getString("terminalTime"));
                carData.setLeftFrontDoorLock(data.getInteger("leftFrontDoorLock"));
                carData.setDriverDoor(data.getInteger("driverDoor"));
                carData.setPassengerDoor(data.getInteger("passengerDoor"));
                carData.setLeftRearDoor(data.getInteger("leftRearDoor"));
                carData.setRightRearDoor(data.getInteger("rightRearDoor"));
                carData.setBatteryChargeStatus(data.getInteger("batteryChargeStatus"));
                carData.setAcChargeGunConnectionState(data.getInteger("acChargeGunConnectionState"));
                carData.setDcDhargeGunConnectionState(data.getInteger("dcDhargeGunConnectionState"));
                carData.setObcChrgInpAcI(data.getDouble("obcChrgInpAcI"));
                carData.setTotalVoltage(data.getDouble("totalVoltage"));
                carData.setTotalCurrent(data.getDouble("totalCurrent"));
                carData.setChargDeltMins(data.getInteger("chargDeltMins"));
                String remainedOilMile = data.getString("remainedOilMile");
                // 总里程偏移
                int totalOdometer = Double.valueOf(data.getString("totalOdometer")).intValue();
                if (offsetMile > 0) {
                    totalOdometer = totalOdometer + offsetMile;
                }
                carData.setTotalOdometer(String.valueOf(totalOdometer));
                if (remainedOilMile != null) {
                    if ("0".equals(carData.getRemainedPowerMile())) {
                        try {
                            double powerPercent = Double.parseDouble(carData.getRemainPower());
                            int powerMile = (int) (powerPercent * Constants.DEFAULT_EXT_POWER_MILE / 100);
                            carData.setRemainedPowerMile(String.valueOf(powerMile));
                        } catch (Exception e){
                            Log.d(TAG, "数据格式化错误：" + e.getMessage());
                        }
                    }
                    double oilMile = 0;
                    try {
                        oilMile = Double.parseDouble(remainedOilMile);
                    } catch (Exception e){
                        Log.d(TAG, "数据格式化错误：" + e.getMessage());
                    }
                    if (oilMile < 0) {
                        oilMile = 0;
                    }
                    if (oilMile > 0) {
                        double oilPercent = oilMile / maxOil * 100;
                        Log.d(TAG, "纯油续航：" + oilPercent);
                        DecimalFormat decimalFormat = new DecimalFormat("#.0");
                        carData.setRemainedOil(decimalFormat.format(oilPercent));
                        carData.setRemainedOilMile(remainedOilMile);
                    } else {
                        carData.setRemainedOil("0");
                        carData.setRemainedOilMile("0");
                    }
                }
                res = getCarLocation(cacToken);
                if (res != null && res.containsKey("code")) {
                    if (res.getInteger("code") == 0) {
                        data = res.getJSONObject("data");
                        carData.setLng(data.getDouble("lng"));
                        carData.setLat(data.getDouble("lat"));
                        carData.setLocation(data.getString("addrDesc"));
                    }
                }

                LocalDateTime localDateTime = LocalDateTime.now();
                String time = Constants.DATE_FORMAT.format(localDateTime);
                SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor edit = config.edit();
                edit.putString(Constants.LAST_UPDATE_KEY, time);
                edit.apply();
                return carData;
            } else {
                Log.d(TAG, "获取车辆信息返回 " + res.toJSONString());
            }
        } catch (IOException e) {
            Log.d(TAG, "获取车辆信息失败，原因：" + e.getMessage());
        }
        return null;
    }

    public JSONObject getCarLocation(String cacToken) {
        if (carDataJSON == null) {
            return null;
        }
        String carId = carDataJSON.getString("carId");
        if (cacToken == null) {
            cacToken = getCacToken();
        }
        try {
            return DeepalUtil.getCarLocation(cacToken, carId);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "获取车辆定位失败，原因：" + e.getMessage());
        }
        return null;
    }

    public String getCellularData(String cacToken) {
        if (carDataJSON == null) {
            return null;
        }
        String carId = carDataJSON.getString("carId");
        if (cacToken == null) {
            cacToken = getCacToken();
        }
        try {
            JSONObject res = DeepalUtil.getCellularData(cacToken, carId);
            if (res.getInteger("code") == 0) {
                JSONArray data = res.getJSONArray("data");
                if (!data.isEmpty()) {
                    JSONObject cellular = data.getJSONObject(0);
                    String left = cellular.getString("left");
                    String total = cellular.getString("total");
                    String unit = cellular.getString("totalUnit");
                    return left + "/" + total + " " + unit;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "获取流量失败，原因：" + e.getMessage());
        }
        return null;
    }

    public JSONArray getControlHistory(String cacToken) {
        if (carDataJSON == null) {
            return null;
        }
        String carId = carDataJSON.getString("carId");
        if (cacToken == null) {
            cacToken = getCacToken();
        }
        try {
            JSONObject res = DeepalUtil.getControlActionHistory(cacToken, carId);
            if (res.getInteger("code") == 0) {
                return res.getJSONObject("data").getJSONArray("data");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "获取操作历史失败，原因：" + e.getMessage());
        }
        return null;
    }

    public String getCacToken() {
        try {
            boolean needRefresh = true;
            if (cacToken != null) {
                needRefresh = !checkToken(cacToken);
            }
            if (needRefresh) {
                Log.d(TAG, "需要刷新token");
                if (token != null) {
                    JSONObject res = DeepalUtil.getCacTokenByAuthToken(token);
                    if (res.getInteger("code") != 200) {
                        Log.d(TAG, "用token刷新cacToken返回 " + res.toJSONString());
                        return null;
                    }
                    JSONObject data = res.getJSONObject("data");
                    String accessToken = data.getString("cacToken");
                    String refreshToken = data.getString("refreshToken");
                    SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(context);
                    SharedPreferences.Editor edit = config.edit();
                    edit.putString(Constants.CAC_TOKEN_KEY, accessToken);
                    edit.putString(Constants.REFRESH_TOKEN_KEY, refreshToken);
                    edit.apply();
                    return accessToken;
                } else if (refreshToken != null) {
                    JSONObject jsonObject = DeepalUtil.refreshCacToken(refreshToken);
                    if (jsonObject.getInteger("code") == 200) {
                        JSONObject data = jsonObject.getJSONObject("data");
                        String newRefreshToken = data.getString("refresh_token");
                        if (!refreshToken.equals(newRefreshToken)) {
                            Log.d(TAG, "refreshToken变动，更新");
                            refreshToken = newRefreshToken;
                            SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(context);
                            SharedPreferences.Editor edit = config.edit();
                            edit.putString(Constants.REFRESH_TOKEN_KEY, refreshToken);
                            edit.apply();
                        }
                        String accessToken = data.getString("access_token");
                        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(context);
                        SharedPreferences.Editor edit = config.edit();
                        edit.putString(Constants.CAC_TOKEN_KEY, accessToken);
                        edit.apply();
                        return accessToken;
                    } else {
                        Log.d(TAG, "用refreshToken刷新cacToken返回 " + jsonObject.toJSONString());
                    }
                }
            }
            return cacToken;
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "刷新cacToken失败，原因：" + e.getMessage());
        }
        return null;
    }

    public boolean checkToken(String cacToken) {
        try {
            JSONObject res = DeepalUtil.getBaseConfig(cacToken);
            return res.getInteger("code") == 0;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void setContext(Context context) {
        if (this.context == null) {
            this.context = context;
        }
        init();
    }
    public void init() {
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(context);
        carType = config.getString(Constants.CAR_TYPE_KEY, "SL03");
        String carDataStr = config.getString(Constants.CAR_DATA_KEY, null);
        if (carDataStr != null) {
            carDataJSON = JSON.parseObject(carDataStr);
        }
        token = config.getString(Constants.TOKEN_KEY, null);
        refreshToken = config.getString(Constants.REFRESH_TOKEN_KEY, null);
        cacToken = config.getString(Constants.CAC_TOKEN_KEY, null);
        maxOil = config.getInt(Constants.MAX_OIL_KEY, Constants.DEFAULT_MILE_MILE);
        offsetMile = config.getInt(Constants.OFFSET_MILE_KEY, 0);
        Log.d(TAG, "初始化数据，carData: " + carDataStr + ", maxOil: " + maxOil + ", offsetMile:" + offsetMile);
    }

    public JSONObject getCarDataJSON() {
        return carDataJSON;
    }
}
