package cn.edu.xjtlu.testapp.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlainPlace {
    private Integer id;

    private String placeType;

    private String code;

    private String name;

    private String shortName;

    private Object type;

    private String description;

    private String department;

    private String iconType;

    private Boolean displayIconName;

    private List<String> imgUrl;

    private Object contact;

    private String zone;

    private Integer baseFloorId;

    private Object extraInfo;

    private Integer level;

    private Float iconLevel;

    private Integer displayLevel;

    private Point location;

    private List<List<List<Point>>> areaCoords;

    private Object address;

    private Integer buildingId;

    private String buildingCode;

    private String buildingName;

    private String buildingZone;

    private Integer floorId;

    private String floorName;

    private Integer floorLevelIndex;

    public PlainPlace() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPlaceType() {
        return placeType;
    }

    public void setPlaceType(String placeType) {
        this.placeType = placeType;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public Object getType() {
        return type;
    }

    public void setType(Object type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getIconType() {
        return iconType;
    }

    public void setIconType(String iconType) {
        this.iconType = iconType;
    }

    public Boolean getDisplayIconName() {
        return displayIconName;
    }

    public void setDisplayIconName(Boolean displayIconName) {
        this.displayIconName = displayIconName;
    }

    public List<String> getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(List<String> imgUrl) {
        this.imgUrl = imgUrl;
    }

    public Object getContact() {
        return contact;
    }

    public void setContact(Object contact) {
        this.contact = contact;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public Integer getBaseFloorId() {
        return baseFloorId;
    }

    public void setBaseFloorId(Integer baseFloorId) {
        this.baseFloorId = baseFloorId;
    }

    public Object getExtraInfo() {
        return extraInfo;
    }

    public void setExtraInfo(Object extraInfo) {
        this.extraInfo = extraInfo;
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

    public List<List<List<Point>>> getAreaCoords() {
        return areaCoords;
    }

    public void setAreaCoords(List<List<List<Point>>> areaCoords) {
        this.areaCoords = areaCoords;
    }

    public Object getAddress() {
        return address;
    }

    public void setAddress(Object address) {
        this.address = address;
    }

    public Integer getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(Integer buildingId) {
        this.buildingId = buildingId;
    }

    public String getBuildingCode() {
        return buildingCode;
    }

    public void setBuildingCode(String buildingCode) {
        this.buildingCode = buildingCode;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    public String getBuildingZone() {
        return buildingZone;
    }

    public void setBuildingZone(String buildingZone) {
        this.buildingZone = buildingZone;
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

    @Override
    public String toString() {
        return "PlainPlace{" +
                "id=" + id +
                ", placeType=" + placeType +
                ", code=" + code +
                ", name=" + name +
                ", shortName=" + shortName +
                ", type=" + type +
                ", description=" + description +
                ", department=" + department +
                ", iconType=" + iconType +
                ", displayIconName=" + displayIconName +
                ", imgUrl=" + imgUrl +
                ", contact=" + contact +
                ", zone=" + zone +
                ", baseFloorId=" + baseFloorId +
                ", extraInfo=" + extraInfo +
                ", level=" + level +
                ", iconLevel=" + iconLevel +
                ", displayLevel=" + displayLevel +
                ", location=" + location +
                ", areaCoords=" + areaCoords +
                ", address=" + address +
                ", buildingId=" + buildingId +
                ", buildingCode=" + buildingCode +
                ", buildingName=" + buildingName +
                ", buildingZone=" + buildingZone +
                ", floorId=" + floorId +
                ", floorName=" + floorName +
                ", floorLevelIndex=" + floorLevelIndex +
                '}';
    }
}