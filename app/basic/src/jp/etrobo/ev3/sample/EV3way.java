/*
 *  EV3way.java (for leJOS EV3)
 *  Created on: 2016/02/11
 *  Copyright (c) 2016 Embedded Technology Software Design Robot Contest
 */
package jp.etrobo.ev3.sample;

import jp.etrobo.ev3.balancer.Balancer;

import lejos.hardware.Battery;
import lejos.hardware.port.BasicMotorPort;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;
import lejos.hardware.port.TachoMotorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorMode;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

/**
 * EV3way本体のモータとセンサーを扱うクラス。
 */
public class EV3way {
    public static final int   TAIL_ANGLE_STAND_UP   = 94;   // 完全停止時の角度[度]
    public static final int   TAIL_ANGLE_DRIVE      = 3;    // バランス走行時の角度[度]

    // 下記のパラメータはセンサ個体/環境に合わせてチューニングする必要があります
    private static final Port  MOTORPORT_LWHEEL     = MotorPort.C;    // 左モータポート
    private static final Port  MOTORPORT_RWHEEL     = MotorPort.B;    // 右モータポート
    private static final Port  MOTORPORT_TAIL       = MotorPort.A;    // 尻尾モータポート
    private static final Port  SENSORPORT_TOUCH     = SensorPort.S1;  // タッチセンサーポート
    private static final Port  SENSORPORT_SONAR     = SensorPort.S2;  // 超音波センサーポート
    private static final Port  SENSORPORT_COLOR     = SensorPort.S3;  // カラーセンサーポート
    private static final Port  SENSORPORT_GYRO      = SensorPort.S4;  // ジャイロセンサーポート
    private static final float GYRO_OFFSET          = 0.0F;           // ジャイロセンサーオフセット値
    private static final float LIGHT_WHITE          = 0.2F;           // 白色のカラーセンサー輝度値
    private static final float LIGHT_BLACK          = 0.0F;           // 黒色のカラーセンサー輝度値
    private static final float SONAR_ALERT_DISTANCE = 0.3F;           // 超音波センサーによる障害物検知距離[m]
    private static final float P_GAIN               = 2.5F;           // 完全停止用モータ制御比例係数
    private static final int   PWM_ABS_MAX          = 60;             // 完全停止用モータ制御PWM絶対最大値
    private static final float THRESHOLD = (LIGHT_WHITE + LIGHT_BLACK) / 2.0F;  // ライントレースの閾値

    private static final float DELTA_T = 0.004F;
    private static final float Kp = 0.36F, Ki = 0.5F, Kd = 0.5F;
    private static float[] diff = new float[2];
    private static float integral;

    // モータ制御用オブジェクト
    public TachoMotorPort motorPortL; // 左モータ
    public TachoMotorPort motorPortR; // 右モータ
    public TachoMotorPort motorPortT; // 尻尾モータ

    // タッチセンサ
    private EV3TouchSensor touch;
    private SensorMode touchMode;
    private float[] sampleTouch;

    // 超音波センサ
    private EV3UltrasonicSensor sonar;
    private SampleProvider distanceMode;  // 距離検出モード
    private float[] sampleDistance;

    // カラーセンサ
    private EV3ColorSensor colorSensor;
    private SensorMode redMode;           // 輝度検出モード
    private float[] sampleLight;

    // ジャイロセンサ
    private EV3GyroSensor gyro;
    private SampleProvider rate;          // 角速度検出モード
    private float[] sampleGyro;

    public EV3way() {
        this.__initalizeAllMotor();
        this.__initalizeTouchSensor();
        this.__initalizeSonicSensor();
        this.__initalizeColorSensor();
        this.__initalizeGyroSensor();
    }
    private void __initalizeAllMotor() {
        this.motorPortL = MOTORPORT_LWHEEL.open(TachoMotorPort.class); // 左モータ
        this.motorPortR = MOTORPORT_RWHEEL.open(TachoMotorPort.class); // 右モータ
        this.motorPortT = MOTORPORT_TAIL.open(TachoMotorPort.class);   // 尻尾モータ
        this.motorPortL.setPWMMode(BasicMotorPort.PWM_BRAKE);
        this.motorPortR.setPWMMode(BasicMotorPort.PWM_BRAKE);
        this.motorPortT.setPWMMode(BasicMotorPort.PWM_BRAKE);
    }
    private void __initalizeTouchSensor() {
        this.touch = new EV3TouchSensor(SENSORPORT_TOUCH);
        this.touchMode = this.touch.getTouchMode();
        this.sampleDistance = new float[this.touchMode.sampleSize()];
    }
    private void __initalizeSonicSensor() {
        this.sonar = new EV3UltrasonicSensor(SENSORPORT_SONAR);
        this.distanceMode = this.sonar.getDistanceMode(); // 距離検出モード
        this.sampleDistance = new float[this.distanceMode.sampleSize()];
        this.sonar.enable();
    }
    private void __initalizeColorSensor() {
        this.colorSensor = new EV3ColorSensor(SENSORPORT_COLOR);
        this.redMode = this.colorSensor.getRedMode();     // 輝度検出モード
        this.sampleLight = new float[this.redMode.sampleSize()];
    }
    private void __initalizeGyroSensor() {
        this.gyro = new EV3GyroSensor(SENSORPORT_GYRO);
        this.rate = this.gyro.getRateMode();              // 角速度検出モード
        this.sampleGyro = new float[this.rate.sampleSize()];
    }

    public void idling() {
        for (int i = 0; i < 1500; i++) {
            this.motorPortL.controlMotor(0, 0);
            this.getBrightness();
            this.getSonarDistance();
            this.getGyroValue();
            Battery.getVoltageMilliVolt();
            Balancer.control(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 8000);
        }
        Delay.msDelay(10000);
    }

    /*
     * updateBasicSensorValues()
     * 4ms 間隔で 実行されるべきメソッド
     */
    public void updateBasicSensorValues() {
        this.rate.fetchSample(this.sampleGyro, 0);
        this.redMode.fetchSample(this.sampleLight, 0);
        this.touchMode.fetchSample(this.sampleTouch, 0);
        this.distanceMode.fetchSample(this.sampleDistance, 0);
    }

    public float getGyroValue() { return this.sampleGyro[0]; }
    public float getBrightness() { return this.sampleLight[0]; }
    public float getSonarDistance() { return this.sampleDistance[0]; }
    public float getTouchValue() { return this.sampleTouch[0]; }
    public boolean isPressed() { return (int) this.getTouchValue() == 0; }
    
    public void controllTail(int angle) {
        float pwm = (float)(angle - motorPortT.getTachoCount()) * P_GAIN;
        // PWM出力飽和処理
        if (pwm > PWM_ABS_MAX) {
            pwm = PWM_ABS_MAX;
        } else if (pwm < -PWM_ABS_MAX) {
            pwm = -PWM_ABS_MAX;
        }
        motorPortT.controlMotor((int)pwm, 1);
    }

    public void ready() {
        this.gyro.reset();
        this.motorPortL.controlMotor(0, 0);
        this.motorPortR.controlMotor(0, 0);
        this.motorPortT.controlMotor(0, 0);
        this.motorPortL.resetTachoCount();   // 左モータエンコーダリセット
        this.motorPortR.resetTachoCount();   // 右モータエンコーダリセット
        this.motorPortT.resetTachoCount();   // 尻尾モータエンコーダリセット
        Balancer.init();                // 倒立振子制御初期化
    }
    
    public void close() {
        motorPortL.close();
        motorPortR.close();
        motorPortT.close();
        colorSensor.setFloodlight(false);
        sonar.disable();
    }
}
