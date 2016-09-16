package jp.etrobo.ev3.device;

import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;
import lejos.hardware.port.TachoMotorPort;

public class RightMotor {
	private static RightMotor rightMotor = new RightMotor();

    private final Port  MOTORPORT_RWHEEL     = MotorPort.B;    // 左モータポート
    static TachoMotorPort motorPortR; // 右モータ

    private RightMotor () {
        motorPortR = MOTORPORT_RWHEEL.open(TachoMotorPort.class); // 右モータ
        motorPortR.controlMotor(0, 0);
        motorPortR.resetTachoCount();   // 右モータエンコーダリセット
    }

    public static RightMotor getInstance() {
    	return rightMotor;
    }

    public void controlMotor(int pwm, int mode) {
    	motorPortR.controlMotor(pwm, mode);
    }

    public int getTachoCount() {
    	return motorPortR.getTachoCount();
    }

    public void resetTachoCount() {
    	motorPortR.resetTachoCount();
    }

    public void close() {
    	motorPortR.close();
    }
}
