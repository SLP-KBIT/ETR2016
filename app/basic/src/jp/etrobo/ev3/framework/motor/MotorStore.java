package jp.etrobo.ev3.framework.motor;

import lejos.hardware.port.BasicMotorPort;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;
import lejos.hardware.port.TachoMotorPort;

public class MotorStore {
    private static final Port  MOTORPORT_LWHEEL = MotorPort.C; // 左モータポート
    private static final Port  MOTORPORT_RWHEEL = MotorPort.B; // 右モータポート
    private static final Port  MOTORPORT_TAIL   = MotorPort.A; // 尻尾モータポート
    
    // モータ制御用オブジェクト
    public TachoMotorPort motorPortL; // 左モータ
    public TachoMotorPort motorPortR; // 右モータ
    public TachoMotorPort motorPortT; // 尻尾モータ
    
    public MotorStore() {
        this.__initalizeAllMotor();
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
