package jp.etrobo.ev3.framework.strategy;

import jp.etrobo.ev3.framework.motor.MotorStore;
import jp.etrobo.ev3.framework.sensor.SensorStore;

public class StrategySelector {
    private SensorStore sensors;
    private MotorStore motors;
    private ReadyStrategy readyStrategy;
    private ExceptionStrategy exceptionStrategy;
    private BasicStrategy basicStrategy;
    
    public StrategySelector(MotorStore motors, SensorStore sensors) {
        this.sensors = sensors;
        this.motors = motors;
        this.readyStrategy = new ReadyStrategy(motors, sensors);
        this.exceptionStrategy = new ExceptionStrategy(motors, sensors);
        this.basicStrategy = new BasicStrategy(motors, sensors);
    }

    public BaseStrategy select() {
        if (!this.isClear(this.readyStrategy)) {
            return this.readyStrategy;
        } else if (!this.isClear(this.basicStrategy)) {
            return this.basicStrategy;
        }
        return this.exceptionStrategy;
    }
    
    private boolean isClear(BaseStrategy strategy) {
        return strategy.getState() == State.CLEAR;
    }
}
