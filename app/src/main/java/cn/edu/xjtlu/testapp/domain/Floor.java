package cn.edu.xjtlu.testapp.domain;

import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.List;

import cn.edu.xjtlu.testapp.graphic.GraphicPlace;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Floor implements Serializable {
    private Integer id;

    private String name;

    private Integer levelIndex;

    private Integer levelOffset;

    private PlaceFloor[] buildingList;

    private Integer direction;

    private Float ratio;

    private Float[][][] refCoords;

    private String imgUrl;

    private Boolean hasGate;

    private Boolean hasOccupation;

    public float degree = 0;

    public float scale = 1;

    public final PointF origin = new PointF(0, 0);

    public PointF[] envelope;

    public final Matrix matrix = new Matrix();

    public List<GraphicPlace> placeList;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getLevelIndex() {
        return levelIndex;
    }

    public void setLevelIndex(Integer levelIndex) {
        this.levelIndex = levelIndex;
    }

    public Integer getLevelOffset() {
        return levelOffset;
    }

    public void setLevelOffset(Integer levelOffset) {
        this.levelOffset = levelOffset;
    }

    public PlaceFloor[] getBuildingList() {
        return buildingList;
    }

    public void setBuildingList(PlaceFloor[] buildingList) {
        this.buildingList = buildingList;
    }

    public Integer getDirection() {
        return direction;
    }

    public void setDirection(Integer direction) {
        this.direction = direction;
    }

    public Float getRatio() {
        return ratio;
    }

    public void setRatio(Float ratio) {
        this.ratio = ratio;
    }

    public Float[][][] getRefCoords() {
        return refCoords;
    }

    public void setRefCoords(Float[][][] refCoords) {
        this.refCoords = refCoords;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public Boolean getHasGate() {
        return hasGate;
    }

    public void setHasGate(Boolean hasGate) {
        this.hasGate = hasGate;
    }

    public Boolean getHasOccupation() {
        return hasOccupation;
    }

    public void setHasOccupation(Boolean hasOccupation) {
        this.hasOccupation = hasOccupation;
    }

    @Override
    public String toString() {
        return "Floor{" +
                "id=" + id +
                ", name=" + name +
                ", levelIndex=" + levelIndex +
                ", levelOffset=" + levelOffset +
                ", buildingList=" + buildingList +
                ", direction=" + direction +
                ", ratio=" + ratio +
                ", refCoords=" + refCoords +
                ", imgUrl=" + imgUrl +
                ", hasGate=" + hasGate +
                ", hasOccupation=" + hasOccupation +
                '}';
    }
}