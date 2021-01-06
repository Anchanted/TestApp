package cn.edu.xjtlu.testapp.graphic;

public class AnimationTransition {
    public float translateX;
    public float translateY;
    public float scaleX;
    public float scaleY;

    public AnimationTransition() {
        this(0, 0, 0, 0);
    }

    public AnimationTransition(float translateX, float translateY, float scaleX, float scaleY) {
        this.translateX = translateX;
        this.translateY = translateY;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
    }
}
