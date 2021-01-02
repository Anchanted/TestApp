package cn.edu.xjtlu.testapp.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlaceFloor implements Serializable {
    private Integer placeId;

    private Integer floorId;

    private String floorName;

    private Integer floorLevelIndex;

    private Integer level;

    private Float iconLevel;

    private Integer displayLevel;

    private Point location;

    private List<List<Point>> areaCoords;

    public Integer getPlaceId() {
        return placeId;
    }

    public void setPlaceId(Integer placeId) {
        this.placeId = placeId;
    }

    public Integer getFloorId() {
        return floorId;
    }

    public void setFloorId(Integer floorId) {
        this.floorId = floorId;
    }

    public String getFloorName() {
        return floorName;
    }

    public void setFloorName(String floorName) {
        this.floorName = floorName;
    }

    public Integer getFloorLevelIndex() {
        return floorLevelIndex;
    }

    public void setFloorLevelIndex(Integer floorLevelIndex) {
        this.floorLevelIndex = floorLevelIndex;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Float getIconLevel() {
        return iconLevel;
    }

    public void setIconLevel(Float iconLevel) {
        this.iconLevel = iconLevel;
    }

    public Integer getDisplayLevel() {
        return displayLevel;
    }

    public void setDisplayLevel(Integer displayLevel) {
        this.displayLevel = displayLevel;
    }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }

    public List<List<Point>> getAreaCoords() {
        return areaCoords;
    }

    public void setAreaCoords(List<List<Point>> areaCoords) {
        this.areaCoords = areaCoords;
    }

    @Override
    public String toString() {
        return "PlaceFloor{" +
                "placeId=" + placeId +
                ", floorId=" + floorId +
                ", floorName=" + floorName +
                ", floorLevelIndex=" + floorLevelIndex +
                ", level=" + level +
                ", iconLevel=" + iconLevel +
                ", displayLevel=" + displayLevel +
                ", location=" + location +
                ", areaCoords=" + areaCoords +
                '}';
    }
}