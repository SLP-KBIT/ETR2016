package jp.etrobo.ev3.framework.strategy;

import jp.etrobo.ev3.framework.motor.MotorStore;
import jp.etrobo.ev3.framework.sensor.SensorStore;

public abstract class BaseStrategy {
    protected State state;
    protected MotorStore motors;
    protected SensorStore sensors;

    public BaseStrategy(MotorStore motors, SensorStore sensors) {
       this.state = State.NOT_SELECTED;
       this.motors = motors;
       this.sensors = sensors;
    }
    
    public abstract State execute();
    
    public State getState() {
        return this.state;
    }
}
