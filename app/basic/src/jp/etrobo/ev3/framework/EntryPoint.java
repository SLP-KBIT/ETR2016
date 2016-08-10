package jp.etrobo.ev3.framework;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import jp.etrobo.ev3.framework.tasks.Driver;
import jp.etrobo.ev3.framework.tasks.RemoteTask;

public class EntryPoint {
    private ScheduledExecutorService scheduler;

    private Driver driver;
    private RemoteTask remoteTask;

    private ScheduledFuture<?> futureDrive;
    private ScheduledFuture<?> futureRemote;


    public EntryPoint() {
    }

    public static void main(String[] args) {
    }
}
