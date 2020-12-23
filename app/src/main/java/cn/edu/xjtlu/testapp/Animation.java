package cn.edu.xjtlu.testapp;

public class Animation {
    public final int duration;
    public final long startTime;
    public final long endTime;
    public final Object startValue;
    public final Object endValue;
    public long currentTime;

    public Animation(int duration, Object startValue, Object endValue) {
        this.duration = duration;
        this.startTime = System.currentTimeMillis();
        this.endTime = this.startTime + this.duration;
        this.startValue = startValue;
        this.endValue = endValue;
        this.currentTime = this.startTime;
    }
}
