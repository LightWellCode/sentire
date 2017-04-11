package biz.lightwell.sentiremobile.aaa;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.List;

import biz.lightwell.sentiremobile.R;

public class ConfigOptionsActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView txtCloudUrl, txtLastSentDateTime;
    private Button btnSaveOptions;
    private ConfigDataObj configDataObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_options);

        btnSaveOptions = (Button) findViewById(R.id.btn_save_options);
        txtCloudUrl     = (TextView) findViewById(R.id.cloud_url);
        txtLastSentDateTime = (TextView)findViewById(R.id.last_sent_datetime);

        btnSaveOptions.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ConfigDataObj.count(ConfigDataObj.class) > 0) {
            List<ConfigDataObj> configDataObjList = (List) ConfigDataObj.find(ConfigDataObj.class, "cloud_url != ?", "''");
            configDataObj = configDataObjList.get(0);
            txtCloudUrl.setText(configDataObj.getCloudURL());
            txtLastSentDateTime.setText(configDataObj.getTime());
        } else {
            configDataObj = new ConfigDataObj();
        }

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_save_options:
                save_options();
                break;
        }
    }

    private void save_options() {
        configDataObj.setCloudURL(txtCloudUrl.getText().toString());
        configDataObj.save();
        Toast.makeText(this, "Options Saved", Toast.LENGTH_SHORT).show();
    }
}
