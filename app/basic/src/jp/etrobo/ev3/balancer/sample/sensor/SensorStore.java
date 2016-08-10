package jp.etrobo.ev3.balancer.sample.sensor;

import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorMode;
import lejos.robotics.SampleProvider;

public class SensorStore {
    // private fields {{{
    // ports {{{
    private static final Port SENSORPORT_TOUCH = SensorPort.S1; // タッチセンサーポート
    private static final Port SENSORPORT_SONAR = SensorPort.S2; // 超音波センサーポート
    private static final Port SENSORPORT_COLOR = SensorPort.S3; // カラーセンサーポート
    private static final Port SENSORPORT_GYRO  = SensorPort.S4; // ジャイロセンサーポート
    // }}}
    // タッチセンサ {{{
    private EV3TouchSensor touch;
    private SensorMode touchMode;
    private float[] sampleTouch;
    // }}}
    // 超音波センサ {{{
    private EV3UltrasonicSensor sonar;
    private SampleProvider distanceMode;
    private float[] sampleDistance;
    private int cycleCounter = 0;
    // }}}
    // カラーセンサ {{{
    private EV3ColorSensor colorSensor;
    private SensorMode redMode;
    private float[] sampleLight;
    // }}}
    // ジャイロセンサ {{{
    private EV3GyroSensor gyro;
    private SampleProvider rate;
    private float[] sampleGyro; 
    // }}}
    // }}}
    
    public SensorStore() {
        this.__initalizeColorSensor();
        this.__initalizeGyroSensor();
        this.__initalizeSonicSensor();
        this.__initalizeTouchSensor();
        this.resetGyro();
    }

    /*
     * SensorStore の field のセンサー値を更新します
     * 4ms 毎に毎回この関数を飛び出してください
     */
    public void updateSensorValues() {
        this.touchMode.fetchSample(sampleTouch, 0);
        this.redMode.fetchSample(sampleLight, 0);
        this.rate.fetchSample(sampleGyro, 0);
        this.fetchSampleSonic();
    }

    public void resetGyro() { this.gyro.reset(); }
    public float getGyloValue() { return this.sampleGyro[0]; }
    public float getColorValue() { return this.sampleLight[0]; }
    public float getTouchValue() { return this.sampleTouch[0]; }
    public float getDistanceValue() { return this.sampleDistance[0]; }
    public boolean isPressed() { return (int)this.sampleTouch[0] == 0; }

    
    // private methods {{{
    private void __initalizeTouchSensor() {
        this.touch = new EV3TouchSensor(SENSORPORT_TOUCH);
        this.touchMode = this.touch.getTouchMode();
        this.sampleDistance = new float[this.touchMode.sampleSize()];
    }
    private void __initalizeSonicSensor() {
        this.sonar = new EV3UltrasonicSensor(SENSORPORT_SONAR);
        this.distanceMode = this.sonar.getDistanceMode(); // 距離検出モード
        this.sampleDistance = new float[this.distanceMode.sampleSize()];
        this.sonar.enable();
    }
    private void __initalizeColorSensor() {
        this.colorSensor = new EV3ColorSensor(SENSORPORT_COLOR);
        this.redMode = this.colorSensor.getRedMode();     // 輝度検出モード
        this.sampleLight = new float[this.redMode.sampleSize()];
    }
    private void __initalizeGyroSensor() {
        this.gyro = new EV3GyroSensor(SENSORPORT_GYRO);
        this.rate = this.gyro.getRateMode();              // 角速度検出モード
        this.sampleGyro = new float[this.rate.sampleSize()];
    }
    
    /*
     * distanceMode.fetchSample の wrapper
     * 40ms 毎に1度 センサーから値を取得し更新します
     */
    private void fetchSampleSonic() {
        if (this.cycleCounter == 10) {
            this.distanceMode.fetchSample(sampleDistance, 0);
            this.cycleCounter = 0;
        } else {
            this.cycleCounter++;
        }
    }
    // }}}

}
