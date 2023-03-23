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
    private String refreshToken = null;
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
                carData.setColor(carDataJSON.getString("color"));
                carData.setCarName(carDataJSON.getString("carName"));
                carData.setPlateNumber(carDataJSON.getString("plateNumber"));
                carData.setVehicleTemperature(data.getString("vehicleTemperature"));
                carData.setRemainedPowerMile(data.getString("remainedPowerMile"));
                carData.setRemainPower(data.getString("remainPower"));
                carData.setServerTime(data.getString("serverTime"));
                carData.setTerminalTime(data.getString("terminalTime"));
                carData.setDriverDoorLock(data.getInteger("driverDoorLock"));
                carData.setPassengerDoorLock(data.getInteger("passengerDoorLock"));
                carData.setChargeStatus(data.getInteger("chargeStatus"));
                String remainedOilMile = data.getString("remainedOilMile");
                // 总里程偏移
                String totalOdometer = data.getString("totalOdometer");
                if (offsetMile > 0) {
                    totalOdometer = String.valueOf(Double.valueOf(totalOdometer).intValue() + offsetMile);
                }
                carData.setTotalOdometer(totalOdometer);
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
                if (res.getInteger("code") == 0) {
                    data = res.getJSONObject("data");
                    carData.setLng(data.getDouble("lng"));
                    carData.setLat(data.getDouble("lat"));
                    carData.setLocation(data.getString("addrDesc"));
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

    public String getCacToken() {
        try {
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
                return data.getString("access_token");
            } else {
                Log.d(TAG, "刷新cacToken返回 " + jsonObject.toJSONString());
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "刷新cacToken失败，原因：" + e.getMessage());
        }
        return null;
    }

    public void setContext(Context context) {
        if (this.context == null) {
            this.context = context;
        }
        init();
    }
    public void init() {
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(context);
        String carDataStr = config.getString(Constants.CAR_DATA_KEY, null);
        if (carDataStr != null) {
            carDataJSON = JSON.parseObject(carDataStr);
        }
        refreshToken = config.getString(Constants.REFRESH_TOKEN_KEY, null);
        maxOil = config.getInt(Constants.MAX_OIL_KEY, Constants.DEFAULT_MILE_MILE);
        offsetMile = config.getInt(Constants.OFFSET_MILE_KEY, 0);
        Log.d(TAG, "初始化数据，refreshToken: " + refreshToken + ", " +
                "carData: " + carDataStr + ", maxOil: " + maxOil + ", offsetMile:" + offsetMile);
    }
}
