package biz.lightwell.sentiremobile.sensorMgmt;

import com.orm.SugarRecord;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by stew on 3/23/2017.
 * Purpose:
 * Notes:
 */

public class sensorDataObj extends SugarRecord {

    // TODO: 3/27/2017 pick a data storage approach for the different data types 
    private String sensorDataKey;       // MQ2 | TEMP | LAT | LONG | ALTITUDE
    private String sensorDataValue;     // hmmm....some float, integer and stings -set all to string?
    private String sensorDataType;      // store the data value type here for conversion...?
    private Calendar sensorDateTime;        //
    private String sensorDeviceID;      // android could be connected to multiple sensor devices

    public sensorDataObj() {}

    public sensorDataObj(String sensorDataKey, String sensorDataValue, String sensorDataType, Calendar sensorDateTime, String sensorDeviceID) {
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

    public String getSensorDataType() {
        return sensorDataType;
    }

    public void setSensorDataType(String sensorDataType) {
        this.sensorDataType = sensorDataType;
    }

    public Calendar getSensorDateTime() {
        return sensorDateTime;
    }

    public String getTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = sensorDateTime.getTime();
        return dateFormat.format(date);
    }

    public void setSensorDateTime(Calendar sensorDateTime) {
        this.sensorDateTime = sensorDateTime;
    }

    public String getSensorDeviceID() {
        return sensorDeviceID;
    }

    public void setSensorDeviceID(String sensorDeviceID) {
        this.sensorDeviceID = sensorDeviceID;
    }

    public JSONObject getJSON(){
        JSONObject object = new JSONObject();
        try {
            object.put("sensorDataKey", this.sensorDataKey);
            object.put("sensorDataValue", this.sensorDataValue);
            object.put("sensorDataType", this.sensorDataType);
            object.put("sensorDateTime", this.getTime());
            object.put("sensorDeviceID", this.sensorDeviceID);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }

}


