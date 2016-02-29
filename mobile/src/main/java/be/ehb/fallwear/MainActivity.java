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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class MainActivity extends AppCompatActivity {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "MainActivity";

    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private BroadcastReceiver mUnRegisterBroadcastReceiver;
    private ProgressBar mRegistrationProgressBar;
    private TextView mInformationTextView;
    private Button mBtnReg;
    private Button mBtnUnReg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRegistrationProgressBar = (ProgressBar) findViewById(R.id.registrationProgressBar);
        mBtnReg = (Button) findViewById(R.id.btnReg);
        mBtnUnReg = (Button) findViewById(R.id.bntUnReg);


        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mRegistrationProgressBar.setVisibility(ProgressBar.GONE);
                mBtnReg.setVisibility(View.GONE);
                mBtnUnReg.setVisibility(View.VISIBLE);
                mBtnReg.setEnabled(true);
                mInformationTextView.setVisibility(View.VISIBLE);



                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                String token = sharedPreferences
                        .getString(QuickstartPreferences.TOKEN,"00");
                if (sentToken) {
                    String message = getResources().getString(R.string.gcm_send_message);
                    mInformationTextView.setText(String.format(message, token));
                } else {
                    mInformationTextView.setText(getString(R.string.token_error_message));
                }
            }
        };


        mUnRegisterBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mRegistrationProgressBar.setVisibility(ProgressBar.GONE);
                mInformationTextView.setVisibility(View.VISIBLE);
                mBtnReg.setVisibility(View.VISIBLE);
                mBtnUnReg.setVisibility(View.GONE);
                mBtnUnReg.setEnabled(true);
                mInformationTextView.setText(R.string.unregistered);
            }
        };

        mInformationTextView = (TextView) findViewById(R.id.informationTextView);

    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
        LocalBroadcastManager.getInstance(this).registerReceiver(mUnRegisterBroadcastReceiver,
                new IntentFilter(QuickstartPreferences.UNREGISTRATION_COMPLETE));

        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);

        boolean regTopic = sharedPreferences.
                getBoolean(QuickstartPreferences.REGISTRATION_TOPICS, false);

        if (regTopic){
            mRegistrationBroadcastReceiver.onReceive(this,null);
        }
        else
        {
            mUnRegisterBroadcastReceiver.onReceive(this,null);
        }
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mUnRegisterBroadcastReceiver);
        super.onPause();
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    public void unRegClick(View view){
        if (checkPlayServices()) {
            Log.i(TAG,"UnReg Clicked");
            mRegistrationProgressBar.setVisibility(ProgressBar.VISIBLE);
            mBtnUnReg.setEnabled(false);
            mInformationTextView.setText(getString(R.string.unregistering_message));

            Intent intent = new Intent(this, UnRegistrationIntentService.class);
            startService(intent);
        }
    }

    public void regClick(View view){
        // register topics on GCM
        mRegistrationProgressBar.setVisibility(ProgressBar.VISIBLE);
        mInformationTextView.setVisibility(View.VISIBLE);
        mBtnReg.setEnabled(false);
        mInformationTextView.setText(getString(R.string.registering_message));

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
    }

}
