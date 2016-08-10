/*
 *  EV3way.java (for leJOS EV3)
 *  Created on: 2016/02/11
 *  Copyright (c) 2016 Embedded Technology Software Design Robot Contest
 */
package jp.etrobo.ev3.sample;

import jp.etrobo.ev3.balancer.Balancer;
import lejos.hardware.Battery;
import lejos.hardware.lcd.LCD;
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
    private static final float LIGHT_WHITE          = 0.4F;           // 白色のカラーセンサー輝度値
    private static final float LIGHT_BLACK          = 0.0F;           // 黒色のカラーセンサー輝度値
    private static final float P_GAIN               = 2.5F;           // 完全停止用モータ制御比例係数
    private static final float P_SLOW_GAIN          = 0.5F;
    private static final float P_GETUP_GAIN         = 4.5F;
    private static final float P_HARD_GAIN          = 6.0F;
    private static final int   PWM_ABS_MAX          = 60;             // 完全停止用モータ制御PWM絶対最大値
    private static final int   PWM_GETUP_MAX              = 80;       // 起き上がるときのPWM絶対最大値
    private static final float THRESHOLD = (LIGHT_WHITE+LIGHT_BLACK)/2.0F;  // ライントレースの閾値

    private static final float DELTA_T = 0.004F;
    private static final float Kp = 0.36F, Ki = 0.5F, Kd = 0.5F;
    private static float sensor_val;
    private static float target_val;
    private static float[] diff = new float[2];
    private static float integral;

    // モータ制御用オブジェクト
    // EV3LargeRegulatedMotor では PWM 制御ができないので、TachoMotorPort を利用する
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

    /**
     * コンストラクタ。
     */
    public EV3way() {
        motorPortL = MOTORPORT_LWHEEL.open(TachoMotorPort.class); // 左モータ
        motorPortR = MOTORPORT_RWHEEL.open(TachoMotorPort.class); // 右モータ
        motorPortT = MOTORPORT_TAIL.open(TachoMotorPort.class);   // 尻尾モータ
        motorPortL.setPWMMode(BasicMotorPort.PWM_BRAKE);
        motorPortR.setPWMMode(BasicMotorPort.PWM_BRAKE);
        motorPortT.setPWMMode(BasicMotorPort.PWM_BRAKE);

        // タッチセンサー
        touch = new EV3TouchSensor(SENSORPORT_TOUCH);
        touchMode = touch.getTouchMode();
        sampleTouch = new float[touchMode.sampleSize()];

        // 超音波センサー
        sonar = new EV3UltrasonicSensor(SENSORPORT_SONAR);
        distanceMode = sonar.getDistanceMode(); // 距離検出モード
        sampleDistance = new float[distanceMode.sampleSize()];
        sonar.enable();

        // カラーセンサー
        colorSensor = new EV3ColorSensor(SENSORPORT_COLOR);
        redMode = colorSensor.getRedMode();     // 輝度検出モード
        sampleLight = new float[redMode.sampleSize()];

        // ジャイロセンサー
        gyro = new EV3GyroSensor(SENSORPORT_GYRO);
        rate = gyro.getRateMode();              // 角速度検出モード
        sampleGyro = new float[rate.sampleSize()];
    }

    /**
     * 走行関連メソッドの空回し。
     * Java の初期実行性能が悪く、倒立振子に十分なリアルタイム性が得られない。
     * そのため、走行によく使うメソッドについて、HotSpot がネイティブコードに変換するまで空実行する。
     * HotSpot が起きるデフォルトの実行回数は 1500。
     */
    public void idling() {
        for (int i=0; i < 1500; i++) {
            motorPortL.controlMotor(0, 0);
            getBrightness();
            getSonarDistance();
            getGyroValue();
            Battery.getVoltageMilliVolt();
            Balancer.control(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 8000);
        }
        Delay.msDelay(10000);       // 別スレッドで HotSpot が完了するだろう時間まで待つ。
    }

    /**
     * センサー、モータ、倒立振子ライブラリのリセット。
     */
    public void reset() {
        //gyro.reset();
        motorPortL.controlMotor(0, 0);
        motorPortR.controlMotor(0, 0);
        motorPortT.controlMotor(0, 0);
        motorPortL.resetTachoCount();   // 左モータエンコーダリセット
        motorPortR.resetTachoCount();   // 右モータエンコーダリセット
        motorPortT.resetTachoCount();   // 尻尾モータエンコーダリセット
        Balancer.init();                // 倒立振子制御初期化
    }

    /**
     * ジャイロセンサーの初期化
     */
    public void resetGyro() {
        gyro.reset();
    }

    /**
     * 両サイドのモータエンコーダリセット
     */
    public void resetMotor(){
        motorPortL.resetTachoCount();   // 左モータエンコーダリセット
        motorPortR.resetTachoCount();   // 右モータエンコーダリセット
    }

    /**
     * センサー、モータの終了処理。
     */
    public void close() {
        motorPortL.close();
        motorPortR.close();
        motorPortT.close();
        colorSensor.setFloodlight(false);
        sonar.disable();
    }

    /**
     * タッチセンサー押下のチェック。
     * @return true ならタッチセンサーが押された。
     */
    public final boolean touchSensorIsPressed() {
        touchMode.fetchSample(sampleTouch, 0);
        return ((int)sampleTouch[0] != 0);
    }

    /**
     * PID制御でのターン値の取得
     */
    public float getPIDTurnValue(){
        float p, i, d;
        float turn;

        sensor_val = getBrightness();
        target_val = THRESHOLD;

        diff[0] = diff[1];
        diff[1] = sensor_val - target_val;
        integral += (diff[1] + diff[0]) / 2.0 * DELTA_T;

        p = Kp * diff[1];
        i = Ki * integral;
        d = Kd * (diff[1] + diff[0]) / DELTA_T;
        turn = p + i + d;

        if (turn > 100.0F) {
        	turn = 100.0F;
        } else if ( -100.0F > turn){
        	turn = -100.0F;
        }

    	return turn;
    }

    /**
     * 走行体完全停止用モータの角度制御
     * @param angle モータ目標角度[度]
     */
    public void controlTail(int angle) {
        float pwm = (float)(angle - motorPortT.getTachoCount()) * P_GAIN; // 比例制御
        // PWM出力飽和処理
        if (pwm > PWM_ABS_MAX) {
            pwm = PWM_ABS_MAX;
        } else if (pwm < -PWM_ABS_MAX) {
            pwm = -PWM_ABS_MAX;
        }
        motorPortT.controlMotor((int)pwm, 1);
    }

    /**
     * ゆっくり制御用モータの角度制御
     * @param angle モータ目標角度[度]
     */
    public void controlTailSlow(int angle) {
        float pwm = (float)(angle - motorPortT.getTachoCount()) * P_SLOW_GAIN; // 比例制御
        // PWM出力飽和処理
        if (pwm > PWM_ABS_MAX) {
            pwm = PWM_ABS_MAX;
        } else if (pwm < -PWM_ABS_MAX) {
            pwm = -PWM_ABS_MAX;
        }
        motorPortT.controlMotor((int)pwm, 1);
    }

    /**
     * 尻尾状態を維持する用モータの角度制御
     * @param angle モータ目標角度[度]
     */
    public void controlTailPropUP(int angle) {
        float pwm = (float)(angle - motorPortT.getTachoCount()); // 比例制御
        // 尻尾が目標値より高いか低いかで出力の比例定数を設定
        if (0 < pwm) {
        	pwm = pwm * P_HARD_GAIN;
        } else if(pwm > 0) {
        	pwm = pwm * P_GAIN ;
        }

        // PWM出力飽和処理
        if (pwm > PWM_ABS_MAX) {
            pwm = PWM_ABS_MAX;
        } else if (pwm < -PWM_ABS_MAX) {
            pwm = -PWM_ABS_MAX;
        }
        LCD.drawString("pwm:"+pwm, 0, 5);
        motorPortT.controlMotor((int)pwm, 1);
    }

    /**
     * 尻尾状態で起き上がる用モータの角度制御
     * @param angle モータ目標角度[度]
     */
    public void controlTailGetUp(int angle) {
        float pwm = (float)(angle - motorPortT.getTachoCount()); // 比例制御
        // 尻尾が目標値より高いか低いかで出力の比例定数を設定
        if (0 < pwm) {
        	pwm = pwm * P_GETUP_GAIN ;
        } else if(pwm > 0) {
        	pwm = pwm * P_GAIN;
        }

        // PWM出力飽和処理
        if (pwm > PWM_GETUP_MAX ) {
            pwm = PWM_GETUP_MAX ;
        } else if (pwm < -PWM_GETUP_MAX ) {
            pwm = -PWM_GETUP_MAX ;
        }
        motorPortT.controlMotor((int)pwm, 1);
    }

    /**
     * クラス外からバランサーAPIの設定
     *
     */
    public void setBalancerParm(float forward, float turn){
        float gyroNow = getGyroValue();                 // ジャイロセンサー値
        int thetaL = motorPortL.getTachoCount();        // 左モータ回転角度
        int thetaR = motorPortR.getTachoCount();        // 右モータ回転角度
        int battery = Battery.getVoltageMilliVolt();    // バッテリー電圧[mV]
        Balancer.control (forward, turn, gyroNow, GYRO_OFFSET, thetaL, thetaR, battery); // 倒立振子制御
    }

    /**
     * クラス外からバランサーAPIの設定
     *
     */
    public void setBalancerParm(float forward, float turn, float gyroOffset){
        float gyroNow = getGyroValue();                 // ジャイロセンサー値
        int thetaL = motorPortL.getTachoCount();        // 左モータ回転角度
        int thetaR = motorPortR.getTachoCount();        // 右モータ回転角度
        int battery = Battery.getVoltageMilliVolt();    // バッテリー電圧[mV]
        Balancer.control (forward, turn, gyroNow, gyroOffset, thetaL, thetaR, battery); // 倒立振子制御
    }

    /**
     * 超音波センサーにより障害物との距離を取得する。
     * @return 障害物との距離(m)。
     */
    public final float getSonarDistance() {
        distanceMode.fetchSample(sampleDistance, 0);
        return sampleDistance[0];
    }

    /*
     * カラーセンサーから輝度値を取得する。
     * @return 輝度値。
     */
    private final float getBrightness() {
        redMode.fetchSample(sampleLight, 0);
        return sampleLight[0];
    }

    /**
     * ジャイロセンサーから角速度を取得する。
     * @return 角速度。
     */
    public final float getGyroValue() {
        rate.fetchSample(sampleGyro, 0);
        // leJOS ではジャイロセンサーの角速度値が正負逆になっているので、
        // 倒立振子ライブラリの仕様に合わせる。
        return -sampleGyro[0];
    }
}
