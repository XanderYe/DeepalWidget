package cn.xanderye.android.deepalwidget;

import android.app.AlertDialog;
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
import cn.xanderye.android.deepalwidget.constant.Constants;
import cn.xanderye.android.deepalwidget.service.CarDataTimeService;
import cn.xanderye.android.deepalwidget.service.DeepalService;
import cn.xanderye.android.deepalwidget.util.AndroidUtil;
import cn.xanderye.android.deepalwidget.util.DeepalUtil;
import cn.xanderye.android.deepalwidget.util.DeviceUtil;
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

    private TextView lastUpdateText;

    private EditText tokenEditText, maxOilText, offsetText;

    private Spinner colorSpinner;

    private Button saveBtn, updateBtn, startServiceBtn, stopServiceBtn;

    private ImageView refreshTime;

    private JSONObject updateJson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        config = PreferenceManager.getDefaultSharedPreferences(this);

        tokenEditText = findViewById(R.id.tokenEditText);
        /*deviceIdEditText = findViewById(R.id.deviceIdEditText);*/
        maxOilText = findViewById(R.id.maxOilText);
        lastUpdateText = findViewById(R.id.lastUpdateText);
        offsetText = findViewById(R.id.offsetText);
        refreshTime = findViewById(R.id.refreshTime);
        colorSpinner = findViewById(R.id.colorSpinner);
        updateBtn = findViewById(R.id.updateBtn);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, Constants.CAR_COLORS);
        colorSpinner.setAdapter(adapter);

        String token = config.getString(Constants.REFRESH_TOKEN_KEY, null);
        if (token != null) {
            tokenEditText.setText(token);
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

        String lastUpdate = config.getString(Constants.LAST_UPDATE_KEY, "");
        lastUpdateText.setText(lastUpdate);

        int offsetMile = config.getInt(Constants.OFFSET_MILE_KEY, 0);
        if (offsetMile > 0) {
            offsetText.setText(String.valueOf(offsetMile));
        }

        saveBtn = findViewById(R.id.saveBtn);
        saveBtn.setOnClickListener(v -> {
            save();
        });
        refreshTime.setOnClickListener(v -> {
            String date = config.getString(Constants.LAST_UPDATE_KEY, "");
            lastUpdateText.setText(date);
            Toast.makeText(this, "已更新", Toast.LENGTH_SHORT).show();
        });
        startServiceBtn = findViewById(R.id.startServiceBtn);
        startServiceBtn.setOnClickListener(v -> {
            AndroidUtil.startAliveService(getApplicationContext(), CarDataTimeService.class);
            Toast.makeText(this, "启动服务", Toast.LENGTH_SHORT).show();
        });

        stopServiceBtn = findViewById(R.id.stopServiceBtn);
        stopServiceBtn.setOnClickListener(v -> {
            AndroidUtil.stopAliveService(getApplicationContext(), CarDataTimeService.class);
            Toast.makeText(this, "停止服务", Toast.LENGTH_SHORT).show();
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
        checkUpdate(this);
    }

    private void save() {
        saveBtn.setEnabled(false);
        String refreshToken = tokenEditText.getEditableText().toString().trim();
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
        if ("".equals(refreshToken)) {
            Toast.makeText(this, "请输入refresh token", Toast.LENGTH_SHORT).show();
            return;
        }
        ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
        /*String finalDeviceId = deviceId;*/
        int finalMaxOil = maxOil;
        int finalOffsetMile = offsetMile;
        singleThreadExecutor.execute(() -> {
            Looper.prepare();
            try {
                JSONObject res = DeepalUtil.refreshCacToken(refreshToken);
                if (res.getInteger("code") != 200) {
                    String errorMsg = "获取信息失败，原因" + res.getString("message");
                    Log.d(TAG, errorMsg);
                    Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
                    return;
                }
                JSONObject data = res.getJSONObject("data");
                String accessToken = data.getString("access_token");

                res = DeepalUtil.getCarByToken(accessToken);
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
                edit.putString(Constants.REFRESH_TOKEN_KEY, refreshToken);
                edit.putString(Constants.CAR_DATA_KEY, carData.toJSONString());
                if (finalMaxOil > 0) {
                    edit.putInt(Constants.MAX_OIL_KEY, finalMaxOil);
                }
                if (finalOffsetMile > 0) {
                    edit.putInt(Constants.OFFSET_MILE_KEY, finalOffsetMile);
                }
                edit.apply();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, 1, 1, "关于");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1: {
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
}