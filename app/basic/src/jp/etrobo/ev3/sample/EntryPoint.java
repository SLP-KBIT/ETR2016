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

    public EntryPoint() {
        this.robot = new EV3way();
        this.robot.idling();
        this.robot.ready();

        this.driveTask = new EV3wayTask(this.robot);
        this.remoteTask = new RemoteTask();
        this.scheduler = Executors.newScheduledThreadPool(3);
    }

    private void startup() {
        this.futureDrive =
            this.scheduler.scheduleAtFixedRate(this.driveTask, 0, 4, TimeUnit.MILLISECONDS);
        this.futureRemote =
            this.scheduler.scheduleAtFixedRate(this.remoteTask, 0, 100, TimeUnit.MILLISECONDS);
    }

    private void shutdown() {
        this.scheduler.shutdownNow();
    }

    public static void main(String[] args) {
        EntryPoint main = new EntryPoint();
        main.startup();
        main.shutdown();
    }
}
