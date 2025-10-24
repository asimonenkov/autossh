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

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import org.connectbot.bean.HostBean;
import org.connectbot.util.HostDatabase;

import java.util.List;

public class DelayedBootConnectService extends Service {
    private static final String TAG = "CB.DelayedBootConnectService";
    private static final int DELAY_MILLIS = 10000; // 10 seconds delay

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Starting delayed boot connect service");

        // Use a handler to delay the connection attempt
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                performAutoConnections();
                stopSelf();
            }
        }, DELAY_MILLIS);

        return START_NOT_STICKY;
    }

    private void performAutoConnections() {
        Log.d(TAG, "Performing auto connections for hosts with auto-connect enabled");

        HostDatabase hostdb = HostDatabase.get(this);
        List<HostBean> hosts = hostdb.getHosts(false); // Don't sort by color

        // Start the TerminalManager service first to ensure it's running
        Intent serviceIntent = new Intent(this, TerminalManager.class);
        startForegroundService(serviceIntent);
        
        // Wait a bit for the service to start, then perform connections
        try {
            Thread.sleep(3000); // Wait 3 seconds for service to initialize
            
            // Try to get the instance multiple times with delay if needed
            TerminalManager terminalManager = null;
            int attempts = 0;
            while (terminalManager == null && attempts < 10) { // Max 10 attempts
                terminalManager = TerminalManager.getInstance();
                if (terminalManager == null) {
                    Log.d(TAG, "Waiting for TerminalManager to initialize... attempt " + (attempts + 1));
                    Thread.sleep(1000); // Wait 1 second before next attempt
                    attempts++;
                }
            }
            
            if (terminalManager != null) {
                for (HostBean host : hosts) {
                    if (host.getAutoConnect()) {
                        Log.d(TAG, "Attempting to auto-connect to: " + host.getNickname());
                        try {
                            terminalManager.openConnection(host);
                        } catch (Exception e) {
                            Log.e(TAG, "Failed to auto-connect to: " + host.getNickname(), e);
                        }
                    }
                }
            } else {
                Log.e(TAG, "TerminalManager is still null after waiting, cannot perform auto connections");
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted while waiting for TerminalManager", e);
        }
    }
}