package jp.etrobo.ev3.framework.strategy;

import jp.etrobo.ev3.framework.motor.MotorStore;
import jp.etrobo.ev3.framework.sensor.SensorStore;

public class StrategySelector {
    private SensorStore sensors;
    private MotorStore motors;
    private ExceptionStrategy exceptionStrategy;
    
    public StrategySelector(MotorStore motors, SensorStore sensors) {
        this.sensors = sensors;
        this.motors = motors;
        this.exceptionStrategy = new ExceptionStrategy(motors, sensors);
    }

    public BaseStrategy select() {
        return this.exceptionStrategy;
    }
}
