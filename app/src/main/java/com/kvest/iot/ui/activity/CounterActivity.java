package com.kvest.iot.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;

/**
 * Created by kvest on 1/21/17.
 */

public class CounterActivity extends Activity {
    private static final String TAG = "KVEST_TAG";
    private static final String[] GPIO_NAMES = {"BCM5", "BCM6", "BCM12", "BCM13", "BCM16", "BCM17", "BCM18"};
    private static final String GPIO_INCREASE_BUTTON = "BCM23";
    private static final int[] DIGITS = {63, 12, 91, 94, 108, 118, 119, 28, 127, 126};

    private Gpio[] segments;
    private Gpio increaseButtonGpio;
    private int value;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();

        setValue(0);
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

            increaseButtonGpio = service.openGpio(GPIO_INCREASE_BUTTON);
            increaseButtonGpio.setDirection(Gpio.DIRECTION_IN);
            increaseButtonGpio.setEdgeTriggerType(Gpio.EDGE_FALLING);
            increaseButtonGpio.registerGpioCallback(new GpioCallback() {
                @Override
                public boolean onGpioEdge(Gpio gpio) {
                    Log.d(TAG, "INCREASE");
                    setValue(value + 1);

                    return true;
                }
            });
        } catch (IOException e) {
            cleanUp();
        }
    }

    private void cleanUp() {
        // Close the Gpio pin.
        Log.i(TAG, "Closing GPIO pins");
        try {
            increaseButtonGpio.close();

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
            increaseButtonGpio = null;
        }
    }

    public void setValue(int value) {
        if (value == DIGITS.length) {
            this.value = 0;
        } else if (value < 0) {
            this.value = DIGITS.length - 1;
        } else {
            this.value = value;
        }

        try {
            //update
            for (int i = 0; i < segments.length; i++) {
                segments[i].setValue(((DIGITS[this.value] >> i) & 1) == 1);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error setting value");
        }
    }
}
