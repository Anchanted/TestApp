package cn.edu.xjtlu.testapp.graphic;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.StaticLayout;

import java.util.List;

import cn.edu.xjtlu.testapp.bean.PlainPlace;
import cn.edu.xjtlu.testapp.bean.Point;

public class GraphicPlace {
    public enum TextPosition {
        BOTTOM(0, Paint.Align.CENTER), LEFT(1, Paint.Align.LEFT), RIGHT(2, Paint.Align.RIGHT), TOP(3, Paint.Align.CENTER);
        public final int nativeInt;
        public final Paint.Align align;
        TextPosition(int i, Paint.Align alignment) {
            nativeInt = i;
            align = alignment;
        }
    }
    public final int id;

    public final String name;

    public final String iconType;

    public final Bitmap iconImg;

    public final float iconLevel;

    public final Boolean displayName;

    public final Point location;

    public final List<List<Point>> areaCoords;

    public StaticLayout staticLayout;

    public TextPosition textPosition = TextPosition.BOTTOM;

    public final int textWidth;

    public final int textHeight;

    private GraphicPlace() {
        this.id = -1;
        this.name = null;
        this.iconType = null;
        this.iconImg = null;
        this.iconLevel = -1;
        this.displayName = null;
        this.location = null;
        this.areaCoords = null;
        this.textWidth = 0;
        this.textHeight = 0;
    }

    public GraphicPlace(PlainPlace pp, Bitmap img, StaticLayout layout) {
        this.id = pp.getId();
        this.name = pp.getShortName();
        this.iconType = pp.getIconType();
        this.iconImg = img;
        this.iconLevel = pp.getIconLevel();
        this.displayName = pp.getDisplayIconName();
        this.location = pp.getLocation();
        this.areaCoords = pp.getAreaCoords();
        this.staticLayout = layout;
        this.textHeight = layout.getHeight();
        int width = 0;
        for (int i = 0; i < layout.getLineCount(); i++) {
            width = Math.max(width, (int) Math.ceil(layout.getLineWidth(i)));
        }
        this.textWidth = width;
    }

    @Override
    public String toString() {
        return "GraphicPlace{" +
                "id=" + id +
                ", name=" + name +
                ", iconType=" + iconType +
                ", iconImg=" + iconImg +
                ", iconLevel=" + iconLevel +
                ", displayName=" + displayName +
                ", location=" + location +
                ", areaCoords=" + areaCoords +
                '}';
    }
}
