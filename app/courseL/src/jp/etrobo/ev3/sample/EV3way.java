/*
 *  EV3way.java (for leJOS EV3)
 *  Created on: 2016/02/11
 *  Copyright (c) 2016 Embedded Technology Software Design Robot Contest
 */
package jp.etrobo.ev3.sample;

import jp.etrobo.ev3.balancer.Balancer;
import lejos.hardware.Battery;
import lejos.hardware.Sound;
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
    private static final float SONAR_ALERT_DISTANCE = 0.3F;           // 超音波センサーによる障害物検知距離[m]
    private static final float P_GAIN               = 2.5F;           // 完全停止用モータ制御比例係数
    private static final int   PWM_ABS_MAX          = 60;             // 完全停止用モータ制御PWM絶対最大値
    private static final float THRESHOLD = 0.30F;  // ライントレースの目標値
    private static final float TURN_MAX = 100.0F;

    //////////////
    // Lコース  //
    //////////////

    private static float Kp1 = 3.0F, Kp2 = 3.0F;  // Lコース
    // 距離
    private final int CALL_DISTANCE1 = 10000;
    private final int CALL_DISTANCE2 = 10500;  // ゴールとルックアップの間
    //private final int comoletionDistance = 10500;
    private final int comoletionDistance = 1000;

    private static float baseForward = 70.0F;
    private static float sensor_val;
    private static float target_val;
    private static float difference_val;

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

    private int         driveCallCounter = 0;
    private boolean     sonarAlert   = false;

    // 非同期メソッド
    private Thread call1, call2;

    // 距離測定用のフラグ
    private Boolean callFlag1 = true;
    private Boolean callFlag2 = true;

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

        // スピーカー
        call1 = new Thread(() -> {
    		Sound.playTone(400,150);
    		Sound.playTone(450,150);
    	});
        call2 = new Thread(() -> {
    		Sound.playTone(400,300);
    	});
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
        gyro.reset();
        motorPortL.controlMotor(0, 0);
        motorPortR.controlMotor(0, 0);
        motorPortT.controlMotor(0, 0);
        motorPortL.resetTachoCount();   // 左モータエンコーダリセット
        motorPortR.resetTachoCount();   // 右モータエンコーダリセット
        motorPortT.resetTachoCount();   // 尻尾モータエンコーダリセット
        Balancer.init();                // 倒立振子制御初期化
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
     * 走行制御。
     */
    public boolean controlDrive() {
    	boolean nextFlag = false;
        float forward =  0.0F; // 前後進命令
        float turn    =  0.0F; // 旋回命令
        // 一定以上進んだらルックアップへ
        if (motorPortR.getTachoCount() > comoletionDistance) {
        	nextFlag = true;
        }
        forward = baseForward;  // 前進命令
        float p; // 比例定数

        sensor_val = getBrightness();
        target_val = THRESHOLD;
        difference_val = sensor_val - target_val;

        if ( 0 < difference_val ) {
        	p = Kp1 * difference_val;
        } else {
        	p = Kp2 * difference_val;
        }
        turn = TURN_MAX * p;
        turn = turn * -1;  // 右エッジ

        if (turn > TURN_MAX) {
        	turn = TURN_MAX;
        } else if ( -TURN_MAX > turn){
        	turn = -TURN_MAX;
        }

        float gyroNow = getGyroValue();                 // ジャイロセンサー値
        int thetaL = motorPortL.getTachoCount();        // 左モータ回転角度
        int thetaR = motorPortR.getTachoCount();        // 右モータ回転角度
        int battery = Battery.getVoltageMilliVolt();    // バッテリー電圧[mV]
        Balancer.control (forward, turn, gyroNow, GYRO_OFFSET, thetaL, thetaR, battery); // 倒立振子制御
        motorPortL.controlMotor(Balancer.getPwmL(), 1); // 左モータPWM出力セット
        motorPortR.controlMotor(Balancer.getPwmR(), 1); // 右モータPWM出力セット

/*
        // 距離測定
        if(motorPortR.getTachoCount() > CALL_DISTANCE1 && callFlag1) {
        	call1.start();
        	callFlag1 = false;
        }

        if(motorPortR.getTachoCount() > CALL_DISTANCE2 && callFlag2) {
        	call2.start();
        	callFlag2 = false;
        }
*/

        return nextFlag;
    }

    //
    // ルックアップに使う変数 (こんな書き方したらダメ、そのうち直す)
    //
    private int lookupStateNum = 0;
    private int timeCounter = 0;
    private boolean firstFlag = true;
    private int firstAngle;

    public static final int FORWARD_SPEED = 20;
    public static final int DIFF_SPEED = 5;

    public static final int TAIL_ANGLE = 85;    // 尻尾走行時の角度[度]
    public static final int DAWN_ANGLE = 63;    // 倒れた状態の角度[度]

    public static final int DOWN_TIME = 7000;   // 倒れるのにかける時間[ms]
    public static final int UP_TIME = 5000;   // 起き上がるのにかける時間[ms]
    public static final int STRAIGHT_DISTANCE = 600; // 前進するときに進む距離

    private static final float P_SLOW_GAIN          = 0.5F;
    private static final float P_GETUP_GAIN         = 4.5F;
    private static final float P_HARD_GAIN          = 6.0F;

    private static final int   PWM_GETUP_MAX              = 80;       // 起き上がるときのPWM絶対最大値

    /**
     * ルックアップ制御
     */
    public boolean controlLookup() {
    	boolean nextFlag = false;
    	float turn;

    	switch (lookupStateNum) {
    		case 0: // ゲートを見つけるまで
    			if (goUntilGate()) {
    				lookupStateNum++;
    			}
    			break;
                case 1:  // 尻尾下ろし
                    controlTailSlow(TAIL_ANGLE);
                    turn = getPTurnValue();
                    setBalancerParm(0.0F, turn);
                    motorPortL.controlMotor(Balancer.getPwmL(), 1); // 左モータPWM出力セット
                    motorPortR.controlMotor(Balancer.getPwmR(), 1); // 右モータPWM出力セット
                    if (++timeCounter > 3000 / 4) { // 約3000ms
                    	lookupStateNum++;
                        timeCounter = 0;
                    }
                    break;
                case 2:  // 揺れなくなるまで待機
                    controlTailSlow(TAIL_ANGLE);
                    turn = getPTurnValue();
                    setBalancerParm(0.0F, turn);
                    motorPortL.controlMotor(Balancer.getPwmL(), 1); // 左モータPWM出力セット
                    motorPortR.controlMotor(Balancer.getPwmR(), 1); // 右モータPWM出力セット
                    if(getGyroValue() < 0) {
                    	lookupStateNum++;
                    }
                    break;
                case 3:  // 倒れる
                    controlTailPropUP(TAIL_ANGLE);
                    setBalancerParm(0.0F, 0.0F, -30.0F);
                    motorPortL.controlMotor(Balancer.getPwmL(), 1); // 左モータPWM出力セット
                    motorPortR.controlMotor(Balancer.getPwmR(), 1); // 右モータPWM出力セット
                    if(++timeCounter > 200/4){ // 約200ms
                    	lookupStateNum++;
                        timeCounter = 0;
                    }
                    break;
                case 4:  // 角度キープ
                    controlTailPropUP(TAIL_ANGLE);
                    setBalancerParm(0.0F, 0.0F);
                    motorPortL.controlMotor(0, 1); // 左モータPWM出力セット
                    motorPortR.controlMotor(0, 1); // 右モータPWM出力セット
                    if(++timeCounter > 2000/4){ // 約2000ms
                    	lookupStateNum++;
                        timeCounter = 0;
                    }
                    break;
                case 5:  // さらに倒れる
                    if (fallBackward(DAWN_ANGLE, DOWN_TIME)){
                    	lookupStateNum++;
                    }
                    break;
                case 6:  // 前進する
                    controlTailPropUP(DAWN_ANGLE);
                    if (goStraight(STRAIGHT_DISTANCE)) {
                    	lookupStateNum++;
                    }
                    break;
                case 7:  // 戻ってくる
                    if (standUp(TAIL_ANGLE, UP_TIME)){
                    	lookupStateNum++;
                    }
                    break;
                case 8:  // キープ
                    LCD.drawString("TailCount"+ motorPortT.getTachoCount(), 0, 4);
                    controlTailPropUP(TAIL_ANGLE);
                    motorPortL.controlMotor(0, 1); // 左モータPWM出力セット
                    motorPortR.controlMotor(0, 1); // 右モータPWM出力セット
                    break;
    	}

    	return nextFlag;
    }


    /*
     * ゲート検知までゆっくり前進
     */
    private boolean goUntilGate() {
        if (++driveCallCounter >= 40/4) {  // 約40msごとに障害物検知
            sonarAlert = alertObstacle();  // 障害物検知
            driveCallCounter = 0;
        }
        float forward =  0.0F; // 前後進命令
        float turn    =  0.0F; // 旋回命令
        forward = 10.0F;  // 前進命令
        float p; // 比例定数

        sensor_val = getBrightness();
        target_val = THRESHOLD;
        difference_val = sensor_val - target_val;

        if ( 0 < difference_val ) {
        	p = Kp1 * difference_val;
        } else {
        	p = Kp2 * difference_val;
        }
        turn = TURN_MAX * p;
        turn = turn * -1;  // 右エッジ

        if (turn > TURN_MAX) {
           	turn = TURN_MAX;
        } else if ( -TURN_MAX > turn){
           	turn = -TURN_MAX;
        }

        float gyroNow = getGyroValue();                 // ジャイロセンサー値
        int thetaL = motorPortL.getTachoCount();        // 左モータ回転角度
        int thetaR = motorPortR.getTachoCount();        // 右モータ回転角度
        int battery = Battery.getVoltageMilliVolt();    // バッテリー電圧[mV]
        Balancer.control (forward, turn, gyroNow, GYRO_OFFSET, thetaL, thetaR, battery); // 倒立振子制御
        motorPortL.controlMotor(Balancer.getPwmL(), 1); // 左モータPWM出力セット
        motorPortR.controlMotor(Balancer.getPwmR(), 1); // 右モータPWM出力セット

        // ゲートを見つけたら次へ
        if (sonarAlert) {
        	return true;
        }
    	return false;
    }

    private float getPTurnValue() {
        float p; // 比例定数
        float turn    =  0.0F; // 旋回命令
        sensor_val = getBrightness();
        target_val = THRESHOLD;
        difference_val = sensor_val - target_val;

        if ( 0 < difference_val ) {
        	p = Kp1 * difference_val;
        } else {
        	p = Kp2 * difference_val;
        }
        turn = TURN_MAX * p;
        turn = turn * -1;  // 右エッジ

        if (turn > TURN_MAX) {
           	turn = TURN_MAX;
        } else if ( -TURN_MAX > turn){
           	turn = -TURN_MAX;
        }

        return turn;
    }

    /*
     * 指定した角度まで、指定された時間をかけて倒れる
     * @return 目標角度までの調整完了
     */
    private boolean fallBackward(int angle, int msec) {
        if (firstFlag) {
            firstAngle = motorPortT.getTachoCount();
            firstFlag = false;
        }

        double progress = timeCounter /(msec/4.0);
        int diffAngle = angle - firstAngle;
        int currentAngle = (int) (firstAngle + (diffAngle * progress));

        controlTailPropUP(currentAngle);
        if (++timeCounter > msec/4){
            timeCounter = 0;
            firstFlag = true;
            return true;
        }
        return false;
    }

    /*
     * 指定した角度まで、指定された時間をかけて起き上がる
     * @return 目標角度までの調整完了
     */
    private boolean standUp(int angle, int msec) {
        if (firstFlag) {
            firstAngle = motorPortT.getTachoCount();
            firstFlag = false;
        }

        LCD.drawString("TailCount"+ motorPortT.getTachoCount(), 0, 3);
        double progress = timeCounter / (msec/4.0);
        int diffAngle = angle - firstAngle;
        int currentAngle = (int) (firstAngle + (diffAngle * progress));

        controlTailGetUp(currentAngle);
        if (++timeCounter > msec/4){
            timeCounter = 0;
            firstFlag = true;
            return true;
        }
        return false;
    }

    /*
     * 前進する
     */
    private boolean goStraight(int distance) {
        if (firstFlag) {
            resetMotor();
            firstFlag = false;
        }
        if (motorPortL.getTachoCount() > motorPortR.getTachoCount()){
            motorPortL.controlMotor(FORWARD_SPEED, 1); // 左モータPWM出力セット
            motorPortR.controlMotor(FORWARD_SPEED + DIFF_SPEED, 1); // 右モータPWM出力セット
        } else {
            motorPortL.controlMotor(FORWARD_SPEED + DIFF_SPEED, 1); // 左モータPWM出力セット
            motorPortR.controlMotor(FORWARD_SPEED, 1); // 右モータPWM出力セット
        }
        if (motorPortL.getTachoCount() > distance){
            motorPortL.controlMotor(0, 1); // 左モータPWM出力セット
            motorPortR.controlMotor(0, 1); // 右モータPWM出力セット
            return true;
        }
        return false;
    }

    /*
     * 両サイドのモータエンコーダリセット
     */
    public void resetMotor(){
        motorPortL.resetTachoCount();   // 左モータエンコーダリセット
        motorPortR.resetTachoCount();   // 右モータエンコーダリセット
    }

    /*
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

    /*
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

    /*
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


    /*
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

    /*
     * バランサーAPIの設定
     *
     */
    public void setBalancerParm(float forward, float turn){
        float gyroNow = getGyroValue();                 // ジャイロセンサー値
        int thetaL = motorPortL.getTachoCount();        // 左モータ回転角度
        int thetaR = motorPortR.getTachoCount();        // 右モータ回転角度
        int battery = Battery.getVoltageMilliVolt();    // バッテリー電圧[mV]
        Balancer.control (forward, turn, gyroNow, GYRO_OFFSET, thetaL, thetaR, battery); // 倒立振子制御
    }

    /*
     * バランサーAPIの設定
     *
     */
    public void setBalancerParm(float forward, float turn, float gyroOffset){
        float gyroNow = getGyroValue();                 // ジャイロセンサー値
        int thetaL = motorPortL.getTachoCount();        // 左モータ回転角度
        int thetaR = motorPortR.getTachoCount();        // 右モータ回転角度
        int battery = Battery.getVoltageMilliVolt();    // バッテリー電圧[mV]
        Balancer.control (forward, turn, gyroNow, gyroOffset, thetaL, thetaR, battery); // 倒立振子制御
    }


    /*
     * 超音波センサーによる障害物検知
     * @return true(障害物あり)/false(障害物無し)
     */
    private boolean alertObstacle() {
        float distance = getSonarDistance();
        if ((distance <= SONAR_ALERT_DISTANCE) && (distance >= 0)) {
            return true;  // 障害物を検知
        }
        return false;
    }

    /*
     * 超音波センサーにより障害物との距離を取得する。
     * @return 障害物との距離(m)。
     */
    private final float getSonarDistance() {
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

    /*
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
