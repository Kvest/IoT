package com.kvest.iot.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;
import com.kvest.iot.R;

import java.io.IOException;

/**
 * Created by kvest on 1/18/17.
 */

public class RedYellowLedsActivity extends Activity {
    private static final int INTERVAL_BETWEEN_BLINKS_MS = 1000;
    private static final String TAG = "KVEST_TAG";

    private Handler mHandler = new Handler();
    private Gpio mRedLedGpio, mYellowLedGpio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_activity);

        init();
    }


    private void init() {
        PeripheralManagerService service = new PeripheralManagerService();
        try {
            String redPinName = "BCM6";
            mRedLedGpio = service.openGpio(redPinName);
            mRedLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

            String yellowPinName = "BCM5";
            mYellowLedGpio = service.openGpio(yellowPinName);
            mYellowLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);

            Log.i(TAG, "Start blinking LED GPIO pin");
            // Post a Runnable that continuously switch the state of the GPIO, blinking the
            // corresponding LED
            mHandler.post(mBlinkRunnable);
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        }
    }

    private void cleanUp() {
        // Remove pending blink Runnable from the handler.
        mHandler.removeCallbacks(mBlinkRunnable);
        // Close the Gpio pin.
        Log.i(TAG, "Closing LED GPIO pin");
        try {
            mRedLedGpio.close();
            mYellowLedGpio.close();
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        } finally {
            mRedLedGpio = null;
            mYellowLedGpio = null;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        cleanUp();
    }

    private Runnable mBlinkRunnable = new Runnable() {
        @Override
        public void run() {
            // Exit Runnable if the GPIO is already closed
            if (mRedLedGpio == null || mYellowLedGpio == null) {
                return;
            }
            try {
                // Toggle the GPIO state
                mRedLedGpio.setValue(!mRedLedGpio.getValue());
                mYellowLedGpio.setValue(!mYellowLedGpio.getValue());
                Log.d(TAG, "State set red to " + mRedLedGpio.getValue() + " and yellow to " + mYellowLedGpio.getValue());

                // Reschedule the same runnable in {#INTERVAL_BETWEEN_BLINKS_MS} milliseconds
                mHandler.postDelayed(mBlinkRunnable, INTERVAL_BETWEEN_BLINKS_MS);
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }
    };

}
