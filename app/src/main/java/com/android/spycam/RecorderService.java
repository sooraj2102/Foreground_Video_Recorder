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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Timer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.location.Location;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;


public class RecorderService extends Service implements SurfaceHolder.Callback,
		 MediaRecorder.OnInfoListener {

	private WindowManager windowManager;
	private SurfaceView surfaceView;
	private Camera camera = null;
	private MediaRecorder mediaRecorder = null;
	private SurfaceHolder surfHolder = null;

	private static boolean RECORD_AUDIO = true;

	private int NOTIFICATION_ID = 1234;
	private final String TAG = "ANDROID_" + getClass().getSimpleName();
	private String outputFile;
	private boolean VIDEO_RECORDER_ON = false;

	// used to access UI thread for toasts
	private Handler handler = new Handler(Looper.getMainLooper());
	private Context con;

	@Override
	public void onCreate() {
		con = this;

		// Create new SurfaceView, set its size to 1x1, move it to the top left
		// corner and set this service as a callback

		windowManager = (WindowManager) this
				.getSystemService(Context.WINDOW_SERVICE);
		surfaceView = CameraRecorder.surfaceView;
		/*new SurfaceView(getApplicationContext());
		LayoutParams layoutParams = new WindowManager.LayoutParams(1, 1,
		LayoutParams.TYPE_TOAST,
		LayoutParams.FLAG_NOT_FOCUSABLE,
		PixelFormat.TRANSLUCENT);
		layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
		windowManager.addView(surfaceView, layoutParams);
		surfHolder = surfaceView.getHolder();
		surfHolder.addCallback(this);*/
		//getAudioManagerStreams();
		surfHolder = surfaceView.getHolder();
		surfHolder.addCallback(this);
	}

	// Stop recording and remove SurfaceView
	@Override
	public void onDestroy() {

		Log.v(TAG, "RecorderService Service is being destroyed");
		stopMediaRecorder();
//		windowManager.removeView(surfaceView);
	}

	public int onStartCommand(Intent intent, int flags, int startID) {
		// gets email and access token from calling activity
			Helpers.createStopPauseNotification("Helios Background Video Recorder", "Stop", "Pause",
					this, RecorderService.class, NOTIFICATION_ID);
			startRecordingVideo(surfHolder);
		return START_STICKY;
	}
	@Override
	public void surfaceCreated(SurfaceHolder surfaceHolder) {
		Log.e(TAG,"Surface Created");
		surfHolder = surfaceHolder;
		startRecordingVideo(surfHolder);
	}

	private void startRecordingVideo(SurfaceHolder surfaceHolder) {
		Log.e(TAG,"Recording is going to start");
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new java.util.Date().getTime());

		outputFile = Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
				+ File.separator + "Hello_" + timeStamp + ".mp4";

		camera = getCameraInstance();
		if (camera == null) {
			Helpers.displayToast(handler, con , "Camera unavailable or in use",
					Toast.LENGTH_LONG);
			Helpers.createStopPlayNotification("Spy Camera Recorder", "Stop", "Play",
					this, RecorderService.class,NOTIFICATION_ID);
			stopMediaRecorder();
			return;
		}
		mediaRecorder = new MediaRecorder();
		camera.unlock();

		mediaRecorder.setCamera(camera);
		mediaRecorder.setOrientationHint(90);
		CamcorderProfile profile = getValidCamcorderProfile();

		if (RECORD_AUDIO) {
			mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
			int targetFrameRate = 15;

			mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
			mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
			mediaRecorder.setVideoFrameRate(targetFrameRate);
			mediaRecorder.setVideoSize(profile.videoFrameWidth,
					profile.videoFrameHeight);
			mediaRecorder.setVideoEncodingBitRate(profile.videoBitRate);
			mediaRecorder.setVideoEncoder(profile.videoCodec);
			mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
		}

		mediaRecorder.setOutputFile(outputFile);
		Log.d(TAG, "Saving file to " + outputFile);
		mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
		mediaRecorder.setMaxDuration(-1);
		mediaRecorder.setMaxFileSize(TokenFetcherTask.MAX_VIDEO_FILE_SIZE);
		mediaRecorder.setOnInfoListener(this);

		try {
			mediaRecorder.prepare();
			startMediaRecorder();
			VIDEO_RECORDER_ON = true;
		} catch (IllegalStateException e) {
			Log.d(TAG,
					"IllegalStateException preparing MediaRecorder: "
							+ e.getMessage());
			stopMediaRecorder();
		} catch (IOException e) {
			Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
			stopMediaRecorder();
		}
	}

	private void startMediaRecorder() {

		mediaRecorder.start();
		/*try {
			Thread.sleep(250);
		} catch (InterruptedException ie) {
		}*/
	}

	private void stopMediaRecorder() {

		try {
			if (VIDEO_RECORDER_ON) {
				mediaRecorder.stop();
				Log.v(TAG, "Stopped media recorder");
				mediaRecorder.release();
				camera.lock();
				camera.release();
				VIDEO_RECORDER_ON = false;
				Log.v(TAG, "Released camera and media recorder");
			}
		} catch (RuntimeException e) {
			// do nothing - happens if user pushed pause or stop button when
			// recording
			// was already stopped
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder surfaceHolder, int format,
							   int width, int height) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open(); // attempt to get a Camera instance
		} catch (Exception e) {
			// Camera is not available (in use or does not exist)
			Log.d(TAG, "Error opening camera");
		}
		return c; // returns null if camera is unavailable
	}

	private CamcorderProfile getValidCamcorderProfile() {
		CamcorderProfile profile;

		if (CamcorderProfile
				.hasProfile(CamcorderProfile.QUALITY_TIME_LAPSE_720P)) {
			profile = CamcorderProfile
					.get(CamcorderProfile.QUALITY_TIME_LAPSE_720P);
			return profile;
		}

		if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_720P))
			profile = CamcorderProfile.get(CamcorderProfile.QUALITY_720P);
		else
			profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);

		return profile;
	}

	public void onInfo(MediaRecorder mr, int what, int extra) {
		// called by MediaRecorder if we go over max file size
		if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED) {
			Log.d(TAG, "MediaRecorder hit max file size");

			stopMediaRecorder();

			startRecordingVideo(surfHolder);
		} else { // log the event
			Log.d(TAG, " MediaRecorder.onInfo called with " + what + " extra "
					+ extra);
		}
	}
}