package jp.etrobo.ev3.sample;

import jp.etrobo.ev3.balancer.Balancer;



public class DriveMode implements Mode {
	private EV3way body;
	private float driveCallCounter = 0;
	private boolean     sonarAlert   = false;

	private static final float SONAR_ALERT_DISTANCE = 0.2F;           // 超音波センサーによる障害物検知距離[m]
	private static final int TAIL_ANGLE_DRIVE = 3;           // バランス走行時の角度[度]

	DriveMode(EV3way b){
		body = b;
	}

	public void strategyRun(){
		body.controlTail(TAIL_ANGLE_DRIVE);

        if (++driveCallCounter >= 40/4) {  // 約40msごとに障害物検知
            sonarAlert = alertObstacle();  // 障害物検知
            driveCallCounter = 0;
        }
        float forward; // 前後進命令
        float turn =  0.0F; // 旋回命令
        if (sonarAlert) {           // 障害物を検知したら停止
            forward = 0.0F;
            StrategyMode.setLookupMode();
        } else {
            forward = 20.0F;  // 前進命令
            turn = body.getPIDTurnValue();
        }
        body.setBalancerParm(forward, turn);
        body.motorPortL.controlMotor(Balancer.getPwmL(), 1); // 左モータPWM出力セット
        body.motorPortR.controlMotor(Balancer.getPwmR(), 1); // 右モータPWM出力セット
	}

    /*
     * 超音波センサーによる障害物検知
     * @return true(障害物あり)/false(障害物無し)
     */
    private boolean alertObstacle() {
        float distance = body.getSonarDistance();
        if ((distance <= SONAR_ALERT_DISTANCE) && (distance >= 0)) {
            return true;  // 障害物を検知
        }
        return false;
    }
}
