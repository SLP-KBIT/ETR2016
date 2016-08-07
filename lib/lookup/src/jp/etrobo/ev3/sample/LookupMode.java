package jp.etrobo.ev3.sample;

import jp.etrobo.ev3.balancer.Balancer;
import lejos.hardware.Button;
import lejos.utility.Delay;

public class LookupMode implements Mode {

	private EV3way body;
	private int stateNum = 0;
	private int timeCounter = 0;

    public static final int TAIL_ANGLE = 85;    // 尻尾走行時の角度[度]

	LookupMode(EV3way body){
		this.body = body;
	}

	public void strategyRun(){
		float turn;
		switch(stateNum){
			case 0:  // 尻尾下ろし
				body.controlTail(TAIL_ANGLE);
				turn = body.getPIDTurnValue();
				body.setBalancerParm(0.0F, turn);
				body.motorPortL.controlMotor(Balancer.getPwmL(), 1); // 左モータPWM出力セット
				body.motorPortR.controlMotor(Balancer.getPwmR(), 1); // 右モータPWM出力セット
				if(++timeCounter > 3000/4){ // 約3000ms
					stateNum = 1;
					timeCounter = 0;
				}
				break;
			case 1:  // 倒れる
				body.controlTail(TAIL_ANGLE);
				body.setBalancerParm(0.0F, 0.0F, -30.0F);
				body.motorPortL.controlMotor(Balancer.getPwmL(), 1); // 左モータPWM出力セット
				body.motorPortR.controlMotor(Balancer.getPwmR(), 1); // 右モータPWM出力セット
				if(++timeCounter > 200/4){ // 約200ms
					Delay.msDelay(1000); // 倒れ切るまで停止
					stateNum = 2;
					timeCounter = 0;
				}
				break;
			case 2:
				Button.LEDPattern(1);
				body.controlTail(TAIL_ANGLE);
				body.motorPortL.controlMotor(0, 1); // 左モータPWM出力セット
		        body.motorPortR.controlMotor(0, 1); // 右モータPWM出力セット
				break;
		}
	}
}
