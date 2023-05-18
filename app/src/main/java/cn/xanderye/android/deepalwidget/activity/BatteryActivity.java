package cn.xanderye.android.deepalwidget.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import cn.xanderye.android.deepalwidget.R;
import cn.xanderye.android.deepalwidget.entity.CarData;
import cn.xanderye.android.deepalwidget.service.DeepalService;
import cn.xanderye.android.deepalwidget.util.CommonUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author yezhendong
 * @description:
 * @date 2023/4/21 9:43
 */
public class BatteryActivity extends AppCompatActivity {

    private TextView titleText, voltageText, currentText, powerText, remainTimeText, chargeRemainMileText, chargeRemainPowerText;

    private LinearLayout remainTimeLayout;

    private ProgressBar powerProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battery);

        titleText = findViewById(R.id.titleText);
        voltageText = findViewById(R.id.voltageText);
        currentText = findViewById(R.id.currentText);
        powerText = findViewById(R.id.powerText);
        remainTimeLayout = findViewById(R.id.remainTimeLayout);
        remainTimeText = findViewById(R.id.remainTimeText);
        chargeRemainMileText = findViewById(R.id.chargeRemainMileText);
        chargeRemainPowerText = findViewById(R.id.chargeRemainPowerText);
        powerProgress = findViewById(R.id.powerProgress);

        DeepalService deepalService = DeepalService.getInstance();
        deepalService.setContext(this);

        ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
        singleThreadExecutor.execute(() -> {
            CarData carData = deepalService.getCarData();
            if (carData == null) {
                Toast.makeText(this, "获取信息失败", Toast.LENGTH_SHORT).show();
                return;
            }
            int chargeGunType = getChargeGunType(carData);
            Double chargeVoltage = 0D;
            Double chargeCurrent = 0D;
            String title;
            boolean isCharge = false;
            if (chargeGunType == 0) {
                title = "直流充电枪已连接";
            } else if (chargeGunType == 1) {
                title = "交流充电枪已连接";
            } else {
                title = "充电枪未连接";
            }
            if (carData.getBatteryChargeStatus() == 1) {
                title += "，充电中";
                isCharge = true;
            } else if (carData.getBatteryChargeStatus() == 2 || carData.getBatteryChargeStatus() == 3) {
                title += "，充电结束";
            } else {
                title += "，未充电";
            }
            if (chargeGunType == 0) {
                chargeVoltage = carData.getTotalVoltage();
                chargeCurrent = Math.abs(carData.getTotalCurrent());
            } else if (chargeGunType == 1) {
                chargeVoltage = 220D;
                chargeCurrent = Math.abs(carData.getObcChrgInpAcI());
            }
            BigDecimal powerDecimal = new BigDecimal(chargeVoltage * chargeCurrent / 1000).setScale(2, RoundingMode.HALF_UP);
            Integer chargDeltMins = carData.getChargDeltMins();
            String remainTime = CommonUtil.formatTime(chargDeltMins);
            String finalTitle = title;
            int powerPercent = Double.valueOf(carData.getRemainPower()).intValue();
            boolean finalIsCharge = isCharge;
            Double finalChargeVoltage = chargeVoltage;
            Double finalChargeCurrent = chargeCurrent;
            runOnUiThread(() -> {
                titleText.setText(finalTitle);
                if (finalIsCharge) {
                    remainTimeLayout.setVisibility(View.VISIBLE);
                    voltageText.setText(finalChargeVoltage + "V");
                    currentText.setText(finalChargeCurrent + "A");
                    powerText.setText(powerDecimal + "kW");
                    remainTimeText.setText(remainTime);
                }
                chargeRemainMileText.setText(carData.getRemainedPowerMile() + "km");
                chargeRemainPowerText.setText(powerPercent + "%");
                powerProgress.setProgress(powerPercent);
            });
        });
        singleThreadExecutor.shutdown();
    }

    /**
     * 0 直流电，1 交流电，2 未连接
     * @param carData
     * @return int
     * @author yezhendong
     * @date 2023/4/21
     */
    private int getChargeGunType(CarData carData) {
        if (carData.getDcDhargeGunConnectionState() != null && carData.getDcDhargeGunConnectionState() == 1) {
            return 0;
        } else if (carData.getAcChargeGunConnectionState() != null && carData.getAcChargeGunConnectionState() == 1) {
            return 1;
        } else {
            return 2;
        }
    }

}
