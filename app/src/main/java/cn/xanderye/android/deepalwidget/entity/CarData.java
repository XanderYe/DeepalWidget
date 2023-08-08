package cn.xanderye.android.deepalwidget.entity;

import lombok.Data;

/**
 * @author yezhendong
 * @description:
 * @date 2023/3/16 14:54
 */
@Data
public class CarData {

    private String type;
    /**
     * 颜色
     */
    private String color;
    /**
     * 名称
     */
    private String carName;
    /**
     * 车牌
     */
    private String plateNumber;

    /**
     * 总里程
     */
    private String totalOdometer;
    /**
     * 车内温度
     */
    private String vehicleTemperature;
    /**
     * 剩余公里数
     */
    private String remainedPowerMile;
    /**
     * 剩余电量
     */
    private String remainPower;
    /**
     * 剩余油表里程
     */
    private String remainedOilMile;
    /**
     * 剩余油表百分比
     */
    private String remainedOil;
    /**
     * 更新时间
     */
    private String terminalTime;
    /**
     * 服务器时间
     */
    private String serverTime;

    /**
     * 车锁状态 0闭锁 1解锁
     */
    private Integer leftFrontDoorLock;
    /**
     * 车门状态 0关闭 1开启
     */
    private Integer driverDoor;
    private Integer passengerDoor;
    private Integer leftRearDoor;
    private Integer rightRearDoor;
    /**
     * 车辆充电状态: 1-充电中; !1-未充电
     */
    private Integer batteryChargeStatus;
    /**
     * 电源状态反馈: 0-OFF 1-ACC 2-ON 3-START
     */
    private Integer powerStatusFeedBack;

    /**
     * 直流/交流充电枪连接状态
     * dcDhargeGunConnectionState、acChargeGunConnectionState  1-已连接 !1-未连接
     */
    private Integer dcDhargeGunConnectionState;
    private Integer acChargeGunConnectionState;
    /**
     * 直流电压
     */
    private Double totalVoltage;
    /**
     * 直流电流
     */
    private Double totalCurrent;
    /**
     * 交流电流
     */
    private Double obcChrgInpAcI;
    /**
     * 充电剩余时间
     */
    private Integer chargDeltMins;
    /**
     * 位置信息
     */
    private String location;
    /**
     * 经度
     */
    private Double lng;
    /**
     * 纬度
     */
    private Double lat;
}
