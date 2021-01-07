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

    public static AnimationTransition minus(AnimationTransition o1, AnimationTransition o2) {
        if (o1 == null) return null;
        else if (o2 == null) return o1;
        return new AnimationTransition(o1.translateX - o2.translateX, o1.translateY - o2.translateY, o1.scaleX - o2.scaleX, o1.scaleY - o2.scaleY);
    }

    @Override
    public String toString() {
        return "AnimationTransition{" +
                "translateX=" + translateX +
                ", translateY=" + translateY +
                ", scaleX=" + scaleX +
                ", scaleY=" + scaleY +
                '}';
    }
}
