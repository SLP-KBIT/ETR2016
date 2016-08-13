package jp.etrobo.ev3.framework.strategy;

import jp.etrobo.ev3.framework.motor.MotorController;
import jp.etrobo.ev3.framework.sensor.SensorController;

public class StrategySelector {
    private SensorController sensors;
    private MotorController motors;
    private ReadyStrategy readyStrategy;
    private ExceptionStrategy exceptionStrategy;
    private BasicStrategy basicStrategy;
    
    public StrategySelector(MotorController motors, SensorController sensors) {
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
