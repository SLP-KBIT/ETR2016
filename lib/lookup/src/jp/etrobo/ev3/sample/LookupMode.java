package jp.etrobo.ev3.sample;

import jp.etrobo.ev3.balancer.Balancer;
import lejos.hardware.Button;

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
				body.controlTailSlow(TAIL_ANGLE);
				turn = body.getPIDTurnValue();
				body.setBalancerParm(0.0F, turn);
				body.motorPortL.controlMotor(Balancer.getPwmL(), 1); // 左モータPWM出力セット
				body.motorPortR.controlMotor(Balancer.getPwmR(), 1); // 右モータPWM出力セット
				if(++timeCounter > 3000/4){ // 約3000ms
					stateNum = 1;
					timeCounter = 0;
				}
				break;
			case 1:  // 尻尾下ろし
				body.controlTail(TAIL_ANGLE);
				turn = body.getPIDTurnValue();
				body.setBalancerParm(0.0F, turn);
				body.motorPortL.controlMotor(Balancer.getPwmL(), 1); // 左モータPWM出力セット
				body.motorPortR.controlMotor(Balancer.getPwmR(), 1); // 右モータPWM出力セット
				if(++timeCounter > 3000/4){ // 約3000ms
					stateNum = 2;
					timeCounter = 0;
				}
				break;
			case 2:  // 倒れる
				Button.LEDPattern(1);
				body.controlTail(TAIL_ANGLE);
				body.setBalancerParm(0.0F, 0.0F, -30.0F);
				body.motorPortL.controlMotor(Balancer.getPwmL(), 1); // 左モータPWM出力セット
				body.motorPortR.controlMotor(Balancer.getPwmR(), 1); // 右モータPWM出力セット
				if(++timeCounter > 200/4){ // 約200ms
					stateNum = 3;
					timeCounter = 0;
				}
				break;
			case 3:  // さらに倒れる
				body.controlTail(TAIL_ANGLE - 5);
				body.motorPortL.controlMotor(0, 1); // 左モータPWM出力セット
		        body.motorPortR.controlMotor(0, 1); // 右モータPWM出力セット
				if(++timeCounter > 2000/4){ // 約2000ms
					stateNum = 4;
					timeCounter = 0;
				}
				break;
			case 4:  // さらに倒れる
				body.controlTail(TAIL_ANGLE - 10);
				body.motorPortL.controlMotor(0, 1); // 左モータPWM出力セット
		        body.motorPortR.controlMotor(0, 1); // 右モータPWM出力セット
				if(++timeCounter > 2000/4){ // 約2000ms
					stateNum = 5;
					timeCounter = 0;
				}
				break;
			case 5:  // さらに倒れる
				body.controlTail(TAIL_ANGLE - 15);
				body.motorPortL.controlMotor(0, 1); // 左モータPWM出力セット
		        body.motorPortR.controlMotor(0, 1); // 右モータPWM出力セット
				if(++timeCounter > 2000/4){ // 約2000ms
					stateNum = 6;
					timeCounter = 0;
				}
				break;
			case 6:  // さらに倒れる
				body.controlTail(TAIL_ANGLE - 20);
				body.motorPortL.controlMotor(0, 1); // 左モータPWM出力セット
		        body.motorPortR.controlMotor(0, 1); // 右モータPWM出力セット
				if(++timeCounter > 2000/4){ // 約2000ms
					stateNum = 7;
					timeCounter = 0;
				}
				break;
			case 7:  // さらに倒れる
				body.controlTail(TAIL_ANGLE - 25);
				body.motorPortL.controlMotor(0, 1); // 左モータPWM出力セット
		        body.motorPortR.controlMotor(0, 1); // 右モータPWM出力セット
				if(++timeCounter > 2000/4){ // 約2000ms
					stateNum = 8;
					timeCounter = 0;
				}
				break;
			case 8:  // さらに倒れる
				Button.LEDPattern(2);
				body.controlTail(TAIL_ANGLE -25);
				body.motorPortL.controlMotor(0, 1); // 左モータPWM出力セット
		        body.motorPortR.controlMotor(0, 1); // 右モータPWM出力セット
				break;
		}
	}
}
