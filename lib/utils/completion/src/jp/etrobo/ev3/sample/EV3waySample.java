/*
 *  EV3waySample.java (for leJOS EV3)
 *  Created on: 2016/02/11
 *  Copyright (c) 2016 Embedded Technology Software Design Robot Contest
 */
package jp.etrobo.ev3.sample;

import java.math.BigDecimal;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;
import lejos.utility.Delay;

/**
 * 2輪倒立振子ライントレースロボットの leJOS EV3 用 Java サンプルプログラム。
 */
public class EV3waySample {
    private EV3way         body;            // EV3 本体
    private boolean        touchPressed;    // タッチセンサーが押されたかの状態

    // スケジューラ
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> futureDrive;
    private ScheduledFuture<?> futureRemote;

    // タスク
    private EV3wayTask  driveTask;   // 走行制御
    private RemoteTask  remoteTask;  // リモート制御

    // パラメータセッティング用
	private int parmSelect = 0;
	private BigDecimal p = new BigDecimal("0.36");
	private BigDecimal i = new BigDecimal("0.5");
	private BigDecimal d = new BigDecimal("0.5");
	private BigDecimal forward = new BigDecimal("30.0");
	private BigDecimal fixPIDValue = new BigDecimal("0.01");
	private BigDecimal fixForwardValue = new BigDecimal("5.0");

    /**
     * コンストラクタ。
     * スケジューラとタスクオブジェクトを作成。
     */
    public EV3waySample() {
        body = new EV3way();
        body.idling();
        body.reset();
        touchPressed = false;

        scheduler  = Executors.newScheduledThreadPool(2);
        driveTask  = new EV3wayTask(body);
        remoteTask = new RemoteTask();
        futureRemote = scheduler.scheduleAtFixedRate(remoteTask, 0, 100, TimeUnit.MILLISECONDS);
    }

    /**
     * スタート前の作業。
     * 尻尾を完全停止位置に固定し、スタート指示があるかをチェックする。
     * @return true=wait / false=start
     */
    public boolean waitForStart() {
        boolean res = true;
        body.controlTail(EV3way.TAIL_ANGLE_STAND_UP);
        if (body.touchSensorIsPressed()) {
            touchPressed = true;          // タッチセンサーが押された
        } else {
            if (touchPressed) {
                res = false;
                touchPressed = false;     // タッチセンサーが押された後に放した
            }
        }
        if (remoteTask.checkRemoteCommand(RemoteTask.REMOTE_COMMAND_START)) {  // PC で 'g' キーが押された
            res = false;
        }
        return res;
    }

    /**
     * 終了指示のチェック。
     */
    public boolean waitForStop() {
    	boolean res = true;
        if (body.touchSensorIsPressed()) {
            touchPressed = true;          // タッチセンサーが押された
        } else {
            if (touchPressed) {
                res = false;
                touchPressed = false;     // タッチセンサーが押された後に放した
            }
        }
        if (remoteTask.checkRemoteCommand(RemoteTask.REMOTE_COMMAND_STOP)) { // PC で 's' キー押されたら走行終了
            res = false;
        }
        return res;
    }

    /**
     * 走行開始時の作業スケジューリング。
     */
    public void start() {
        futureDrive = scheduler.scheduleAtFixedRate(driveTask, 0, 4, TimeUnit.MILLISECONDS);
    }

    /**
     * 走行終了時のタスク終了後処理。
     */
    public void stop () {
        if (futureDrive != null) {
            futureDrive.cancel(true);
            body.close();
        }
        if (futureRemote != null) {
            futureRemote.cancel(true);
            remoteTask.close();
        }
    }

    /**
     * スケジューラのシャットダウン。
     */
    public void shutdown() {
        scheduler.shutdownNow();
    }

    /**
     * パラメータをボタンで設定
     */
    public void parmSetting() {
		if(Button.UP.isDown()){
			parmSelect--;
			if(parmSelect < 0) {
				parmSelect = 0;
			}
		}
		if(Button.DOWN.isDown()){
			parmSelect++;
			if(parmSelect > 3) {
				parmSelect = 3;
			}
		}
		if(Button.RIGHT.isDown()){
	    	switch (parmSelect) {
	    		case 0:
	    			p = p.add(fixPIDValue);
	    			break;
	    		case 1:
	    			i = i.add(fixPIDValue);
	    			break;
	    		case 2:
	    			d = d.add(fixPIDValue);
	    			break;
	    		case 3:
	    			forward = forward.add(fixForwardValue);
	    			break;
	    	}
		}
		if(Button.LEFT.isDown()){
	    	switch (parmSelect) {
	    		case 0:
	    			p = p.subtract(fixPIDValue);
	    			break;
	    		case 1:
	    			i = i.subtract(fixPIDValue);
	    			break;
	    		case 2:
	    			d = d.subtract(fixPIDValue);
	    			break;
	    		case 3:
	    			forward = forward.subtract(fixForwardValue);
	    			break;
	    	}
		}
		LCD.clear();
		LCD.drawString("P:" + p, 0, 0);
		LCD.drawString("I:" + i, 0, 1);
		LCD.drawString("D:" + d, 0, 2);
		LCD.drawString("forward:" + forward, 0, 3);
		LCD.drawString("select:" + parmSelect, 0, 5);
		LCD.refresh();
    }

    /**
     * PIDのパラメータを登録
     */
    private void setPIDParm(){
    	//float Kp = p.floatValue(), Ki = i.floatValue(), Kd = d.floatValue();
    	float Kp = 0.36F, Ki = 0.5F, Kd = 0.5F;
    	body.setPIDParm(Kp, Ki, Kd);
    }

    /**
     * フォワード値のパラメータを登録
     */
	private void setForwardParm() {
		float baseForward = forward.floatValue();
		body.setForwardParm(baseForward);
	}

    /**
     * メイン
     */
    public static void main(String[] args) {
    	LCD.drawString("Please Wait...  ", 0, 4);
		EV3waySample program = new EV3waySample();

    	// スタート待ち
    	LCD.drawString("Touch to START", 0, 4);

    	while (program.waitForStart()) {
    		program.parmSetting();
    		Delay.msDelay(100);
    	}
    	program.setPIDParm();
    	program.setForwardParm();
    	LCD.clear();

    	LCD.drawString("Running       ", 0, 4);
    	program.start();
    	while (program.waitForStop()) {
    		Delay.msDelay(100);
    	}

    	program.stop();
    	program.shutdown();
    }
}