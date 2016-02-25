package be.ehb.fallwear;

import android.app.Activity;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Messenger;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import be.ehb.common.AppConstants;
import be.ehb.common.FallDetectionService;
import be.ehb.common.MessageHandler;

public class MainActivity extends Activity implements MessageHandler.FallingEventListener, MessageHandler.RunningEventListener  {

    private static final String TAG = "WearMainAct";
    private MessageHandler messageHandler = new MessageHandler();

    public void onStartClick(View view) {
        FallDetectionService.Actions.StartMonitor(this, new Messenger(messageHandler), 0, 250, 50, 250, 100);
    }

    public void onStopClick(View view) {
        FallDetectionService.Actions.StopMonitor(this);
    }


    public void onSimulateFall(View view) {
        FallDetectionService.Actions.SimulateFall(this, new Messenger(messageHandler));
    }

    public void onConnectClick(View view){

    }

    public void onSendClick(View view){
        Log.d(TAG, "OnClick called");
        //sendMsgHTTP("test message");
        sendMsg(AppConstants.MSG_FALLEN);

    }

    public void onResetClick(View view) {
        SharedPreferences settings = getSharedPreferences(FallDetectionService.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        boolean isRunning = settings.getBoolean(FallDetectionService.PREFS_KEY_RUNNING, false);
        editor.putInt(FallDetectionService.PREFS_KEY_FALLEN, 0);
        editor.apply();
        hasFallen(0);
        onRunningChanged(isRunning);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"resume");
        messageHandler.registerRunningListener(this);
        messageHandler.registerFallingListener(this);
        /*
        SharedPreferences settings = getSharedPreferences(FallDetectionService.PREFS_NAME, 0);
        boolean isRunning = settings.getBoolean(FallDetectionService.PREFS_KEY_RUNNING,false);
        int fallen = settings.getInt(FallDetectionService.PREFS_KEY_FALLEN, 0);
        setCount(fallen);
        setRunning(isRunning);
        */
    }

    private TextView txtFallen;
    private TextView txtRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "create");
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                Log.d(TAG, "Inflated");
                txtFallen = (TextView) stub.findViewById(R.id.txtFallen);
                txtRunning = (TextView) stub.findViewById(R.id.txtRunning);
                SharedPreferences settings = getSharedPreferences(FallDetectionService.PREFS_NAME, 0);
                boolean isRunning = settings.getBoolean(FallDetectionService.PREFS_KEY_RUNNING, false);
                int fallen = settings.getInt(FallDetectionService.PREFS_KEY_FALLEN, 0);
                onRunningChanged(isRunning);
            }
        });
    }

    @Override
    public void hasFallen(int times) {
        String fallenFormat = getResources().getString(R.string.youhavefallen);
        String fallenMsg = String.format(fallenFormat, times);
        txtFallen.setText(fallenMsg);
        sendMsg(AppConstants.MSG_FALLEN);
    }

    @Override
    public void onRunningChanged(boolean running) {
        if (running) {
            txtRunning.setText(R.string.running);
        } else {
            txtRunning.setText(R.string.stopped);
        }
    }

    private boolean isWifiEnabled(){
        WifiManager wifiManager = (WifiManager) getSystemService(getApplicationContext().WIFI_SERVICE);
        Log.d(TAG, String.valueOf(wifiManager.getWifiState()));
        return wifiManager.isWifiEnabled();
    }

    private void sendMsg(String message){
        if (isWifiEnabled()){
            Log.d(TAG,"Send via Wifi");
            sendMsgWifi(message);
        }
        else
        {
            Log.d(TAG,"Send via phone");
        }
    }

    private void sendMsgWifi(final String message){
// Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = AppConstants.GCM_URL;

        final Map<String, String> mHeaders = new HashMap<String, String>();
        mHeaders.put("Authorization", "key=" + AppConstants.GCM_KEY);
        mHeaders.put("Content-Type", "application/json");


// Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.i(TAG, "Response is: " + response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i(TAG, "That didn't work!");
                        Log.d(TAG, String.valueOf(error));
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return mHeaders;
            }
            @Override
            public byte[] getBody() throws AuthFailureError {
                JSONObject obj = new JSONObject();
                JSONObject msg = new JSONObject();

                try {
                    msg.put("message",message);
                    obj.put("to", AppConstants.TOPIC_FALLENAPP);
                    obj.put("data", msg);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


/*

                String httpPostBody = "{\n" +
                        "    \"to\": \"/topics/global\",\n" +
                        "    \"data\": {\n" +
                        "    \"message\": \"Test\"\n" +
                        "  }\n" +
                        "}";
                // usually you'd have a field with some values you'd want to escape, you need to do it yourself if overriding getBody. here's how you do it

                */
                Log.i(TAG,obj.toString());

                return obj.toString().getBytes();
            }
        };
// Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}
