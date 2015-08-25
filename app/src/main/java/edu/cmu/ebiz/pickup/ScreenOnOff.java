package edu.cmu.ebiz.pickup;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;

public class ScreenOnOff extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_on_off);

        // Start AEScreenOnOffService Service

        Intent i0 = new Intent();
        i0.setAction("edu.cmu.ebiz.pickup.AEScreenOnOffService");
        startService(i0);

        this.finish();
    }

}