/*
 *  EV3waySample.java (for leJOS EV3)
 *  Created on: 2016/02/11
 *  Copyright (c) 2016 Embedded Technology Software Design Robot Contest
 */
package jp.etrobo.ev3.sample;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.utility.Delay;

/**
 * 2輪倒立振子ライントレースロボットの leJOS EV3 用 Java サンプルプログラム。
 */
public class Lookup {
    private EV3way         body;            // EV3 本体
    private boolean        touchPressed;    // タッチセンサーが押されたかの状態
    private int standTailAngle;             // 倒立状態の尻尾の角度

    // スケジューラ
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> futureDrive;
    private ScheduledFuture<?> futureRemote;

    // タスク
    private EV3wayTask  driveTask;   // 走行制御
    private RemoteTask  remoteTask;  // リモート制御

    // 非同期メソッド
    private Thread call;
    private Thread resetGyro;

    /**
     * コンストラクタ。
     * スケジューラとタスクオブジェクトを作成。
     */
    public Lookup() {
    	LCD.clear();
        body = new EV3way();
        body.idling();
        body.reset();
        standTailAngle = EV3way.TAIL_ANGLE_STAND_UP;
        touchPressed = false;

        scheduler  = Executors.newScheduledThreadPool(2);
        driveTask  = new EV3wayTask();
        StrategyMode.initSetBody(body);
        StrategyMode.setDriveMode();
        remoteTask = new RemoteTask();
        futureRemote = scheduler.scheduleAtFixedRate(remoteTask, 0, 100, TimeUnit.MILLISECONDS);
        call = new Thread(() -> {
    		Sound.playTone(400,300);
    		Sound.playTone(450,300);
    		Sound.playTone(800,300);
    	});
        resetGyro = new Thread(() -> {
        	Sound.playTone(400,150);
        	Sound.playTone(400,150);
        	body.resetGyro();
        	call.start();
    	});
    }

    /**
     * スタート前の作業。
     * 尻尾の角度を調整する
     * @return true=wait / false=start
     */
    public boolean settingAngle(){
        boolean res = true;
        body.controlTail(standTailAngle);
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
        if(Button.UP.isDown()){
        	standTailAngle++;
        } else if(Button.DOWN.isDown()) {
        	standTailAngle--;
        }
        LCD.drawString(String.format("angle:%1$3d", standTailAngle), 0, 0);
        return res;
    }

    /**
     * ジャイロセンサーのリセット
     */
    public void resetGyro(){
    	resetGyro.start();
    }

    /**
     * スタート前の作業。
     * 尻尾を完全停止位置に固定し、スタート指示があるかをチェックする。
     * @return true=wait / false=start
     */
    public boolean waitForStart() {
        boolean res = true;
        body.controlTail(standTailAngle);
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
     * メイン
     */
    public static void main(String[] args) {
        LCD.drawString("Please Wait...  ", 0, 4);

        Lookup program = new Lookup();

        // 尻尾の角度の調整
        LCD.drawString("Setting Angle", 0, 4);
        while (program.settingAngle()) {
            Delay.msDelay(100);
        }
        program.resetGyro();

        // スタート待ち
        LCD.drawString("Touch to START", 0, 4);
        while (program.waitForStart()) {
            Delay.msDelay(100);
        }

        LCD.drawString("Running       ", 0, 4);
        program.start();
        while (program.waitForStop()) {
            Delay.msDelay(100);
        }
        program.stop();
        program.shutdown();
    }
}