package cn.edu.xjtlu.testapp.graphic;

import androidx.annotation.NonNull;

public class BBox extends BShape {
    public BBox(float x, float y, float width, float height) {
        this.minX = x;
        this.minY = y;
        this.maxX = x + width;
        this.maxY = y + height;
    }

    @Override
    public String toString() {
        return "BBox{" +
                "(" + minX + ", " + minY + ")" +
                ", (" + maxX + ", " + maxY + ")" +
                ", width=" + (maxX - minX) +
                ", height=" + (maxY - minY) +
                '}';
    }
}
