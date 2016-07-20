package jp.etrobo.ev3.sample;



public class DriveMode implements Mode {
	private EV3way body;
	int a =0;
	long ex = 0;
	long now;

	DriveMode(EV3way b){
		body = b;
	}

	public void strategyRun(){
		body.controlDrive();
		body.controlTail(EV3way.TAIL_ANGLE_DRIVE);
	}
}
