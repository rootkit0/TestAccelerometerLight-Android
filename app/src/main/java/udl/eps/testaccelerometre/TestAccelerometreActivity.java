package udl.eps.testaccelerometre;

import android.app.Activity;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import android.widget.TextView;
import android.widget.Toast;

public class TestAccelerometreActivity extends Activity implements SensorEventListener {
    private SensorManager sensorManager;
    private boolean color = false;
    private TextView view;
    private TextView viewAcc;
    private TextView viewLight;
    private long lastUpdateAcc, lastUpdateLight;

    private float maxLightRange, medLightRange, minLightRange;
    private float lastLightValue = 0;

    private static final String TAG = "ActivityAccelerometer";

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        view = findViewById(R.id.textView);
        view.setBackgroundColor(Color.GREEN);
        viewAcc = findViewById(R.id.textView2);
        viewLight = findViewById(R.id.textView3);
        viewLight.setBackgroundColor(Color.YELLOW);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        setListeners();
    }

    private void setListeners() {
        if(sensorManager != null) {
            Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            Sensor light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

            if(accelerometer != null) {
                sensorManager.registerListener(this,
                        accelerometer,
                        SensorManager.SENSOR_DELAY_NORMAL);

                String showCapabilities = getText(R.string.shake) +
                        "\nMax Delay: " + accelerometer.getMaxDelay() +
                        "\nMin Delay: " + accelerometer.getMinDelay() +
                        "\nPower: " + accelerometer.getPower() +
                        "\nResolution: " + accelerometer.getResolution() +
                        "\nVersion: " + accelerometer.getVersion();

                viewAcc.setText(showCapabilities);
            }
            else {
                viewAcc.setText(R.string.noAccel);
            }
            lastUpdateAcc = System.currentTimeMillis();

            if(light != null) {
                sensorManager.registerListener(this,
                        light,
                        SensorManager.SENSOR_DELAY_NORMAL);

                maxLightRange = light.getMaximumRange();
                medLightRange = 2/3 * maxLightRange;
                minLightRange = 1/3 * maxLightRange;

                String showCapabilities = getText(R.string.luminic) + "\n"
                        + getText(R.string.high) + maxLightRange + "\n"
                        + getText(R.string.medium) + maxLightRange + "\n"
                        + getText(R.string.low) + minLightRange + "\n";

                viewLight.setText(showCapabilities);
            }
            else {
                viewLight.setText(R.string.noLuminic);
            }
            lastUpdateLight = System.currentTimeMillis();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            getAccelerometer(event);
        }

        if(event.sensor.getType() == Sensor.TYPE_LIGHT) {
            getLight(event);
        }
    }

    private void getAccelerometer(SensorEvent event) {
        float[] values = event.values;
        // Movement
        float x = values[0];
        float y = values[1];
        float z = values[2];

        float accelationSquareRoot = (x * x + y * y + z * z) / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
        long actualTime = System.currentTimeMillis();
        if (accelationSquareRoot >= 2)
        {
            if (actualTime - lastUpdateAcc < 200) {
                return;
            }
            lastUpdateAcc = actualTime;

            Toast.makeText(this, R.string.shuffed, Toast.LENGTH_SHORT).show();
            if (color) {
                view.setBackgroundColor(Color.GREEN);

            } else {
                view.setBackgroundColor(Color.RED);
            }
            color = !color;
        }
    }

    private void getLight(SensorEvent event) {
        float lightValue = event.values[0];
        long actualTime = System.currentTimeMillis();

        if(lastLightValue != lightValue) {
            if((actualTime - lastUpdateLight) > 1000) {
                lastUpdateLight = actualTime;
                lastLightValue = lightValue;

                viewLight.append(getText(R.string.newLuminic) + " " + lastLightValue + "\n");

                if (lastLightValue < minLightRange) {
                    viewLight.append(getText(R.string.low) + "\n");
                }
                else if(lastLightValue < medLightRange) {
                    viewLight.append(getText(R.string.medium) + "\n");
                }
                else {
                    viewLight.append(getText(R.string.high) + "\n");
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    protected void onPause() {
        // unregister listener
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setListeners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
    }
}