package jp.etrobo.ev3.framework;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import jp.etrobo.ev3.framework.tasks.Driver;
import jp.etrobo.ev3.framework.tasks.RemoteTask;
import lejos.hardware.lcd.LCD;

public class EntryPoint {
    private ScheduledExecutorService scheduler;

    private Driver driver;
    private RemoteTask remoteTask;

    private ScheduledFuture<?> futureDrive;
    private ScheduledFuture<?> futureRemote;

    private ScheduledExecutorService schedule;

    public EntryPoint() {
        this.schedule = Executors.newScheduledThreadPool(2);
        this.driver = new Driver();
        this.remoteTask = new RemoteTask();
        this.futureRemote =
            scheduler.scheduleAtFixedRate(remoteTask, 0, 100, TimeUnit.MILLISECONDS);
    }

    public static void main(String[] args) {
        LCD.drawString("Please Wait...  ", 0, 4);
        EntryPoint program = new EntryPoint();

        LCD.drawString("Driving Now...", 0, 4);
        while (true) program.driveStart();
    }
    
    private void driveStart() {
        futureDrive = scheduler.scheduleAtFixedRate(driver, 0, 4, TimeUnit.MILLISECONDS);
    }
}
