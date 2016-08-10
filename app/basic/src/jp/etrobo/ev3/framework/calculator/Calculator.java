package jp.etrobo.ev3.framework.calculator;

public class Calculator {
    private TailPID tailPID;
    private TurnPID turnPID;

    public Calculator() {
        this.tailPID = new TailPID(-1, -1, -1);
        this.turnPID = new TurnPID(0.06F, 0.004F, 0.03F);
    }
    
    public float turnPIDcalc(float targetColorValue, float currentColorValue) {
        return this.turnPID.calculate(targetColorValue, targetColorValue);
    }

    public float tailPIDcalc(float targetTailAngle, float currentTailAngle) {
        return this.tailPID.calculate(targetTailAngle, currentTailAngle);
    }
}
