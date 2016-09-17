/*
 *  EV3wayTask.java (for leJOS EV3)
 *  Created on: 2016/02/11
 *  Copyright (c) 2016 Embedded Technology Software Design Robot Contest
 */
package jp.etrobo.ev3.sample;

import lejos.hardware.Button;

/**
 * EV3way を制御するタスク。
 */
public class EV3wayTask implements Runnable {
    private EV3way body;
    private int stateNum = 0;

    /**
     * コンストラクタ。
     * @param way EV3本体
     */
    public EV3wayTask(EV3way way) {
        body = way;
    }

    /**
     * EV3本体の制御。
     */
    @Override
    public void run() {
    	switch (stateNum) {
    		case 0: // コース完走
    			if (body.controlDrive()) {
    				stateNum++;
    			}
    			body.controlTail(EV3way.TAIL_ANGLE_DRIVE);
    			break;
    		case 1: //ルックアップ
    			Button.LEDPattern(1);
    			if (body.controlLookup()) {
    				//stateNum++;
    			}
    			break;
    	}
    }
}
