package be.ehb.common;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by davy.van.belle on 3/02/2016.
 */
public class FallDetectionService extends Service {

    private static final String TAG = "FallDetecionService";

    private static final String ACTION_START = "be.ehb.fallwear.action.START";
    private static final String ACTION_STOP = "be.ehb.fallwear.action.STOP";
    private static final String ACTION_SIMULATE_FALL = "be.ehb.fallwear.action.simulate";

    private static final String EXTRA_SENSITIVITY = "be.ehb.fallwear.extra.SENSITIVITY";
    private static final String EXTRA_FALLING_TIMEOUT = "be.ehb.fallwear.extra.FALLING";
    private static final String EXTRA_IMPACT_TIMEOUT = "be.ehb.fallwear.extra.IMPACT";
    private static final String EXTRA_DOZED_TIMEOUT = "be.ehb.fallwear.extra.DOZED";
    private static final String EXTRA_ALERT_TIMEOUT = "be.ehb.fallwear.extra.ALERT";

    private static final String EXTRA_MESSENGER = "be.ehb.fallwear.extra.MESSENGER";

    private static final long DEFAULT_FALLING = 100;
    private static final long DEFAULT_ALERT = 250;
    private static final long DEFAULT_IMPACT = 500;
    private static final long DEFAULT_DOZED =500;
    private static final int DEFAULT_SENSITIVITY = 50;

    public static final String PREFS_NAME = "MyPrefs" ;
    public static final String PREFS_KEY_FALLEN = "key_fallen";
    public static final String PREFS_KEY_RUNNING = "key_running";

    private static final int TRUE = 1;
    private static final int FALSE = 0;

    private static SensorManager mSensorManager;
    private static Sensor mSensor;
    private static FallSensorEventListener fsListener;

    private static Messenger messenger;
    private Handler handler;

    private int fallen;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "STARTING");
        handler = new Handler(getMainLooper());
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        fallen = settings.getInt(PREFS_KEY_FALLEN,0);

        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_START.equals(action)) {
                final long falling = intent.getLongExtra(EXTRA_FALLING_TIMEOUT,DEFAULT_FALLING);
                final long alert = intent.getLongExtra(EXTRA_ALERT_TIMEOUT,DEFAULT_ALERT);
                final long impact = intent.getLongExtra(EXTRA_IMPACT_TIMEOUT,DEFAULT_IMPACT);
                final long dozed = intent.getLongExtra(EXTRA_DOZED_TIMEOUT, DEFAULT_DOZED);
                final int sensitivity = intent.getIntExtra(EXTRA_SENSITIVITY, DEFAULT_SENSITIVITY);
                messenger = (Messenger) intent.getExtras().get(EXTRA_MESSENGER);
                handleActionStartMonitor(falling, alert, impact, dozed, sensitivity);
            } else if (ACTION_STOP.equals(action)) {
                handleActionStopMonitor();
            } else if (ACTION_SIMULATE_FALL.equals(action)){
                messenger = (Messenger) intent.getExtras().get(EXTRA_MESSENGER);
                handleActionSimulateFall();
            }
        }
        
        return START_STICKY;
    }

    private void handleActionStartMonitor(long falling, long alert, long impact, long dozed, int sens) {
        Log.d(TAG,"Starting monitor");
        if (fsListener == null) {
            fsListener = new FallSensorEventListener(sens,falling,alert,impact,dozed);
            fsListener.setAlarmListener(alarmListener);
            mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mSensorManager.registerListener(fsListener, mSensor, SensorManager.SENSOR_DELAY_UI);
        }
        SharedPreferences settings = getSharedPreferences(PREFS_NAME,0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(PREFS_KEY_RUNNING, true) ;
        editor.apply();
        Message message = Message.obtain();
        message.arg1 = MessageHandler.MESSAGE_ARG_REFRESH;
        message.arg2 = TRUE;
        try {
            messenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void handleActionStopMonitor() {
        if (fsListener != null) {
            Log.d(TAG,"Stopping monitor");
            mSensorManager.unregisterListener(fsListener);
            mSensorManager = null;
            mSensor = null;
            fsListener = null;
        }
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(PREFS_KEY_RUNNING, false) ;
        editor.apply();

        Message message = Message.obtain();
        message.arg1 = MessageHandler.MESSAGE_ARG_REFRESH;
        message.arg2 = FALSE;
    }

    private void handleActionSimulateFall(){
        Log.d(TAG,"Call simulate");
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Delayed");
                fsListener.simulateAlarm();
            }
        }, 2000);

    }

    private FallSensorEventListener.AlarmListener alarmListener = new FallSensorEventListener.AlarmListener() {
        @Override
        public void onAlarm() {
            fallen++;
            handler.post(new DisplayToast(FallDetectionService.this, "Did  you fall", Toast.LENGTH_SHORT));
            Log.d(TAG, "Alarm Alarm Alarm");
            Log.d(TAG, "Fallen: " + fallen);
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt(PREFS_KEY_FALLEN, fallen);
            editor.apply();

            Message message = Message.obtain();
            message.arg1 = MessageHandler.MESSAGE_ARG_FALLEN;
            message.arg2 = fallen;
            try {
                messenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };


    public static class Actions {
        public static void StartMonitor(Context context, Messenger messenger) {
            Intent intent = new Intent(context, FallDetectionService.class);
            intent.putExtra(EXTRA_MESSENGER, messenger);
            intent.setAction(ACTION_START);
            context.startService(intent);
        }

        public static void StartMonitor(Context context, Messenger messenger, int sensitivity) {
            Intent intent = new Intent(context, FallDetectionService.class);
            intent.putExtra(EXTRA_MESSENGER, messenger);
            intent.setAction(ACTION_START);
            intent.putExtra(EXTRA_SENSITIVITY, sensitivity);
            context.startService(intent);
        }

        public static void StartMonitor(Context context, Messenger messenger, int sensitivity, long alert_timeout, long falling_timeout, long impact_timeout, long dozed_timeout) {
            Intent intent = new Intent(context, FallDetectionService.class);
            intent.putExtra(EXTRA_MESSENGER, messenger);
            intent.setAction(ACTION_START);
            intent.putExtra(EXTRA_ALERT_TIMEOUT, alert_timeout);
            intent.putExtra(EXTRA_DOZED_TIMEOUT, dozed_timeout);
            intent.putExtra(EXTRA_FALLING_TIMEOUT, falling_timeout);
            intent.putExtra(EXTRA_IMPACT_TIMEOUT, impact_timeout);
            intent.putExtra(EXTRA_SENSITIVITY, sensitivity);
            context.startService(intent);
        }

        public static void SimulateFall(Context context, Messenger messenger) {
            Intent intent = new Intent(context, FallDetectionService.class);
            intent.putExtra(EXTRA_MESSENGER, messenger);
            intent.setAction(ACTION_SIMULATE_FALL);
            context.startService(intent);
        }

        public static void StopMonitor(Context context) {
            Intent intent = new Intent(context, FallDetectionService.class);
            intent.setAction(ACTION_STOP);
            context.startService(intent);
        }
    }

}
