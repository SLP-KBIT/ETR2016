/*
 *  EV3wayTask.java (for leJOS EV3)
 *  Created on: 2016/02/11
 *  Copyright (c) 2016 Embedded Technology Software Design Robot Contest
 */
package jp.etrobo.ev3.framework.tasks;

import jp.etrobo.ev3.balancer.Balancer;
import jp.etrobo.ev3.framework.motor.MotorStore;
import jp.etrobo.ev3.framework.sensor.SensorStore;
import jp.etrobo.ev3.framework.strategy.BaseStrategy;
import jp.etrobo.ev3.framework.strategy.StrategySelector;
import lejos.hardware.Battery;
import lejos.hardware.lcd.LCD;
import lejos.utility.Delay;

public class Driver implements Runnable {
    private MotorStore motors;
    private SensorStore sensors;
    private StrategySelector strategySelector;
    
    public Driver() {
        this.sensors = new SensorStore();
        this.motors = new MotorStore();
        this.strategySelector = new StrategySelector(motors, sensors);
    }
    
    public void idling() {
        LCD.clear();
        LCD.drawString("idling now", 0, 4);
        Balancer.init();
        for (int i = 0; i < 1500; i++) {
            this.sensors.idling();
            this.motors.idling();
            Battery.getVoltageMilliVolt();
            Balancer.control(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 8000);
        }
        Delay.msDelay(10000);
        this.sensors.resetGyro();
        this.motors.reset();
    }

    public void close() {
        this.sensors.close();
        this.motors.close();
    }

    @Override
    public void run() {
        this.sensors.updateSensorValues();
        BaseStrategy strategy = this.strategySelector.select();
        strategy.execute();
    }
}