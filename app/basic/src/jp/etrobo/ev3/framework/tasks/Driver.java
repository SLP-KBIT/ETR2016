/*
 *  EV3wayTask.java (for leJOS EV3)
 *  Created on: 2016/02/11
 *  Copyright (c) 2016 Embedded Technology Software Design Robot Contest
 */
package jp.etrobo.ev3.framework.tasks;

import jp.etrobo.ev3.framework.motor.MotorStore;
import jp.etrobo.ev3.framework.sensor.SensorStore;
import jp.etrobo.ev3.framework.strategy.BaseStrategy;
import jp.etrobo.ev3.framework.strategy.StrategySelector;

public class Driver implements Runnable {
    private MotorStore motors;
    private SensorStore sensors;
    private StrategySelector strategySelector;
    
    public Driver() {
        this.sensors = new SensorStore();
        this.motors = new MotorStore();
        this.strategySelector = new StrategySelector(motors, sensors);
    }

    @Override
    public void run() {
        this.sensors.updateSensorValues();
        BaseStrategy strategy = this.strategySelector.select();
        strategy.execute();
    }
}
