/*
 * ConnectBot: simple, powerful, open-source SSH client for Android
 * Copyright 2025 Your Name
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.connectbot.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.connectbot.bean.HostBean;
import org.connectbot.util.HostDatabase;

import java.util.List;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "CB.BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Received boot completed intent");

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // Start the terminal manager service to handle auto-connections
            Intent serviceIntent = new Intent(context, TerminalManager.class);
            context.startForegroundService(serviceIntent);
            
            // Schedule a delayed connection attempt after the system is fully ready
            Intent delayedIntent = new Intent(context, DelayedBootConnectService.class);
            context.startService(delayedIntent);
        }
    }
}