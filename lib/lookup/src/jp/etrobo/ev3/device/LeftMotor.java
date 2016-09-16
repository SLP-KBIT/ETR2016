package jp.etrobo.ev3.device;

import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;
import lejos.hardware.port.TachoMotorPort;

public class LeftMotor {
	private static LeftMotor leftMotor = new LeftMotor();

    private final Port  MOTORPORT_LWHEEL     = MotorPort.C;    // 左モータポート
    static TachoMotorPort motorPortL; // 右モータ

    private LeftMotor () {
        motorPortL = MOTORPORT_LWHEEL.open(TachoMotorPort.class); // 右モータ
        motorPortL.controlMotor(0, 0);
        motorPortL.resetTachoCount();   // 右モータエンコーダリセット
    }

    public static LeftMotor getInstance() {
    	return leftMotor;
    }

    public void controlMotor(int pwm, int mode) {
    	motorPortL.controlMotor(pwm, mode);
    }

    public int getTachoCount() {
    	return motorPortL.getTachoCount();
    }

    public void resetTachoCount() {
    	motorPortL.resetTachoCount();
    }

    public void close() {
    	motorPortL.close();
    }
}
