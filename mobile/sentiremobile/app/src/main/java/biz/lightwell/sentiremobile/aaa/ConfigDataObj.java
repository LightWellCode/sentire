package biz.lightwell.sentiremobile.aaa;

import com.orm.SugarDb;
import com.orm.SugarRecord;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by stew on 4/2/2017.
 * Purpose:
 * Notes:
 */

public class ConfigDataObj extends SugarRecord {
    private String cloudURL;
    private Calendar lastSentDateTime;


    public ConfigDataObj() {    };

    public String getCloudURL() {
        return cloudURL;
    }

    public void setCloudURL(String cloudURL) {
        this.cloudURL = cloudURL;
    }

    public Calendar getLastSentDateTime() {
        return lastSentDateTime;
    }

    public void setLastSentDateTime(Calendar lastSentDateTime) {
        this.lastSentDateTime = lastSentDateTime;
    }
    public String getTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = lastSentDateTime.getTime();
        return dateFormat.format(date);
    }

}
