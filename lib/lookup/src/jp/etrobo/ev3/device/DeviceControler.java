package jp.etrobo.ev3.device;

public abstract class DeviceControler {
	// センサー
	protected ColorSensor colorSensor = ColorSensor.getInstance();
	protected GyroSensor gyroSensor = GyroSensor.getInstance();
	protected SonarSensor sonarSensor = SonarSensor.getInstance();
	protected TouchSensor touchSensor = TouchSensor.getInstance();

	// モーター
	protected LeftMotor leftMotor = LeftMotor.getInstance();
	protected RightMotor rightMotor = RightMotor.getInstance();
}
