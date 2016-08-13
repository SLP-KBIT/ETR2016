package jp.etrobo.ev3.framework.motor;

import jp.etrobo.ev3.balancer.Balancer;
import lejos.hardware.port.BasicMotorPort;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;
import lejos.hardware.port.TachoMotorPort;

public class MotorController {
    private static final Port  MOTORPORT_LWHEEL = MotorPort.C; // 左モータポート
    private static final Port  MOTORPORT_RWHEEL = MotorPort.B; // 右モータポート
    private static final Port  MOTORPORT_TAIL   = MotorPort.A; // 尻尾モータポート
    private static final float P_GAIN       = 2.5F;  // 完全停止用モータ制御比例係数
    private static final int   PWM_ABS_MAX  = 60;    // 完全停止用モータ制御PWM絶対最大値
    
    // モータ制御用オブジェクト
    public TachoMotorPort motorPortL; // 左モータ
    public TachoMotorPort motorPortR; // 右モータ
    public TachoMotorPort motorPortT; // 尻尾モータ
    
    public MotorController() {
        this.__initalizeAllMotor();
    }
   
    public void idling() {
        this.motorPortL.controlMotor(0, 0);
        this.motorPortR.controlMotor(0, 0);
        this.motorPortT.controlMotor(0, 0);
    }
    
    public void reset() {
        this.motorPortL.controlMotor(0, 0);
        this.motorPortR.controlMotor(0, 0);
        this.motorPortT.controlMotor(0, 0);
        this.motorPortL.resetTachoCount();   // 左モータエンコーダリセット
        this.motorPortR.resetTachoCount();   // 右モータエンコーダリセット
        this.motorPortT.resetTachoCount();   // 尻尾モータエンコーダリセット
        Balancer.init();                // 倒立振子制御初期化
    }
    

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
    public void close() {
        this.motorPortL.close();
        this.motorPortR.close();
        this.motorPortT.close();
    }
    
    private void __initalizeAllMotor() {
        this.motorPortL = MOTORPORT_LWHEEL.open(TachoMotorPort.class); // 左モータ
        this.motorPortR = MOTORPORT_RWHEEL.open(TachoMotorPort.class); // 右モータ
        this.motorPortT = MOTORPORT_TAIL.open(TachoMotorPort.class);   // 尻尾モータ
        this.motorPortL.setPWMMode(BasicMotorPort.PWM_BRAKE);
        this.motorPortR.setPWMMode(BasicMotorPort.PWM_BRAKE);
        this.motorPortT.setPWMMode(BasicMotorPort.PWM_BRAKE);
    }
}
