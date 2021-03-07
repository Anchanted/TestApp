package cn.edu.xjtlu.testapp.graphic;

import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Region;
import android.text.StaticLayout;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import cn.edu.xjtlu.testapp.domain.PlainPlace;

public class Marker {
    public final int id;

    public final String placeType;

    public String name;

    public final String iconType;

    public final Bitmap iconImg;

    public final float iconLevel;

    public final Boolean displayName;

    public final Point location;

    public final Point[][] areaCoords;

    public StaticLayout staticLayout;

    public int displayAlpha = 1;

    public final Region clipRegion;

    public int textWidth;

    public int textHeight;

    public float scaleFactor;

    public Marker(GraphicPlace p, Bitmap img, StaticLayout layout, float scaleFactor) {
        this.id = p.id;
        this.placeType = p.placeType;
        this.name = p.name;
        this.iconType = p.iconType;
        this.iconImg = img;
        this.iconLevel = p.iconLevel;
        this.displayName = p.displayName;
        this.location = p.location;
        this.areaCoords = p.areaCoords;
        this.staticLayout = layout;
        this.clipRegion = p.clipRegion;
        this.scaleFactor = scaleFactor;
        this.textHeight = layout.getHeight();
        int width = 0;
        for (int i = 0; i < layout.getLineCount(); i++) {
            width = Math.max(width, (int) Math.ceil(layout.getLineWidth(i)));
        }
        this.textWidth = width;
    }

    public void setStaticLayout(StaticLayout layout) {
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
        return "Marker{" +
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
