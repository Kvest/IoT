package com.kvest.iot.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;

/**
 * Created by kvest on 1/25/17.
 */

public class BH1750Test extends Activity {
    private static final String TAG = "KVEST_TAG";
    public static final int ADDRESS = 0x23;
    private static final byte BH1750_CONTINUOUS_HIGH_RES_MODE = 0x10;

    private I2cDevice bh1750;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("KVEST_TAG", "Start BH1750Test");

        PeripheralManagerService service = new PeripheralManagerService();
        try {
            bh1750 = service.openI2cDevice("I2C1", ADDRESS);

            SystemClock.sleep(500);
            bh1750.writeRegByte(ADDRESS, BH1750_CONTINUOUS_HIGH_RES_MODE);
            int level;
            // Read two bytes, which are low and high parts of sensor value
            level = bh1750.readRegByte(ADDRESS) & 0xff;//Read LSB
            level <<= 8;
            level |= bh1750.readRegByte(ADDRESS) & 0xff;//Read MSB
            level /= 1.2;
            Log.d(TAG, "Value:" + level);

        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (bh1750 != null) {
            try {
                bh1750.close();
            } catch (IOException e) {}
            bh1750 = null;
        }
    }
}
