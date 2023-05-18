package cn.xanderye.android.deepalwidget;

import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import cn.xanderye.android.deepalwidget.activity.BatteryActivity;
import cn.xanderye.android.deepalwidget.constant.Constants;
import cn.xanderye.android.deepalwidget.entity.CarData;
import cn.xanderye.android.deepalwidget.provider.CarWidgetProvider;
import cn.xanderye.android.deepalwidget.service.DeepalService;
import cn.xanderye.android.deepalwidget.util.AndroidUtil;
import cn.xanderye.android.deepalwidget.util.DeepalUtil;
import cn.xanderye.android.deepalwidget.util.DeviceUtil;
import cn.xanderye.android.deepalwidget.util.Permission;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private SharedPreferences config;

    private TextView cellularText;

    private EditText tokenEditText, refreshTokenEditText, maxOilText, offsetText;

    private Spinner colorSpinner;

    private Button saveBtn, updateBtn, activeBtn, batteryBtn, controlBtn;

    private ImageView cellularRefreshImg, copyTokenImg, copyRefreshTokenImg;

    private JSONObject updateJson;

    private static final String HIDE_TOKEN = "****************";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        config = PreferenceManager.getDefaultSharedPreferences(this);

        tokenEditText = findViewById(R.id.tokenEditText);
        /*refreshTokenEditText = findViewById(R.id.refreshTokenEditText);*/
        /*deviceIdEditText = findViewById(R.id.deviceIdEditText);*/
        maxOilText = findViewById(R.id.maxOilText);
        cellularText = findViewById(R.id.cellularText);
        offsetText = findViewById(R.id.offsetText);
        cellularRefreshImg = findViewById(R.id.cellularRefreshImg);
        colorSpinner = findViewById(R.id.colorSpinner);
        updateBtn = findViewById(R.id.updateBtn);
        activeBtn = findViewById(R.id.activeBtn);
        batteryBtn = findViewById(R.id.batteryBtn);
        controlBtn = findViewById(R.id.controlBtn);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, Constants.CAR_COLORS);
        colorSpinner.setAdapter(adapter);

        String token = config.getString(Constants.TOKEN_KEY, null);
        if (token != null) {
            tokenEditText.setText(HIDE_TOKEN);
        }
        /*String refreshToken = config.getString(Constants.REFRESH_TOKEN_KEY, null);
        if (refreshToken != null) {
            refreshTokenEditText.setText(HIDE_TOKEN);
        }*/
        String cacToken = config.getString(Constants.CAC_TOKEN_KEY, null);
        if (cacToken != null) {
            // 自动更新流量
            // updateCellularData(false);
        }
        String carDataStr = config.getString(Constants.CAR_DATA_KEY, null);
        if (carDataStr != null) {
            JSONObject carDataJSON = JSON.parseObject(carDataStr);
            String color = carDataJSON.getString("color");
            if (color != null) {
             colorSpinner.setSelection(Arrays.asList(Constants.CAR_COLORS).indexOf(color));
            }
        }

        int maxOil = config.getInt(Constants.MAX_OIL_KEY, 0);
        if (maxOil > 0) {
            maxOilText.setText(String.valueOf(maxOil));
        }
        DeepalService deepalService = DeepalService.getInstance();
        deepalService.setContext(this);

        int offsetMile = config.getInt(Constants.OFFSET_MILE_KEY, 0);
        if (offsetMile > 0) {
            offsetText.setText(String.valueOf(offsetMile));
        }

        saveBtn = findViewById(R.id.saveBtn);
        saveBtn.setOnClickListener(v -> {
            save();
        });

        copyTokenImg = findViewById(R.id.copyTokenImg);
        copyTokenImg.setOnClickListener(v -> {
            String copyToken = config.getString(Constants.TOKEN_KEY, null);
            if (copyToken == null) {
                Toast.makeText(this, "请先配置token", Toast.LENGTH_SHORT).show();
                return;
            }
            AndroidUtil.copyToClipboard(this, copyToken);
            Toast.makeText(this, "复制成功，请勿泄露给他人", Toast.LENGTH_SHORT).show();
        });
        /*copyRefreshTokenImg = findViewById(R.id.copyRefreshTokenImg);
        copyRefreshTokenImg.setOnClickListener(v -> {
            String copyToken = config.getString(Constants.REFRESH_TOKEN_KEY, null);
            if (copyToken == null) {
                Toast.makeText(this, "请先配置token", Toast.LENGTH_SHORT).show();
                return;
            }
            AndroidUtil.copyToClipboard(this, copyToken);
            Toast.makeText(this, "复制成功，请勿泄露给他人", Toast.LENGTH_SHORT).show();
        });*/

        cellularRefreshImg.setOnClickListener(v -> {
            updateCellularData(true);
        });

        int alertMessage = config.getInt(Constants.ALERT_MESSAGE_KEY, 0);
        if (alertMessage == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.alert_message);
            builder.setNegativeButton("关闭程序", (dialog, which) -> {
                android.os.Process.killProcess(android.os.Process.myPid());
            });
            builder.setPositiveButton("同意", (dialog, which) -> {
                dialog.cancel();
                SharedPreferences.Editor edit = config.edit();
                edit.putInt(Constants.ALERT_MESSAGE_KEY, 1);
                edit.apply();
                register(this);
            });
            builder.create().show();
        }

        updateBtn.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            String version = updateJson.getString("version");
            String content = "版本：" + version + "\n" +
                    updateJson.getString("content").replace("\\n", "\n");
            builder.setMessage(content);
            builder.setNegativeButton("关闭", (dialog, which) -> {
                dialog.cancel();
            });
            builder.setPositiveButton("下载", (dialog, which) -> {
                dialog.cancel();
                Toast.makeText(this, "密码8888", Toast.LENGTH_LONG).show();
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri uri = Uri.parse("https://xanderye.lanzouf.com/b022gzxih");
                intent.setData(uri);
                startActivity(intent);
            });
            builder.create().show();
        });

        activeBtn.setOnClickListener(v -> {
            active();
        });

        batteryBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, BatteryActivity.class);
            startActivity(intent);
        });


        boolean notificationEnabled = Permission.isNotificationEnabled(this);
        if (!notificationEnabled) {
            Toast.makeText(this, "请开启通知权限以保证小组件正常运行", Toast.LENGTH_SHORT).show();
        }

        checkUpdate(this);
    }

    private void save() {
        saveBtn.setEnabled(false);
        String token = tokenEditText.getEditableText().toString().trim();
        /*String refreshToken = refreshTokenEditText.getEditableText().toString().trim();*/
        String color = (String) colorSpinner.getSelectedItem();
        String maxOilStr = maxOilText.getEditableText().toString().trim();
        String offsetMileStr = offsetText.getEditableText().toString().trim();
        int maxOil = 0;
        try {
            maxOil = Integer.parseInt(maxOilStr);
        } catch (Exception ignored) {
        }
        int offsetMile = 0;
        try {
            offsetMile = Integer.parseInt(offsetMileStr);
        } catch (Exception ignored) {
        }
        if (token.contains(HIDE_TOKEN)) {
            token = config.getString(Constants.TOKEN_KEY, "");
        }
        /*if (refreshToken.contains(HIDE_TOKEN)) {
            refreshToken = config.getString(Constants.REFRESH_TOKEN_KEY, "");
        }*/
        if ("".equals(token)) {
            Toast.makeText(this, "请填写token", Toast.LENGTH_SHORT).show();
            return;
        }
        ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
        int finalMaxOil = maxOil;
        int finalOffsetMile = offsetMile;
        String finalToken = token;
        /*String finalRefreshToken = refreshToken;*/
        singleThreadExecutor.execute(() -> {
            Looper.prepare();
            try {
                String newRefreshToken = null;
                String accessToken = null;
                if (finalToken != null) {
                    // 使用登录token获取信息
                    JSONObject res = DeepalUtil.getCacTokenByAuthToken(finalToken);
                    if (res.getInteger("code") != 200) {
                        Toast.makeText(this, "token无效", Toast.LENGTH_SHORT).show();
                        runOnUiThread(() -> saveBtn.setEnabled(true));
                        return;
                    }
                    JSONObject data = res.getJSONObject("data");
                    accessToken = data.getString("cacToken");
                    newRefreshToken = data.getString("refreshToken");
                } else {
                    // 使用refresh token获取信息
                    /*newRefreshToken = finalRefreshToken;
                    JSONObject res = DeepalUtil.refreshCacToken(finalRefreshToken);
                    if (res.getInteger("code") != 200) {
                        Toast.makeText(this, "refresh token无效", Toast.LENGTH_SHORT).show();
                        runOnUiThread(() -> saveBtn.setEnabled(true));
                        return;
                    }
                    JSONObject data = res.getJSONObject("data");
                    accessToken = data.getString("access_token");*/
                }

                JSONObject res = DeepalUtil.getCarByToken(finalToken, accessToken);
                if (res.getInteger("code") != 200) {
                    String errorMsg = "获取信息失败，原因" + res.getString("message");
                    Log.d(TAG, errorMsg);
                    Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
                    runOnUiThread(() -> saveBtn.setEnabled(true));
                    return;
                }
                JSONArray carArray = res.getJSONArray("data");
                if (carArray.isEmpty()) {
                    String errorMsg = "当前账号下没有车辆";
                    Log.d(TAG, errorMsg);
                    Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
                    runOnUiThread(() -> saveBtn.setEnabled(true));
                    return;
                }
                JSONObject carJson = carArray.getJSONObject(0);
                JSONObject carData = new JSONObject();
                carData.put("carId", carJson.getString("carId"));
                carData.put("carName", carJson.getString("carName"));
                carData.put("plateNumber", carJson.getString("plateNumber"));
                carData.put("color", color);

                SharedPreferences.Editor edit = config.edit();
                edit.putString(Constants.TOKEN_KEY, finalToken);
                edit.putString(Constants.REFRESH_TOKEN_KEY, newRefreshToken);
                edit.putString(Constants.CAC_TOKEN_KEY, accessToken);
                edit.putString(Constants.CAR_DATA_KEY, carData.toJSONString());
                if (finalMaxOil > 0) {
                    edit.putInt(Constants.MAX_OIL_KEY, finalMaxOil);
                }
                if (finalOffsetMile >= 0) {
                    edit.putInt(Constants.OFFSET_MILE_KEY, finalOffsetMile);
                }
                edit.apply();
                runOnUiThread(() -> {
                    if (finalToken != null) {
                        tokenEditText.setText(HIDE_TOKEN);
                    }
                    /*if (finalRefreshToken != null) {
                        refreshTokenEditText.setText(HIDE_TOKEN);
                    }*/
                });
                DeepalService deepalService = DeepalService.getInstance();
                deepalService.setContext(this);
                Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Log.d(TAG, "请求失败" + e.getMessage());
                Toast.makeText(this, "请求服务器失败", Toast.LENGTH_SHORT).show();
            }
            runOnUiThread(() -> saveBtn.setEnabled(true));
            Looper.loop();
        });
        singleThreadExecutor.shutdown();
    }

    private void active() {
        activeBtn.setEnabled(false);
        ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
        singleThreadExecutor.execute(() -> {
            Looper.prepare();
            Context mContext = getApplicationContext();
            DeepalService deepalService = DeepalService.getInstance();
            deepalService.setContext(this);
            CarData carData = deepalService.getCarData();
            String msg = "刷新成功";
            if (carData != null) {
                AppWidgetManager manager = AppWidgetManager.getInstance(mContext);
                RemoteViews carDataRemoteViews = CarWidgetProvider.bindButton(mContext);
                CarWidgetProvider.getCarData(carData, mContext, carDataRemoteViews);
                manager.updateAppWidget(new ComponentName(mContext, CarWidgetProvider.class), carDataRemoteViews);
            } else {
                msg = "刷新失败，请检查配置";
            }
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            runOnUiThread(() -> activeBtn.setEnabled(true));
            Looper.loop();
        });
        singleThreadExecutor.shutdown();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, 2, 2, "关于");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 2: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.about_message);
                builder.setNegativeButton("关闭", (dialog, which) -> {
                    dialog.cancel();
                });
                builder.setPositiveButton("源码", (dialog, which) -> {
                    dialog.cancel();
                    Intent intent = new Intent();
                    intent.setAction("android.intent.action.VIEW");
                    Uri uri = Uri.parse("https://github.com/XanderYe/DeepalWidget");
                    intent.setData(uri);
                    startActivity(intent);
                });
                builder.create().show();
                break;
            }
            default:
                break;
        }
        return true;
    }

    public void register(Context context) {
        ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
        singleThreadExecutor.execute(() -> {
            String android = DeviceUtil.getAndroidId(context);
            String deviceName = DeviceUtil.getDeviceName();
            String appVersion = context.getResources().getString(R.string.about_version);
            DeepalUtil.register(android, deviceName, appVersion);
        });
        singleThreadExecutor.shutdown();
    }

    public void checkUpdate(Context context) {
        ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
        singleThreadExecutor.execute(() -> {
            try {
                JSONObject res = DeepalUtil.checkUpdate();
                if (res.getInteger("code") == 0) {
                    String appVersion = context.getResources().getString(R.string.about_version);
                    updateJson = res.getJSONObject("data");
                    String version = updateJson.getString("version");
                    if (!appVersion.equals(version)) {
                        runOnUiThread(() -> updateBtn.setVisibility(View.VISIBLE));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        singleThreadExecutor.shutdown();
    }

    private void updateCellularData(boolean showTips) {
        DeepalService deepalService = DeepalService.getInstance();
        deepalService.setContext(this);
        ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
        singleThreadExecutor.execute(() -> {
            Looper.prepare();
            String newCellularData = deepalService.getCellularData(null);
            String msg = "更新成功";
            if (newCellularData != null) {
                runOnUiThread(() -> cellularText.setText(newCellularData));
            } else {
                msg = "更新失败";
            }
            if (showTips) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
            Looper.loop();
        });
        singleThreadExecutor.shutdown();
    }
}