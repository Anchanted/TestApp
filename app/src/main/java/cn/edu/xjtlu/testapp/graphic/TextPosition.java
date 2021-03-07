package cn.edu.xjtlu.testapp.graphic;

import android.graphics.Paint;

public enum TextPosition {
    BOTTOM(0, Paint.Align.CENTER), LEFT(1, Paint.Align.LEFT), RIGHT(2, Paint.Align.RIGHT), TOP(3, Paint.Align.CENTER);
    public final int nativeInt;
    public final Paint.Align align;
    TextPosition(int i, Paint.Align alignment) {
        nativeInt = i;
        align = alignment;
    }
}
