package com.kvest.iot.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;

/**
 * Created by kvest on 1/21/17.
 */

public class NumberIndicatorActivity extends Activity {
    private static final String TAG = "KVEST_TAG";
    private static final int INTERVAL = 1000;
    private static final String[] GPIO_NAMES = {"BCM5", "BCM6", "BCM12", "BCM13", "BCM16", "BCM17", "BCM18"};
    private static final int[] DIGITS = {63, 12, 91, 94, 108, 118, 119, 28, 127, 126};

    private Gpio[] segments;
    private Handler mHandler = new Handler();
    private int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();

        index = -1;
        mHandler.post(timerRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        cleanUp();
    }

    private void init() {
        PeripheralManagerService service = new PeripheralManagerService();
        try {
            segments = new Gpio[GPIO_NAMES.length];
            for (int i = 0; i < segments.length; i++) {
                segments[i] = service.openGpio(GPIO_NAMES[i]);
                segments[i].setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            }
        } catch (IOException e) {
            cleanUp();
        }
    }

    private void cleanUp() {
        // Close the Gpio pin.
        Log.i(TAG, "Closing GPIO pins");
        try {
            for (int i = 0; i < segments.length; i++) {
                if (segments[i] != null) {
                    segments[i].close();
                    segments[i] = null;
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        } finally {
            segments = null;
        }
    }

    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            // Exit Runnable if the GPIO is already closed
            if (segments == null) {
                return;
            }
            try {

                index = (index + 1) % DIGITS.length;

                boolean value;
                for (int i = 0; i < segments.length; i++) {
                    value = ((DIGITS[index] >> i) & 1) == 1;
                    segments[i].setValue(value);
                }

                // Reschedule next change
                mHandler.postDelayed(timerRunnable, INTERVAL);
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }
    };
}
