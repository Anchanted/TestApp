package cn.edu.xjtlu.testapp.graphic;

public class AnimationTransform {
    public float translateX;
    public float translateY;
    public float scaleX;
    public float scaleY;

    public AnimationTransform() {
        this(0, 0, 1, 1);
    }

    public AnimationTransform(float translateX, float translateY, float scaleX, float scaleY) {
        this.translateX = translateX;
        this.translateY = translateY;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
    }

    public static AnimationTransform minus(AnimationTransform o1, AnimationTransform o2) {
        if (o1 == null) return null;
        else if (o2 == null) return o1;
        return new AnimationTransform(o1.translateX - o2.translateX, o1.translateY - o2.translateY, o1.scaleX - o2.scaleX, o1.scaleY - o2.scaleY);
    }

    @Override
    public String toString() {
        return "AnimationTransform{" +
                "translateX=" + translateX +
                ", translateY=" + translateY +
                ", scaleX=" + scaleX +
                ", scaleY=" + scaleY +
                '}';
    }
}
