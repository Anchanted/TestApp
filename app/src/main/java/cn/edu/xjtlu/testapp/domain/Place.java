package cn.edu.xjtlu.testapp.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Place implements Serializable {
    private Integer id;

    private String placeType;

    private String code;

    private Object name;

    private Object shortName;

    private Object type;

    private Object description;

    private Object department;

    private String iconType;

    private Boolean displayIconName;

    private List<String> imgUrl;

    private Object contact;

    private String zone;

    private Integer baseFloorId;

    private Object extraInfo;

    private Object address;

    private Integer buildingId;

    private String buildingCode;

    private Object buildingName;

    private String buildingZone;

    private List<PlaceFloor> floorInfo;

    public Floor[] floorList;

    public int currentFloorIndex = -1;

    public Place() {}

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

    public Object getName() {
        return name;
    }

    public void setName(Object name) {
        this.name = name;
    }

    public Object getShortName() {
        return shortName;
    }

    public void setShortName(Object shortName) {
        this.shortName = shortName;
    }

    public Object getType() {
        return type;
    }

    public void setType(Object type) {
        this.type = type;
    }

    public Object getDescription() {
        return description;
    }

    public void setDescription(Object description) {
        this.description = description;
    }

    public Object getDepartment() {
        return department;
    }

    public void setDepartment(Object department) {
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

    public Object getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(Object buildingName) {
        this.buildingName = buildingName;
    }

    public String getBuildingZone() {
        return buildingZone;
    }

    public void setBuildingZone(String buildingZone) {
        this.buildingZone = buildingZone;
    }

    public List<PlaceFloor> getFloorInfo() {
        return floorInfo;
    }

    public void setFloorInfo(List<PlaceFloor> floorInfo) {
        this.floorInfo = floorInfo;
    }

    @Override
    public String toString() {
        return "Place{" +
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
                ", address=" + address +
                ", buildingId=" + buildingId +
                ", buildingCode=" + buildingCode +
                ", buildingName=" + buildingName +
                ", buildingZone=" + buildingZone +
                ", floorInfo=" + floorInfo +
                '}';
    }
}
