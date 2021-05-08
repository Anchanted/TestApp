package cn.edu.xjtlu.testapp.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Floor implements Serializable {
    private Integer id;

    private String name;

    private Integer levelIndex;

    private Integer levelOffset;

    private List<Integer> buildingId;

    private Integer direction;

    private Float ratio;

    private Object refCoords;

    private String imgUrl;

    private Boolean hasGate;

    private Boolean hasOccupation;

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

    public List<Integer> getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(List<Integer> buildingId) {
        this.buildingId = buildingId;
    }

    public Integer getDirection() {
        return direction;
    }

    public void setDirection(Integer direction) {
        this.direction = direction;
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
                ", buildingId=" + buildingId +
                ", direction=" + direction +
                ", imgUrl=" + imgUrl +
                ", hasGate=" + hasGate +
                ", hasOccupation=" + hasOccupation +
                '}';
    }
}