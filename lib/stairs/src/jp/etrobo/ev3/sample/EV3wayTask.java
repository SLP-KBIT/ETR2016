/*
 *  EV3wayTask.java (for leJOS EV3)
 *  Created on: 2016/02/11
 *  Copyright (c) 2016 Embedded Technology Software Design Robot Contest
 */
package jp.etrobo.ev3.sample;

import jp.etrobo.ev3.sample.Section;

/**
 * EV3way を制御するタスク。
 */
public class EV3wayTask implements Runnable {
    private EV3way body;
    private BaseStrategy strategy;

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
        Section section = StrategySelector.getCurrentSection();
        switch (section) {
        case BASIC_A:
            // this.strategy = 
            break;
        case BASIC_B:
            // this.strategy = 
            break;
        case STAIRS:
            this.strategy = new StairsStrategy(80.0F);
            break;
        case GATE:
            break;
        case GARAGE:
            break;
        }
        this.strategy.executeStrategy(body);
    }
}
