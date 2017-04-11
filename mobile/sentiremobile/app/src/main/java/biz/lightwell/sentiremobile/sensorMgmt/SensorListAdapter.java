package biz.lightwell.sentiremobile.sensorMgmt;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import biz.lightwell.sentiremobile.R;
import biz.lightwell.sentiremobile.myUtil.C;

/**
 * Created by stew on 3/29/2017.
 * Purpose:
 * Notes:
 */

public class SensorListAdapter extends ArrayAdapter<SensorDataObj> {
    public SensorListAdapter(Context context, List<SensorDataObj> sensorDataPts) {
        super(context, 0 , sensorDataPts);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.i(C.LOGTAG, "sensor list adapter - getview");
        ViewHolder viewHolder;
        SensorDataObj sdo = getItem(position);

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.row_sensor_data, parent, false);

            viewHolder.sensor_date  = (TextView) convertView.findViewById(R.id.row_sensor_date);
            viewHolder.sensor_key   = (TextView) convertView.findViewById(R.id.row_sensor_key);
            viewHolder.sensor_value = (TextView) convertView.findViewById(R.id.row_sensor_value);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.sensor_date.setText(sdo.getTime());
        viewHolder.sensor_key.setText(sdo.getSensorDataKey());
        viewHolder.sensor_value.setText(sdo.getSensorDataValue());

        return convertView;
    }

    // ------------------------------------------------------------------------ methods
    public void reload() {
        this.clear();
        List<SensorDataObj> listof =(List) SensorDataObj.find(SensorDataObj.class, "sensor_data_key != ?", "''");
        if (listof.size() > 0) {
            this.addAll(listof);
        }
    }

    // ------------------------------------------------------------------------ private classes
    private static class ViewHolder{
        TextView sensor_date;
        TextView sensor_key;
        TextView sensor_value;
    }
}
