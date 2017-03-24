package biz.lightwell.sentiremobile.aaa;

/**
 * Created by stew on 3/23/2017.
 * Purpose:
 * Notes:
 */

public class myInfoDataObj {
    // organized as a key / value pairings to manage multiple devices/sensors being connected

    private String infoDataType; // ANDROID | ARDUINO BOARD | PERSON
    private String infoDataKey;  // AndroidID | ArdunioID | PersonEmail | PersonName
    private String infoDataValue; // value....

    public myInfoDataObj() {}

    public myInfoDataObj(String infoDataType, String infoDataKey, String infoDataValue) {
        // TODO: 3/23/2017 add validations...
        this.infoDataType   = infoDataType;
        this.infoDataKey    = infoDataKey;
        this.infoDataValue  = infoDataValue;
    }

    public String getInfoDataType() {
        return infoDataType;
    }

    public void setInfoDataType(String infoDataType) {
        this.infoDataType = infoDataType;
    }

    public String getInfoDataKey() {
        return infoDataKey;
    }

    public void setInfoDataKey(String infoDataKey) {
        this.infoDataKey = infoDataKey;
    }

    public String getInfoDataValue() {
        return infoDataValue;
    }

    public void setInfoDataValue(String infoDataValue) {
        this.infoDataValue = infoDataValue;
    }



}
