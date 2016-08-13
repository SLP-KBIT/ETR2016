package jp.etrobo.ev3.framework.calculator;

public class TurnPID extends BasePID{
    private static final float DELTA_T = 0.004F;


    public TurnPID(float d, float e, float f) {
        super(d, e, f);
    }

    @Override
    public float calculate(float target, float current) {
        this.diff[0] = this.diff[1];
        this.diff[1] = current - target;
        this.integral += (diff[1] + diff[0]) / 2.0 * DELTA_T;
        float p = this.Kp * diff[1];
        float i = this.Ki * integral;
        float d = this.Kd * (diff[1] + diff[0]) / DELTA_T;
        float result = p + i + d * 100.0F;

        if (result > 100.0F) {
            result = 100.0F;
        } else if (-100.0F > result) {
            result = -100.F;
        }

        return result;
    }
}
