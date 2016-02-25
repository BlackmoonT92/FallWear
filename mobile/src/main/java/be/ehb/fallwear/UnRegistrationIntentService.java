package be.ehb.fallwear;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;


/**
 * Created by davy.van.belle on 11/02/2016.
 */

public class UnRegistrationIntentService extends IntentService {
    private static final String TAG = "UnRegistration";

    public UnRegistrationIntentService() {
        super(TAG);
        Log.i(TAG, "Constructor");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "GCM UnRegistration Token");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            String token = sharedPreferences.getString(QuickstartPreferences.TOKEN, "");
            unsubscribeTopics(token);

            InstanceID instanceID = InstanceID.getInstance(this);
            instanceID.deleteInstanceID();



            // Subscribe to topic channels


            // You should store a boolean that indicates whether the generated token has been
            // sent to your server. If the boolean is false, send the token to your server,
            // otherwise your server should have already received the token.
            sharedPreferences.edit().putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false).apply();
            sharedPreferences.edit().putString(QuickstartPreferences.TOKEN,"12").apply();
            // [END register_for_gcm]
        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            sharedPreferences.edit().putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false).apply();
        }
        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent unregistrationComplete = new Intent(QuickstartPreferences.UNREGISTRATION_COMPLETE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(unregistrationComplete);
    }

    private void unsubscribeTopics(String token) throws IOException {
        GcmPubSub pubSub = GcmPubSub.getInstance(this);
        pubSub.unsubscribe(token, QuickstartPreferences.TOPIC_GLOBAL);
        pubSub.unsubscribe(token, QuickstartPreferences.TOPIC_FALLENAPP);
    }
}
