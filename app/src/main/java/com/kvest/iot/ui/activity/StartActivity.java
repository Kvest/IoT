package com.kvest.iot.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by kvest on 1/18/17.
 */

public class StartActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("KVEST_TAG", "Hello Android IoT");
    }
}
