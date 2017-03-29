package biz.lightwell.sentiremobile.aaa;

import com.orm.SugarRecord;

/**
 * Created by stew on 3/23/2017.
 * Purpose:
 * Notes:
 */

public class myInfoDataObj extends SugarRecord {
    // organized as a key / value pairings to manage multiple devices/sensors being connected

    private String infoDataCat; // ANDROID | ARDUINO BOARD | PERSON
    private String infoDataKey;  // AndroidID | ArdunioID | PersonEmail | PersonName
    private String infoDataValue; // value....

    public myInfoDataObj() {}

    public myInfoDataObj(String infoDataCat, String infoDataKey, String infoDataValue) {
        // TODO: 3/23/2017 add validations...
        this.infoDataCat   = infoDataCat;
        this.infoDataKey    = infoDataKey;
        this.infoDataValue  = infoDataValue;
    }

    public String getInfoDataCat() {
        return infoDataCat;
    }

    public void setInfoDataCat(String infoDataCat) {
        this.infoDataCat = infoDataCat;
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
