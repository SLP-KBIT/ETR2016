package jp.etrobo.ev3.sample;

import lejos.hardware.Button;


public class DriveMode implements Mode {

	private EV3way body;

	DriveMode(EV3way b){
		body = b;
	}

	public void strategyRun(){
		Button.LEDPattern(1);

		body.controlDrive();
		body.controlTail(EV3way.TAIL_ANGLE_DRIVE);
	}
}
