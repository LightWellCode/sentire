package biz.lightwell.sentiremobile.sensorMgmt;

import java.security.Timestamp;
import java.util.Date;

/**
 * Created by stew on 3/23/2017.
 * Purpose:
 * Notes:
 */

public class sensorDataObj {

    // TODO: 3/27/2017 pick a data storage approach for the different data types 
    private String sensorDataKey;       // MQ2 | TEMP | LAT | LONG | ALTITUDE
    private String sensorDataValue;     // hmmm....some float, integer and stings -set all to string?
    private String sensorDataType;      // store the data value type here for conversion...?
    private Date sensorDateTime;        //
    private String sensorDeviceID;      // android could be connected to multiple sensor devices

    public sensorDataObj() {}

    public sensorDataObj(String sensorDataKey, String sensorDataValue, Date sensorDateTime, String sensorDeviceID) {
        // TODO: 3/27/2017 add data validations...
        this.sensorDataKey = sensorDataKey;
        this.sensorDataValue = sensorDataValue;
        this.sensorDateTime = sensorDateTime;
        this.sensorDeviceID = sensorDeviceID;
    }

    public String getSensorDataKey() {
        return sensorDataKey;
    }

    public void setSensorDataKey(String sensorDataKey) {
        this.sensorDataKey = sensorDataKey;
    }

    public String getSensorDataValue() {
        return sensorDataValue;
    }

    public void setSensorDataValue(String sensorDataValue) {
        this.sensorDataValue = sensorDataValue;
    }

    public Date getSensorDateTime() {
        return sensorDateTime;
    }

    public void setSensorDateTime(Date sensorDateTime) {
        this.sensorDateTime = sensorDateTime;
    }

    public String getSensorDeviceID() {
        return sensorDeviceID;
    }

    public void setSensorDeviceID(String sensorDeviceID) {
        this.sensorDeviceID = sensorDeviceID;
    }


}


