package jp.etrobo.ev3.framework.strategy;

import jp.etrobo.ev3.framework.motor.MotorController;
import jp.etrobo.ev3.framework.sensor.SensorController;

public abstract class BaseStrategy {
    protected State state;
    protected MotorController motors;
    protected SensorController sensors;

    public BaseStrategy(MotorController motors, SensorController sensors) {
       this.state = State.NOT_SELECTED;
       this.motors = motors;
       this.sensors = sensors;
    }
    
    /**
     * execute()
     * 戦略を実行します
     * @return {State} 戦略の攻略状態を返却します
     */
    public abstract State execute();
    
    /**
     * getState()
     * 現在の戦略の攻略状態を返却します
     * @return {State} 現在の戦略の攻略状態
     */
    public State getState() {
        return this.state;
    }
}
