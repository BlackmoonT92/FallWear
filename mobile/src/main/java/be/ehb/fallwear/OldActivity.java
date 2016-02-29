package be.ehb.fallwear;


import android.content.SharedPreferences;
import android.os.Messenger;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import be.ehb.common.FallDetectionIntentService;
import be.ehb.common.FallDetectionService;
import be.ehb.common.MessageHandler;

import static be.ehb.fallwear.R.*;

public class OldActivity extends AppCompatActivity implements MessageHandler.FallingEventListener,MessageHandler.RunningEventListener {

    private static final String TAG = "MainAct";
    private MessageHandler messageHandler = new MessageHandler();

    public void onStartClick(View view) {
        FallDetectionService.Actions.StartMonitor(this, new Messenger(messageHandler), 0, 250, 50, 250, 100);
    }

    public void onStopClick(View view) {
        //FallDetectionIntentService.actionStopMonitor(this);
        FallDetectionService.Actions.StopMonitor(this);
    }

    public void onSimulateFall(View view) {
        //FallDetectionIntentService.actionSimulateFall(this, new Messenger(messageHandler));
        FallDetectionService.Actions.SimulateFall(this, new Messenger(messageHandler));
    }

    public void onResetClick(View view) {
        SharedPreferences settings = getSharedPreferences(FallDetectionIntentService.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        boolean isRunning = settings.getBoolean(FallDetectionIntentService.PREFS_KEY_RUNNING, false);
        editor.putInt(FallDetectionIntentService.PREFS_KEY_FALLEN, 0);
        editor.apply();
        hasFallen(0);
        onRunningChanged(isRunning);
    }

    @Override
    protected void onResume() {
        super.onResume();
        messageHandler.registerFallingListener(this);
        messageHandler.registerRunningListener(this);
        SharedPreferences settings = getSharedPreferences(FallDetectionIntentService.PREFS_NAME, 0);
        boolean isRunning = settings.getBoolean(FallDetectionIntentService.PREFS_KEY_RUNNING,false);
        int fallen = settings.getInt(FallDetectionIntentService.PREFS_KEY_FALLEN, 0);
        hasFallen(fallen);
        onRunningChanged(isRunning);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_old);
    }




    @Override
    public void hasFallen(int times) {
        TextView txtFallen = (TextView) findViewById(id.txtFallen);
        String fallenFormat = getResources().getString(string.you_have_fallen);
        String fallenMsg = String.format(fallenFormat, times);
        txtFallen.setText(fallenMsg);
    }

    @Override
    public void onRunningChanged(boolean running) {
        TextView txtRunning = (TextView) findViewById(id.txtRunning);
        if (running) {
            txtRunning.setText(string.running);
        } else {
            txtRunning.setText(string.stopped);
        }
    }
}


