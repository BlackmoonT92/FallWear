package be.ehb.fallwear;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

public class IncommingAlarm extends Activity {

    private static final String TAG = "IncommingAlarm";

    private static final long[] pattern = { 0, 800, 500  };

    Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Created");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        setContentView(R.layout.activity_incomming_alarm);

       // playRingtone();

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);


        switch( audio.getRingerMode() ){
            case AudioManager.RINGER_MODE_NORMAL:
                Log.d(TAG, "Normal Mode");
                startVibrate();
                playRingtone();
                break;
            case AudioManager.RINGER_MODE_SILENT:
                Log.d(TAG,"Silent Mode");
                break;
            case AudioManager.RINGER_MODE_VIBRATE:
                Log.d(TAG,"Vibrate Mode");
                startVibrate();
                break;
        }


        //sendToWatch();

    }

    /*
    public void onBtnOpenClick(View view) {
        Log.d(TAG,"Open button clicked");
        stopRingtone();
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.cancel();
    }
    */


    @Override
    protected void onPause() {
        Log.d(TAG, "Pauzing");
        //stopVibrate();
        //stopRingtone();
        super.onPause();
    }

    public void onBtnDismissClick(View view) {
        Log.d(TAG, "Dismiss clicked");
        stopRingtone();
        stopVibrate();
        finish();
    }

    private void startVibrate(){
        vibrator.vibrate(pattern, 0);
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        registerReceiver(vibrateReceiver, filter);
    }

    private void stopVibrate(){
        vibrator.cancel();
        try {
            unregisterReceiver(vibrateReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public BroadcastReceiver vibrateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                Log.d(TAG,"Screen Off");
                vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(pattern, 0);
            }
        }
    };

    private void playRingtone() {
        //this will sound the alarm tone
        //this will sound the alarm once, if you wish to
        //raise alarm in loop continuously then use MediaPlayer and setLooping(true)

        //ringtone.play();
        Intent service = new Intent(this, RingtoneService.class);
        service.setAction(RingtoneService.ACTION_PLAY);
        startService(service);
    }

    private void stopRingtone() {
        //ringtone.stop();
        Intent service = new Intent(this, RingtoneService.class);
        service.setAction(RingtoneService.ACTION_STOP);
        startService(service);
    }

    /*
    private void sendToWatch() {

        Intent service = new Intent(this, RingtoneService.class);
        service.setAction(RingtoneService.ACTION_STOP);

        PendingIntent pendingIntent = PendingIntent.getService(this,0,service,PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        NotificationCompat.WearableExtender wearableOptions =
                new NotificationCompat.WearableExtender();
        builder.setContentTitle(getString(R.string.notif_title))
                .setSmallIcon(R.mipmap.ic_launcher);

        builder.addAction(R.drawable.ic_cast_dark,
                getString(R.string.reply_action),
                pendingIntent)
                .build();


        builder.setPriority(Notification.PRIORITY_HIGH);
        builder.setVibrate(new long[]{0, 100, 50, 100});

        builder.extend(wearableOptions);
        NotificationManagerCompat.from(this).notify(0, builder.build());

    }

    */

}
