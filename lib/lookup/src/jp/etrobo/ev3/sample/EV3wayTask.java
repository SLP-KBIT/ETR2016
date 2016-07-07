/*
 *  EV3wayTask.java (for leJOS EV3)
 *  Created on: 2016/02/11
 *  Copyright (c) 2016 Embedded Technology Software Design Robot Contest
 */
package jp.etrobo.ev3.sample;

/**
 * EV3way を制御するタスク。
 */
public class EV3wayTask implements Runnable {

    /**
     * コンストラクタ。
     * @param way EV3本体
     */
    public EV3wayTask() {
    	StrategyMode.setDriveMode();
    }

    /**
     * EV3本体の制御。
     */
    @Override
    public void run() {
    	StrategyMode.strategyRun();
    	//body.controlDrive();
        //body.controlTail(EV3way.TAIL_ANGLE_DRIVE);
    }
}

