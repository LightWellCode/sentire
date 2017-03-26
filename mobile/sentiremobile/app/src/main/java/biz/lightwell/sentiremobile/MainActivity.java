package biz.lightwell.sentiremobile;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import biz.lightwell.sentiremobile.aaa.GoogleAuthActivity;
import biz.lightwell.sentiremobile.myUtil.C;
import biz.lightwell.sentiremobile.sensorMgmt.SensorMgmtActivity;


public class MainActivity extends AppCompatActivity implements View.OnClickListener  {
    private Button btnAuthOpen, btnSensorOpen;

    // ------------------------------------------------ OVERRIDE
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (C.LOGGING) { Log.d(C.LOGTAG, "MainActivity - onCreate"); }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnAuthOpen = (Button) findViewById(R.id.btn_authenticationMgmt);
        btnSensorOpen = (Button) findViewById(R.id.btn_sensorMgmt);
        btnAuthOpen.setOnClickListener(this);
        btnSensorOpen.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_authenticationMgmt:
                openAuthenticationActivity();
                break;
            case R.id.btn_sensorMgmt:
                openSensorsActivity();
                break;
        }
    }

    // ------------------------------------------------ METHODS & BUTTON HANDLERS
    public void openAuthenticationActivity() {
        Intent intent = new Intent (this, GoogleAuthActivity.class);
        startActivity(intent);
    }

    public void openSensorsActivity() {
        Intent intent = new Intent (this, SensorMgmtActivity.class);
        startActivity(intent);
    }

    // ------------------------------------------------ PRIVATE CLASSES

}
