package cn.xanderye.android.deepalwidget.entity;

import lombok.Data;

/**
 * @author yezhendong
 * @description:
 * @date 2023/3/16 14:54
 */
@Data
public class CarData {

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
     * 驾驶位锁状态 0闭锁 1解锁
     */
    private Integer driverDoorLock;
    /**
     * 副驾驶锁状态 0闭锁 1解锁
     */
    private Integer passengerDoorLock;
    /**
     * 充电状态
     */
    private Integer chargeStatus;
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
