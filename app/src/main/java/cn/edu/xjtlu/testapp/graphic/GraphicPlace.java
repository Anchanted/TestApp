package cn.edu.xjtlu.testapp.graphic;

import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Region;
import android.text.StaticLayout;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import cn.edu.xjtlu.testapp.domain.PlainPlace;

public class GraphicPlace implements Comparable<GraphicPlace>{
    public static int degree;

    public static int imgWidth;

    public static int imgHeight;

    public final int id;

    public final String placeType;

    public final String name;

    public final String iconType;

    public final Bitmap iconImg;

    public final float iconLevel;

    public final Boolean displayName;

    public final Point location;

    public final Point[][] areaCoords;

    public final Matrix matrix = new Matrix();

    public StaticLayout staticLayout;

    public TextPosition textPosition = TextPosition.BOTTOM;

    public ValueAnimator displayAnimator;

    public int displayAlpha;

    public boolean displayAnimationForward;

    public int iconColor;

    public final Region clipRegion = new Region();

    public int textWidth;

    public int textHeight;

    public float halfTextHeight;

    public GraphicPlace(PlainPlace pp, Bitmap img, int color, StaticLayout layout) {
        this.id = pp.getId();
        this.placeType = pp.getPlaceType();
        this.name = pp.getShortName();
        this.iconType = pp.getIconType();
        this.iconImg = img;
        this.iconLevel = pp.getIconLevel();
        this.displayName = pp.getDisplayIconName();
        this.staticLayout = layout;
        this.displayAnimator = ValueAnimator.ofInt(0, 255);
        this.displayAnimator.setDuration(200);
        this.displayAnimator.addUpdateListener(animation -> displayAlpha = ((Number) animation.getAnimatedValue()).intValue());
        this.iconColor = color;
        this.textHeight = layout.getHeight();
        this.halfTextHeight = textHeight / 2f;
        int width = 0;
        for (int i = 0; i < layout.getLineCount(); i++) {
            width = Math.max(width, (int) Math.ceil(layout.getLineWidth(i)));
        }
        this.textWidth = width;

        this.location = new Point((int) pp.getLocation().getX(), (int) pp.getLocation().getY());
        if (pp.getAreaCoords() == null) {
            this.areaCoords = null;
        } else {
            this.areaCoords = new Point[pp.getAreaCoords().size()][];
            for (int i = 0; i < pp.getAreaCoords().size(); i++) {
                List<cn.edu.xjtlu.testapp.domain.Point> pointList = pp.getAreaCoords().get(i);
                areaCoords[i] = new Point[pointList.size()];
                for (int j = 0; j < pointList.size(); j++) {
                    cn.edu.xjtlu.testapp.domain.Point point = pointList.get(j);
                    areaCoords[i][j] = new Point((int) point.getX(), (int) point.getY());
                }
            }
        }
    }

    public GraphicPlace(String name, Point location) {
        this.id = 0;
        this.placeType = "mark";
        this.name = name;
        this.iconType = "pin";
        this.iconImg = null;
        this.iconLevel = 1;
        this.displayName = false;
        this.location = location;
        this.areaCoords = null;
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
