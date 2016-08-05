package jp.etrobo.ev3.sample;

import jp.etrobo.ev3.balancer.Balancer;

public class LookupMode implements Mode {

	private EV3way body;
	private int stateNum;
	private int timeCounter;
	private final int tailStateAngle = 85;

	LookupMode(EV3way body){
		this.body = body;
		stateNum = 0;
		timeCounter = 0;
	}

	public void strategyRun(){
		float turn;
		switch(stateNum){
			case 0:
				body.controlTail(tailStateAngle);
				turn = body.getPIDTurnValue();
				body.setBalancerParm(0.0F, turn);
		        body.motorPortL.controlMotor(Balancer.getPwmL(), 1); // 左モータPWM出力セット
		        body.motorPortR.controlMotor(Balancer.getPwmR(), 1); // 右モータPWM出力セット

				if(++timeCounter < 400/4){ // 約400ms
					stateNum = 1;
					timeCounter = 0;
				}
				break;
			case 1:
				body.controlTail(tailStateAngle);
				body.setBalancerParm(0.0F, 0.0F);
		        body.motorPortL.controlMotor(Balancer.getPwmL(), 1); // 左モータPWM出力セット
		        body.motorPortR.controlMotor(Balancer.getPwmR(), 1); // 右モータPWM出力セット
				break;
		}


	}
}
