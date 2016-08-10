package jp.etrobo.ev3.sample;

import lejos.hardware.lcd.LCD;
import lejos.utility.Delay;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import jp.etrobo.ev3.sample.tasks.EV3wayTask;
import jp.etrobo.ev3.sample.tasks.RemoteTask;

public class EntryPoint {
    private ScheduledExecutorService scheduler;

    private EV3wayTask driveTask;
    private RemoteTask remoteTask;
    private EV3way robot;

    private ScheduledFuture<?> futureDrive;
    private ScheduledFuture<?> futureRemote;

    private boolean touchPressed = false;

    public EntryPoint() {
        this.robot = new EV3way();
        this.robot.idling();
        this.robot.ready();

        this.driveTask = new EV3wayTask(this.robot);
        this.remoteTask = new RemoteTask();
        this.scheduler = Executors.newScheduledThreadPool(2);
    }

    private void startup() {
        this.futureDrive =
            this.scheduler.scheduleAtFixedRate(this.driveTask, 0, 4, TimeUnit.MILLISECONDS);
        this.futureRemote =
            this.scheduler.scheduleAtFixedRate(this.remoteTask, 0, 100, TimeUnit.MILLISECONDS);
    }

    private void stop () {
        if (futureDrive != null) {
            futureDrive.cancel(true);
            this.robot.close();
        }
        if (futureRemote != null) {
            futureRemote.cancel(true);
            remoteTask.close();
        }
    }

    private void shutdown() { this.scheduler.shutdownNow(); }
    private boolean waitForStart() {
        boolean response = true;
        this.robot.controllTail(EV3way.TAIL_ANGLE_STAND_UP);
        if (this.robot.isPressed()) {
            this.touchPressed = true;
        } else {
            if (this.touchPressed) {
                this.touchPressed = false;
                response = false;
            }
        }
        if (remoteTask.checkRemoteCommand(RemoteTask.REMOTE_COMMAND_START)) {
            response = false;
        }
        return response;
    }
    private boolean waitForStop() {
        boolean response = true;
        if (this.robot.isPressed()) {
            this.touchPressed = true;
        } else if (this.touchPressed) {
            response = false;
            this.touchPressed = false;
        }
        if (remoteTask.checkRemoteCommand(RemoteTask.REMOTE_COMMAND_STOP)) { 
            response = false;
        }
        return response;
    }
    
    public static void main(String[] args) {
        LCD.drawString("Please Wait...  ", 0, 4);
        EntryPoint main = new EntryPoint();

        LCD.drawString("Touch to START", 0, 4);
        while (main.waitForStart()) { Delay.msDelay(100); }

        LCD.drawString("Running       ", 0, 4);
        main.startup();
        
        while (main.waitForStop()) { Delay.msDelay(100); }
        main.stop();
        main.shutdown();
    }
}
