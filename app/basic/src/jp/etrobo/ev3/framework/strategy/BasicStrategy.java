package jp.etrobo.ev3.framework.strategy;

import jp.etrobo.ev3.balancer.Balancer;
import jp.etrobo.ev3.framework.calculator.Calculator;
import jp.etrobo.ev3.framework.motor.MotorController;
import jp.etrobo.ev3.framework.sensor.SensorController;
import lejos.hardware.Battery;
import lejos.hardware.lcd.LCD;

public class BasicStrategy extends BaseStrategy {
    private static final float GYRO_OFFSET = 0.0F;
    private static final float THRESHOLD = 0.2F;
    private int timeCounter = 0;
    private Calculator calucrator;
   
    public BasicStrategy(MotorController motors, SensorController sensors) {
        super(motors, sensors);
        this.calucrator = new Calculator();
        this.state = State.NOT_SELECTED;
    }

    @Override
    public State execute() {
        LCD.drawString("execute the BaiscStrategy", 0, 4);
        switch (this.state) {
        case NOT_SELECTED:
            this.state = State.READY;
            break;
        case READY:
            this.ready();
            break;
        case BASIC:
            // this.motors.controlTail(0);
            this.linetrace();
            break;
        default:
            this.state = State.EXCEPTION;
            break;
        }
        return this.state;
    }

    private void ready() {
        this.state = State.BASIC;
    }
    
    private void linetrace() {
        float forward = 20.0F;
        this.motors.controlTail(83);
        this.motors.motorPortL.controlMotor((int)forward, 1); // 左モータPWM出力セット
        this.motors.motorPortR.controlMotor((int)forward, 1); // 右モータPWM出力セット
        // TODO:
        // float forward = 30.0F;
        // float gyro = this.sensors.getGyloValue();
        // float color = this.sensors.getColorValue();
        // float turn = this.calucrator.turnPIDcalc(THRESHOLD, color);
        // int tachoL = this.motors.motorPortL.getTachoCount();
        // int tachoR = this.motors.motorPortR.getTachoCount();
        // int battery = Battery.getVoltageMilliVolt();
        // Balancer.control(forward, turn, gyro, GYRO_OFFSET, tachoL, tachoR, battery);
        // this.motors.motorPortL.controlMotor(Balancer.getPwmL(), 1); // 左モータPWM出力セット
        // this.motors.motorPortR.controlMotor(Balancer.getPwmR(), 1); // 右モータPWM出力セット
    }
}
