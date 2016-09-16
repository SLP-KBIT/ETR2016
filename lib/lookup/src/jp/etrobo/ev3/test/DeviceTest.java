package jp.etrobo.ev3.test;

import jp.etrobo.ev3.device.DeviceControler;
import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;
import lejos.utility.Delay;

public class DeviceTest extends DeviceControler {

	public void showDeviceValue() {
		LCD.clear();
		LCD.drawString("RightMotor:" + rightMotor.getTachoCount(), 0, 0);
		LCD.drawString("LeftMotor:" + leftMotor.getTachoCount(), 0, 1);

		LCD.drawString("TouchSensor:" + touchSensor.touchSensorIsPressed(), 0, 3);
		LCD.drawString("SonarSensor:" + sonarSensor.getDistance(), 0, 4);
		LCD.drawString("ColorSensor:" + colorSensor.getBrightness(), 0, 5);
		LCD.drawString("GyroSensor:" + gyroSensor.getGyroValue(), 0, 6);
	}

	public static void main (String[] args){
		DeviceTest main = new DeviceTest();
		while(Button.ESCAPE.isUp()){
			main.showDeviceValue();
			Delay.msDelay(500);
		}
	}
}
