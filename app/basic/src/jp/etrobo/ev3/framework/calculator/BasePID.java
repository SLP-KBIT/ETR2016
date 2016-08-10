package jp.etrobo.ev3.framework.calculator;

public abstract class BasePID {
    protected float[] diff;
    protected float integral;

    protected float Kp = 0;
    protected float Ki = 0;
    protected float Kd = 0;
    
    public BasePID(float p, float i, float d) {
        this.Kp = p;
        this.Ki = i;
        this.Kd = d;
        this.diff = new float[2];
        this.integral = 0;
    }
    
    public abstract float calculate(float target, float current);
}
