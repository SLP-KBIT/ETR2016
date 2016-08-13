package jp.etrobo.ev3.framework.calculator;

public class Calculator {
    private TailPID tailPID;
    private TurnPID turnPID;

    public Calculator() {
        this.tailPID = new TailPID(-1, -1, -1);
        this.turnPID = new TurnPID(0.06F, 0.004F, 0.03F);
    }
    
    /**
     * 現在の光度と目的の光度から，走行体のターン値を計算します
     * @param {float} targetColorValue
     * @param {float} currentColorValue
     * @return {float} turn
     */
    public float turnPIDcalc(float targetColorValue, float currentColorValue) {
        return this.turnPID.calculate(targetColorValue, targetColorValue);
    }
    
    /**
     * しっぽを目的角度にするための次のステップのパワー値を返却します
     * @param {float} targetTailAngle
     * @param {float} currentTailAngle
     * @return {float} nextPower
     */
    public float tailPIDcalc(float targetTailAngle, float currentTailAngle) {
        return this.tailPID.calculate(targetTailAngle, currentTailAngle);
    }
}
