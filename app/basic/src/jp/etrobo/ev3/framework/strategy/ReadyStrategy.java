package jp.etrobo.ev3.framework.strategy;

import jp.etrobo.ev3.framework.motor.MotorController;
import jp.etrobo.ev3.framework.sensor.SensorController;
import lejos.hardware.lcd.LCD;

public class ReadyStrategy extends BaseStrategy {
    private static final int STAND_UP_TAIL_ANGLE = 83;
    
    public ReadyStrategy(MotorController motors, SensorController sensors) {
        super(motors, sensors);
    }

    @Override
    public State execute() {
        LCD.drawString("execute the ReadyStrategy", 0, 4);
        if (this.sensors.isPressed()) { this.state = State.CLEAR; }
        this.motors.controlTail(STAND_UP_TAIL_ANGLE);
        return this.state;
    }
    
}
