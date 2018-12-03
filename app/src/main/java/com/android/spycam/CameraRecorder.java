/*
 * Copyright (c) 2015, Picker Weng
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 *  Neither the name of CameraRecorder nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Project:
 *     CameraRecorder
 *
 * File:
 *     CameraRecorder.java
 *
 * Author:
 *     Picker Weng (pickerweng@gmail.com)
 */

package com.android.spycam;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

public class CameraRecorder extends Activity implements SurfaceHolder.Callback {

	private static final String TAG = CameraRecorder.class.getSimpleName();

	public static Camera mCamera;
	public static SurfaceView surfaceView;
	public SurfaceHolder surfaceHolder;
	public static String CHANNEL_ID = "asdfbdg";
	public static boolean mPreviewRunning;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        surfaceView = findViewById(R.id.surfaceView1);
        surfaceHolder = surfaceView.getHolder();
        createNotificationChannel();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.addCallback(this);
		Button btnStart = (Button) findViewById(R.id.StartService);
		btnStart.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				Intent intent = new Intent(CameraRecorder.this, RecorderService.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startForegroundService(intent);
				//finish();
			}
		});

		Button btnStop = (Button) findViewById(R.id.StopService);
		btnStop.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				Intent intent = new Intent(CameraRecorder.this, RecorderService.class);
                stopService(intent);
			}
		});
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }
	private void createNotificationChannel() {
		// Create the NotificationChannel, but only on API 26+ because
		// the NotificationChannel class is new and not in the support library
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			CharSequence name = "SPY CAMERA NOTI CHANNEL";
			String description = "this is for a foreground notification";
			int importance = NotificationManager.IMPORTANCE_DEFAULT;
			NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
			channel.setDescription(description);
			// Register the channel with the system; you can't change the importance
			// or other notification behaviors after this
			NotificationManager notificationManager = getSystemService(NotificationManager.class);
			notificationManager.createNotificationChannel(channel);
		}
	}

	@Override
	protected void onResume() {
    	super.onResume();
    	Log.e("On Resume", "Your Activity is in onResume state");
    	if(surfaceView == null)
			surfaceView = findViewById(R.id.surfaceView1);
    	if(surfaceHolder == null)
			surfaceHolder = surfaceView.getHolder();
	}
}
