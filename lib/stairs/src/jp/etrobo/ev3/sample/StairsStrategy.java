package jp.etrobo.ev3.sample;

import jp.etrobo.ev3.balancer.Balancer;
import lejos.hardware.Battery;

/**
 * @classdoc StairsStrategy
 * @author MaxMellon
 */
public class StairsStrategy extends BaseStrategy {
    private static final float GYRO_OFFSET = 0.0F;
    private float forward = 80.0F;

    /**
     * 階段を攻略する戦略のインスタンスを作成します
     * @param forward {float} 前進速度
     */
    public StairsStrategy(float forward) {
        this.forward = forward;
    }

    /**
     * 戦略を実行します
     * @param robot {EV3way} モータやセンサーを持っているインスタンス
     * @return void
     */
    @Override
    public void executeStrategy(EV3way robot) {
        float currentGyro = robot.getGyroValue();
        int leftTachoCount = robot.motorPortL.getTachoCount();
        int rightTachoCount = robot.motorPortR.getTachoCount();
        int battery = Battery.getVoltageMilliVolt();
        float turn = robot.calcTurn(leftTachoCount, rightTachoCount);
        Balancer.control(this.forward, turn, currentGyro, GYRO_OFFSET, leftTachoCount, rightTachoCount, battery);
        robot.motorPortL.controlMotor(Balancer.getPwmL(), 1);
        robot.motorPortR.controlMotor(Balancer.getPwmR(), 1);
        robot.controlTail(EV3way.TAIL_ANGLE_DRIVE);
    }
}
