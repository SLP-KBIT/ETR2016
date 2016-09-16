package jp.etrobo.ev3.device;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.robotics.SampleProvider;

public class GyroSensor {
	private static GyroSensor gyroSensor = new GyroSensor();

    // タッチセンサ
    static EV3GyroSensor gyro;
    static SampleProvider rate;          // 角速度検出モード
    static float[] sampleGyro;
    final Port  SENSORPORT_GYRO      = SensorPort.S4;  // ジャイロセンサーポート

    private GyroSensor() {
        gyro = new EV3GyroSensor(SENSORPORT_GYRO);
        rate = gyro.getRateMode();              // 角速度検出モード
        sampleGyro = new float[rate.sampleSize()];
    }

    public static GyroSensor getInstance() {
    	return gyroSensor;
    }

    /*
     * ジャイロセンサーから角速度を取得する。
     * @return 角速度。
     */
    public final float getGyroValue() {
        rate.fetchSample(sampleGyro, 0);
        // leJOS ではジャイロセンサーの角速度値が正負逆になっているので、
        // 倒立振子ライブラリの仕様に合わせる。
        return -sampleGyro[0];
    }
}
