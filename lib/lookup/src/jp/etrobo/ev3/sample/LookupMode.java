package jp.etrobo.ev3.sample;

import jp.etrobo.ev3.balancer.Balancer;
import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;

public class LookupMode implements Mode {

    private EV3way body;
    private int stateNum = 0;
    private int timeCounter = 0;
    private boolean firstFlag = true;
    private int firstAngle;

    public static final int FORWARD_SPEED = 20;
    public static final int DIFF_SPEED = 5;

    public static final int TAIL_ANGLE = 85;    // 尻尾走行時の角度[度]
    public static final int DAWN_ANGLE = 63;    // 倒れた状態の角度[度]

    public static final int DOWN_TIME = 7000;   // 倒れるのにかける時間[ms]
    public static final int UP_TIME = 5000;   // 起き上がるのにかける時間[ms]
    public static final int STRAIGHT_DISTANCE = 600; // 前進するときに進む距離


    LookupMode(EV3way body) {
        this.body = body;
    }

    public void strategyRun() {
        float turn;
        switch (stateNum) {
            case 0:  // 尻尾下ろし
                body.controlTailSlow(TAIL_ANGLE);
                turn = body.getPIDTurnValue();
                body.setBalancerParm(0.0F, turn);
                body.motorPortL.controlMotor(Balancer.getPwmL(), 1); // 左モータPWM出力セット
                body.motorPortR.controlMotor(Balancer.getPwmR(), 1); // 右モータPWM出力セット
                if (++timeCounter > 3000 / 4) { // 約3000ms
                    stateNum++;
                    timeCounter = 0;
                }
                break;
            case 1:
                body.controlTailSlow(TAIL_ANGLE);
                turn = body.getPIDTurnValue();
                body.setBalancerParm(0.0F, turn);
                body.motorPortL.controlMotor(Balancer.getPwmL(), 1); // 左モータPWM出力セット
                body.motorPortR.controlMotor(Balancer.getPwmR(), 1); // 右モータPWM出力セット
                if(body.getGyroValue() < 0) {
                    stateNum++;
                }
                break;
            case 2:  // 倒れる
                body.controlTailPropUP(TAIL_ANGLE);
                body.setBalancerParm(0.0F, 0.0F, -30.0F);
                body.motorPortL.controlMotor(Balancer.getPwmL(), 1); // 左モータPWM出力セット
                body.motorPortR.controlMotor(Balancer.getPwmR(), 1); // 右モータPWM出力セット
                if(++timeCounter > 200/4){ // 約200ms
                    stateNum++;
                    timeCounter = 0;
                }
                break;
            case 3:  // 角度キープ
                body.controlTailPropUP(TAIL_ANGLE);
                body.setBalancerParm(0.0F, 0.0F);
                body.motorPortL.controlMotor(0, 1); // 左モータPWM出力セット
                body.motorPortR.controlMotor(0, 1); // 右モータPWM出力セット
                if(++timeCounter > 2000/4){ // 約2000ms
                    stateNum++;
                    timeCounter = 0;
                }
                break;
            case 4:  // さらに倒れる
                if (fallBackward(DAWN_ANGLE, DOWN_TIME)){
                    stateNum++;
                }
                break;
            case 5:  // 前進する
                body.controlTailPropUP(DAWN_ANGLE);
                Button.LEDPattern(2);
                if (goStraight(STRAIGHT_DISTANCE)) {
                    stateNum++;
                }
                break;
            case 6:  // 戻ってくる
                Button.LEDPattern(3);
                if (standUp(TAIL_ANGLE, UP_TIME)){
                    stateNum++;
                }
                break;
            case 7:  // キープ
                Button.LEDPattern(0);
                LCD.drawString("TailCount"+ body.motorPortT.getTachoCount(), 0, 4);
                body.controlTailPropUP(TAIL_ANGLE);
                body.motorPortL.controlMotor(0, 1); // 左モータPWM出力セット
                body.motorPortR.controlMotor(0, 1); // 右モータPWM出力セット
                break;
        }
    }

    /*
     * 指定した角度まで、指定された時間をかけて倒れる
     * @return 目標角度までの調整完了
     */
    private boolean fallBackward(int angle, int msec) {
        if (firstFlag) {
            firstAngle = body.motorPortT.getTachoCount();
            firstFlag = false;
        }

        double progress = timeCounter /(msec/4.0);
        int diffAngle = angle - firstAngle;
        int currentAngle = (int) (firstAngle + (diffAngle * progress));

        body.controlTailPropUP(currentAngle);
        if (++timeCounter > msec/4){
            timeCounter = 0;
            firstFlag = true;
            return true;
        }
        return false;
    }

    /*
     * 指定した角度まで、指定された時間をかけて起き上がる
     * @return 目標角度までの調整完了
     */
    private boolean standUp(int angle, int msec) {
        if (firstFlag) {
            firstAngle = body.motorPortT.getTachoCount();
            firstFlag = false;
        }

        LCD.drawString("TailCount"+ body.motorPortT.getTachoCount(), 0, 3);
        double progress = timeCounter / (msec/4.0);
        int diffAngle = angle - firstAngle;
        int currentAngle = (int) (firstAngle + (diffAngle * progress));

        body.controlTailGetUp(currentAngle);
        if (++timeCounter > msec/4){
            timeCounter = 0;
            firstFlag = true;
            return true;
        }
        return false;
    }

    private boolean goStraight(int distance) {
        if (firstFlag) {
            body.resetMotor();
            firstFlag = false;
        }
        if (body.motorPortL.getTachoCount() > body.motorPortR.getTachoCount()){
            body.motorPortL.controlMotor(FORWARD_SPEED, 1); // 左モータPWM出力セット
            body.motorPortR.controlMotor(FORWARD_SPEED + DIFF_SPEED, 1); // 右モータPWM出力セット
        } else {
            body.motorPortL.controlMotor(FORWARD_SPEED + DIFF_SPEED, 1); // 左モータPWM出力セット
            body.motorPortR.controlMotor(FORWARD_SPEED, 1); // 右モータPWM出力セット
        }
        if (body.motorPortL.getTachoCount() > distance){
            body.motorPortL.controlMotor(0, 1); // 左モータPWM出力セット
            body.motorPortR.controlMotor(0, 1); // 右モータPWM出力セット
            return true;
        }
        return false;
    }
}
