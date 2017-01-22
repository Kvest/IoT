package com.kvest.iot.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.Pwm;

import java.io.IOException;

/**
 * Created by kvest on 1/20/17.
 */

public class PWMActivity extends Activity {
    private static final String TAG = "KVEST_TAG";
    public static final int BRIGHTNESS_STEP = 5;
    public static final int DEFAULT_BRIGHTNESS = 50;

    private Gpio decreaseButtonGpio, increaseButtonGpio;
    private Pwm pwm;

    private int brightness;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PeripheralManagerService service = new PeripheralManagerService();
        try {
            increaseButtonGpio = service.openGpio("BCM5");
            increaseButtonGpio.setDirection(Gpio.DIRECTION_IN);
            increaseButtonGpio.setEdgeTriggerType(Gpio.EDGE_FALLING);
            increaseButtonGpio.registerGpioCallback(new GpioCallback() {
                @Override
                public boolean onGpioEdge(Gpio gpio) {
                    Log.i(TAG, "Increase button pressed");

                    brightness = Math.min(brightness + BRIGHTNESS_STEP, 100);
                    updateBrightness();

                    // Return true to continue listening to events
                    return true;
                }
            });

            decreaseButtonGpio = service.openGpio("BCM6");
            decreaseButtonGpio.setDirection(Gpio.DIRECTION_IN);
            decreaseButtonGpio.setEdgeTriggerType(Gpio.EDGE_FALLING);
            decreaseButtonGpio.registerGpioCallback(new GpioCallback() {
                @Override
                public boolean onGpioEdge(Gpio gpio) {
                    Log.i(TAG, "Decrease button pressed");

                    brightness = Math.max(brightness - BRIGHTNESS_STEP, 0);
                    updateBrightness();

                    // Return true to continue listening to events
                    return true;
                }
            });

            brightness = DEFAULT_BRIGHTNESS;
            pwm = service.openPwm("PWM0");
            initializePwm(pwm);
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        closeGpio(increaseButtonGpio);
        increaseButtonGpio = null;

        closeGpio(decreaseButtonGpio);
        decreaseButtonGpio = null;

        if (pwm != null) {
            try {
                pwm.close();
            } catch (IOException e) {
                Log.w(TAG, "Unable to close PWM", e);
            } finally {
                pwm = null;
            }
        }
    }

    private void closeGpio(Gpio gpio) {
        if (gpio != null) {
            try {
                gpio.close();
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }
    }

    public void initializePwm(Pwm pwm) throws IOException {
        pwm.setPwmFrequencyHz(200);
        pwm.setPwmDutyCycle(brightness);

        // Enable the PWM signal
        pwm.setEnabled(true);
    }

    private void updateBrightness() {
        Log.d(TAG, "Set brightness " + brightness + "%");
        try {
            pwm.setEnabled(false);
            pwm.setPwmDutyCycle(brightness);
            pwm.setEnabled(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
