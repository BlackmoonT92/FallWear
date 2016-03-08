package be.ehb.common;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

/**
 * Created by davy.van.belle on 21/01/2016.
 */
public class FallSensorEventListener implements SensorEventListener {

    private static final String TAG = "FallSensorEventListener";
    private float[]  gravity = new float[3];
    private float[]  gforce = new float[3];

    private final float T1 = 0.4f;
    private final float T2 = 1.5f;
    private final float mean = 1.0f;
    private final float hist = 0.1f;

    private long falling_timeout;
    private long alert_timeout;
    private long impact_timeout;
    private long dozed_timeout;
    private int sensitivity;

    private float min = 1f;
    private float max = 1f;
    private float avg = 1f;

    private enum State {IDLE,FALLING,ALERT,IMPACT,ALARM,DOZED}

    private State mState = State.IDLE;

    private long now = System.currentTimeMillis();

    private AlarmListener alarmListener = null;

    public FallSensorEventListener(int sensitivity, long falling_timeout, long alert_timeout, long impact_timeout, long dozed_timeout){
        this.sensitivity = sensitivity;
        this.falling_timeout = falling_timeout;
        this.alert_timeout = alert_timeout;
        this.impact_timeout = impact_timeout;
        this.dozed_timeout = dozed_timeout;
    }


    @Override
        public void onSensorChanged(SensorEvent event) {
            // In this example, alpha is calculated as t / (t + dT),
            // where t is the low-pass filter's time-constant and
            // dT is the event delivery rate.
            final float G = 9.81f;
            final float alpha = (float) 0.8;

            // Isolate the force of gravity with the low-pass filter.
            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

            gforce[0] = gravity[0] / G;
            gforce[1] = gravity[1] / G;
            gforce[2] = gravity[2] / G;

            float gsum = (float) Math.sqrt(Math.pow(gforce[0], 2) + Math.pow(gforce[1], 2) + Math.pow(gforce[2], 2));

            switch (mState)
            {
                case IDLE:
                    if (gsum < T1){
                        mState = State.FALLING;
                        now = System.currentTimeMillis();
                    }
                    break;

                case FALLING:
                    if (gsum > T1) {
                        mState = State.IDLE;
                    }

                    if ((System.currentTimeMillis()-now) > falling_timeout){
                        now = System.currentTimeMillis();
                        mState = State.ALERT;
                    }
                    break;

                case ALERT:
                    if (gsum > T2){
                        now = System.currentTimeMillis();
                        mState = State.IMPACT;
                    }

                    if ((System.currentTimeMillis()-now) > alert_timeout){
                        mState = State.IDLE;
                    }
                    break;

                case IMPACT:
                    if ((gsum < (mean + hist)) &&  (gsum > (mean - hist))){
                        now = System.currentTimeMillis();
                        mState = State.DOZED;
                    }

                    if ((System.currentTimeMillis()-now) > impact_timeout){
                        mState = State.IDLE;
                    }

                    break;

                case DOZED:
                    if ((gsum > (mean + hist)) &&  (gsum < (mean - hist))){
                        mState = State.IDLE;
                    }

                    if ((System.currentTimeMillis()-now) > dozed_timeout){
                        mState = State.ALARM;
                    }
                    break;

                case ALARM:
                    if (this.alarmListener != null){
                        alarmListener.onAlarm();
                    }
                    mState = State.IDLE;
                    break;
                default:
                    break;
            }

            //Log.d(TAG, "New state: " + mState.name());
            //Log.d(TAG, "Time: " + now);


            if (gsum < min) min = gsum;
            if (gsum > max) max = gsum;
            avg = (avg + gsum)/2;

         Log.d(TAG, "State: " + mState.name() + "\tTime: "  + System.currentTimeMillis() + "\tG-force sum: " + gsum);
         //Log.d(TAG,"Timeouts: " + falling_timeout + alert_timeout + impact_timeout + dozed_timeout);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    public void simulateAlarm(){
        Log.d(TAG,"execute simulate");
        alarmListener.onAlarm();
    }

    public void setAlarmListener(AlarmListener listener){
        this.alarmListener = listener;
    }

    public interface AlarmListener{
        void onAlarm();
    }
}
