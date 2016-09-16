package jp.etrobo.ev3.device;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.sensor.SensorMode;

public class TouchSensor {
	private static TouchSensor touchSensor = new TouchSensor();

    // タッチセンサ
    static EV3TouchSensor touch;
    static SensorMode touchMode;
    static float[] sampleTouch;
    final Port  SENSORPORT_TOUCH     = SensorPort.S1;  // タッチセンサーポート

    private TouchSensor() {
        touch = new EV3TouchSensor(SENSORPORT_TOUCH);
        touchMode = touch.getTouchMode();
        sampleTouch = new float[touchMode.sampleSize()];
    }

    public static TouchSensor getInstance() {
    	return touchSensor;
    }

    /**
     * タッチセンサー押下のチェック。
     * @return true ならタッチセンサーが押された。
     */
    public final boolean touchSensorIsPressed() {
        touchMode.fetchSample(sampleTouch, 0);
        return ((int)sampleTouch[0] != 0);
    }
}
