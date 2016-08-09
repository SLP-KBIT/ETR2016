package jp.etrobo.ev3.sample;

import jp.etrobo.ev3.balancer.Balancer;
import lejos.hardware.Button;

public class LookupMode implements Mode {

	private EV3way body;
	private int stateNum = 0;
	private int timeCounter = 0;
	private boolean firstFlag = true;
	private int firstAngle;

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
			case 1:  // 倒れる
				body.controlTailSupport(TAIL_ANGLE);
				body.setBalancerParm(0.0F, 0.0F, -30.0F);
				body.motorPortL.controlMotor(Balancer.getPwmL(), 1); // 左モータPWM出力セット
				body.motorPortR.controlMotor(Balancer.getPwmR(), 1); // 右モータPWM出力セット
				if(++timeCounter > 200/4){ // 約200ms
					stateNum = 2;
					timeCounter = 0;
				}
				break;
			case 2:  // 角度キープ
				Button.LEDPattern(1);
				body.controlTailSupport(TAIL_ANGLE);
				body.setBalancerParm(0.0F, 0.0F);
				body.motorPortL.controlMotor(0, 1); // 左モータPWM出力セット
				body.motorPortR.controlMotor(0, 1); // 右モータPWM出力セット
				if(++timeCounter > 2000/4){ // 約2000ms
					stateNum = 3;
					timeCounter = 0;
				}
				break;
			case 3:  // さらに倒れる
				Button.LEDPattern(2);
				if (fallBackward(TAIL_ANGLE - 30, 7000)){
					stateNum = 4;
				}
				break;
			case 4:  // キープ
				Button.LEDPattern(3);
				body.controlTailSupport(TAIL_ANGLE -30);
				body.motorPortL.controlMotor(0, 1); // 左モータPWM出力セット
		        body.motorPortR.controlMotor(0, 1); // 右モータPWM出力セット
				break;
		}
	}

	/*
	 * 指定した角度まで、指定された時間をかけて調整
	 * @return 目標角度までの調整完了
	 */
	private boolean fallBackward(int angle, int msec) {
		if(firstFlag) {
			firstAngle = body.motorPortT.getTachoCount();
			firstFlag = false;
		}

		double progress = timeCounter /(msec/4.0);
		int diffAngle = angle - firstAngle;
		int currentAngle = (int) (firstAngle + (diffAngle * progress));

		body.controlTailSupport(currentAngle);
		body.motorPortL.controlMotor(0, 1); // 左モータPWM出力セット
        body.motorPortR.controlMotor(0, 1); // 右モータPWM出力セット
		if(++timeCounter > msec/4){
			timeCounter = 0;
			firstFlag = true;
			return true;
		}
		return false;
	}

}
