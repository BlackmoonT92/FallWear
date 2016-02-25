package be.ehb.common;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by davy.van.belle on 25/01/2016.
 */
public class MessageHandler extends Handler {
    public static int MESSAGE_ARG_REFRESH = 2;
    public static int MESSAGE_ARG_FALLEN = 1;

    private static final String TAG = "MessageHandler";
    private static ArrayList<FallingEventListener> fallingEventListeners = new ArrayList<>();
    private static ArrayList<RunningEventListener> runningEventListeners = new ArrayList<>();

    public interface FallingEventListener {
        void hasFallen(int times);
    }

    public interface RunningEventListener {
        void onRunningChanged(boolean running);
    }

    public void registerFallingListener(FallingEventListener listener){
        if (!fallingEventListeners.contains(listener)) {
            fallingEventListeners.add(listener);
        }
    }

    public void unregisterFallingListener(FallingEventListener listener){
        fallingEventListeners.remove(listener);
    }

    public void registerRunningListener(RunningEventListener listener){
        if (!runningEventListeners.contains(listener)) {
            runningEventListeners.add(listener);
        }
    }

    public void unregisterRunningListener(RunningEventListener listener){
        runningEventListeners.remove(listener);
    }

    @Override
    public void handleMessage(Message message) {
        int arg1 = message.arg1;
        Log.d(TAG, "Message: " + arg1);
        if (arg1 == MESSAGE_ARG_FALLEN){
            hasFallen(message.arg2);
        } else if (arg1 == MESSAGE_ARG_REFRESH) {
            if (message.arg2 == 1){
                onRunningChanged(true);
            } else if (message.arg2 == 0) {
                onRunningChanged(false);
            }
        }
    }

    private void onRunningChanged(boolean b) {
        for (RunningEventListener rel : runningEventListeners){
            rel.onRunningChanged(b);
        }
    }

    private void hasFallen(int times) {
        for (FallingEventListener fel : fallingEventListeners){
            fel.hasFallen(times);
        }
    }
}