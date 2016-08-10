package jp.etrobo.ev3.framework.strategy;

import jp.etrobo.ev3.framework.motor.MotorStore;
import jp.etrobo.ev3.framework.sensor.SensorStore;
import lejos.hardware.lcd.LCD;

public class ExceptionStrategy extends BaseStrategy {
    public ExceptionStrategy(MotorStore motors, SensorStore sensors) {
        super(motors, sensors);
    }

    @Override
    public State execute() {
        LCD.drawString("execute the ExceptionStrategy", 1, 4);
        this.state = State.FAIL;
        this.motors.motorPortL.controlMotor(0, 0);
        this.motors.motorPortR.controlMotor(0, 0);
        this.motors.motorPortT.controlMotor(0, 0);
        return this.state;
    }
}
