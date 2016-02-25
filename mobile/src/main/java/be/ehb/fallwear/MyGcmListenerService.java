/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package be.ehb.fallwear;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.gcm.GcmListenerService;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";


    final String senderId = "DAvY";
    final String msgId = "482039824189" ;


    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + message);

        if (from.startsWith(QuickstartPreferences.TOPIC_FALLENAPP)) {
            Log.d(TAG, "from fallen");
            if (QuickstartPreferences.MSG_FALLEN.equals(message)){
                Log.d(TAG, "has fallen");
                Intent startIntent = new Intent(this,IncommingAlarm.class);
                startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(startIntent);
            }
            else
            {
                Log.d(TAG, "has not fallen");
            }

        } else {
            Log.d(TAG, "Not from fallen");
        }

        // [START_EXCLUDE]
        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
        //sendNotification(message);
        // [END_EXCLUDE]
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String message) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_ic_notification)
                .setContentTitle("GCM Message")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    /*

    private void sendMsg(){
        final GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
         String msg = "";
                try {
                    Bundle data = new Bundle();
                    data.putString("my_message", "Hello World");
                    data.putString("my_action", "SAY_HELLO");
                    gcm.send(getString(R.string.gcm_defaultSenderId), msgId, data);
                    msg = "Sent message";
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }

        Log.i(TAG, msg);
    }

    */

    private void sendMsgHTTP(){
// Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://android.googleapis.com/gcm/send";

        final Map<String, String> mHeaders = new HashMap<String, String>();
        mHeaders.put("Authorization", "key=AIzaSyDYOIqybD8-kr76NU9bRdUYMtrWX2vlySk");
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
                    msg.put("message","Hello");
                    obj.put("to", "/topics/global");
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
