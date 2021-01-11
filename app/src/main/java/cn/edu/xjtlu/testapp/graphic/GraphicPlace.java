package cn.edu.xjtlu.testapp.graphic;

import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Point;
import android.text.StaticLayout;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import cn.edu.xjtlu.testapp.domain.PlainPlace;

public class GraphicPlace implements Comparable<GraphicPlace>{
    public enum TextPosition {
        BOTTOM(0, Paint.Align.CENTER), LEFT(1, Paint.Align.LEFT), RIGHT(2, Paint.Align.RIGHT), TOP(3, Paint.Align.CENTER);
        public final int nativeInt;
        public final Paint.Align align;
        TextPosition(int i, Paint.Align alignment) {
            nativeInt = i;
            align = alignment;
        }
    }
    public static int degree;

    public static int imgWidth;

    public static int imgHeight;

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

    @NonNull
    public final ValueAnimator displayAnimator;

    public int displayAlpha;

    public boolean displayAnimationForward;

    public final int textWidth;

    public final int textHeight;

    public final float halfTextHeight;

    private GraphicPlace() {
        this.id = -1;
        this.name = null;
        this.iconType = null;
        this.iconImg = null;
        this.iconLevel = -1;
        this.displayName = null;
        this.location = null;
        this.areaCoords = null;
        this.displayAnimator = null;
        this.textWidth = 0;
        this.textHeight = 0;
        this.halfTextHeight = 0;
    }

    public GraphicPlace(PlainPlace pp, Bitmap img, StaticLayout layout) {
        this.id = pp.getId();
        this.name = pp.getShortName();
        this.iconType = pp.getIconType();
        this.iconImg = img;
        this.iconLevel = pp.getIconLevel();
        this.displayName = pp.getDisplayIconName();
        this.staticLayout = layout;
        this.displayAnimator = ValueAnimator.ofInt(0, 255);
        this.displayAnimator.setDuration(200);
        this.displayAnimator.addUpdateListener(animation -> displayAlpha = ((Number) animation.getAnimatedValue()).intValue());
        this.textHeight = layout.getHeight();
        this.halfTextHeight = textHeight / 2f;
        int width = 0;
        for (int i = 0; i < layout.getLineCount(); i++) {
            width = Math.max(width, (int) Math.ceil(layout.getLineWidth(i)));
        }
        this.textWidth = width;

        this.location = convertPoint((int) pp.getLocation().getX(), (int) pp.getLocation().getY(), false);
        if (pp.getAreaCoords() == null) {
            this.areaCoords = null;
        } else {
            this.areaCoords = new ArrayList<>();
            for (List<cn.edu.xjtlu.testapp.domain.Point> pointList : pp.getAreaCoords()) {
                List<Point> gpointList = new ArrayList<>();
                for (cn.edu.xjtlu.testapp.domain.Point point : pointList) {
                    gpointList.add(convertPoint((int) point.getX(), (int) point.getY(), false));
                }
                areaCoords.add(gpointList);
            }
        }
    }

    public static Point convertPoint(int oldX, int oldY, boolean reverse) {
        int x, y;
        if (degree == 90) {
            x = reverse ? oldY : imgHeight - oldY;
            y = reverse ? imgHeight - oldX : oldX;
        } else if (degree == -90) {
            x = reverse ? imgWidth - oldY : oldY;
            y = reverse ? oldX : imgWidth - oldX;
        } else {
            x = oldX;
            y = oldY;
        }
        return new Point(x, y);
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

    @Override
    public int compareTo(GraphicPlace o) {
        if (this.areaCoords != null && o.areaCoords != null) {
            return Float.compare(this.iconLevel, o.iconLevel);
        } else if (this.areaCoords != null) {
            return 1;
        } else {
            return -1;
        }
    }
}
