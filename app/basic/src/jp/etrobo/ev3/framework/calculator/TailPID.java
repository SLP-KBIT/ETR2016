package jp.etrobo.ev3.framework.calculator;

public class TailPID extends BasePID{
    public TailPID(float p, float i, float d) {
        super(p, i, d);
    }

    @Override
    public float calculate(float target, float current) {
        return 0;
    }

}
