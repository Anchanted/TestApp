package cn.edu.xjtlu.testapp.graphic;

public class BCircle extends BShape {
    public float centerX;
    public float centerY;
    public float radius;

    public BCircle(float x, float y, float radius) {
        this.minX = x - radius;
        this.minY = y - radius;
        this.maxX = x + radius;
        this.maxY = y + radius;

        this.centerX = x;
        this.centerY = y;
        this.radius = radius;
    }

    @Override
    public String toString() {
        return "BCircle{" +
                "(" + centerX + ", " + centerY + ")" +
                ", radius=" + radius +
                '}';
    }
}
