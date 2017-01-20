package com.kvest.iot.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;

/**
 * Created by kvest on 1/20/17.
 */

public class ButtonActivity extends Activity {
    private static final String TAG = "KVEST_TAG";

    private Gpio mButtonGpio;
    private Gpio mRedLedGpio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Starting ButtonActivity");

        PeripheralManagerService service = new PeripheralManagerService();
        try {
            String redPinName = "BCM6";
            mRedLedGpio = service.openGpio(redPinName);
            mRedLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

            String pinName = "BCM21";
            mButtonGpio = service.openGpio(pinName);
            mButtonGpio.setDirection(Gpio.DIRECTION_IN);
            mButtonGpio.setEdgeTriggerType(Gpio.EDGE_BOTH);
            mButtonGpio.registerGpioCallback(new GpioCallback() {
                @Override
                public boolean onGpioEdge(Gpio gpio) {
                    Log.i(TAG, "GPIO changed, button pressed");
                    try {
                        mRedLedGpio.setValue(!gpio.getValue());
                    } catch (IOException e) {
                        Log.e(TAG, "Error while switching the led", e);
                    }
                    // Return true to continue listening to events
                    return true;
                }
            });
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mButtonGpio != null) {
            // Close the Gpio pin
            Log.i(TAG, "Closing Button GPIO pin");
            try {
                mRedLedGpio.close();
                mButtonGpio.close();
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            } finally {
                mRedLedGpio = null;
                mButtonGpio = null;
            }
        }
    }
}
