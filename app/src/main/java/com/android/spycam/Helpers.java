package com.android.spycam;


import java.util.HashMap;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import static com.android.spycam.CameraRecorder.CHANNEL_ID;

// Miscellaneous support functions used by various classes
class Helpers {

    private Helpers(){}	// not to be instantiated


    static void displayToast(Handler handler, final Context con, final String text, final int toast_length){
        // helper method uses the main thread to display a toast
        // we use this because if this class is used by a Service
        // as opposed to an Activity, we can't access the UI thread
        // in the normal way using RunOnUIThread
        handler.post(new Runnable(){
            public void run(){
                Toast.makeText(con, text, toast_length).show();
            }
        });
    }


    static void createStopPauseNotification(String title, String stopText, String pauseText,
                                            Service con, Class<?> serviceClass, int NOTIFICATION_ID) {

        PendingIntent stopIntent = PendingIntent
                .getService(con, 0, getIntent(TokenFetcherTask.REQUEST_TYPE_STOP, con, serviceClass),
                        PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent pauseIntent = PendingIntent.getService(con, 1,
                getIntent(TokenFetcherTask.REQUEST_TYPE_PAUSE, con, serviceClass),
                PendingIntent.FLAG_CANCEL_CURRENT);
        Log.e("Notification TAG","Start foreground service to avoid unexpected kill");
        // Start foreground service to avoid unexpected kill
        Notification.Builder notification;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
             notification = new Notification.Builder(con, CHANNEL_ID);
         }
         else{
             notification = new Notification.Builder(con);

         }

                Notification no = notification.setContentTitle(title)
                .setContentText("SPY NOTI").setSmallIcon(R.drawable.eye)
                .addAction(R.drawable.pause, pauseText, pauseIntent)
                .addAction(R.drawable.stop, stopText, stopIntent).build();
        con.startForeground(NOTIFICATION_ID, no);
    }

    static void createStopPlayNotification(String title, String stopText, String playText,
                                           Service con, Class<?> serviceClass, int NOTIFICATION_ID) {

        PendingIntent stopIntent = PendingIntent
                .getService(con, 0, getIntent(TokenFetcherTask.REQUEST_TYPE_STOP, con, serviceClass),
                        PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent playIntent = PendingIntent
                .getService(con, 2, getIntent(TokenFetcherTask.REQUEST_TYPE_PLAY, con, serviceClass),
                        PendingIntent.FLAG_CANCEL_CURRENT);

        // Start foreground service to avoid unexpected kill
        Notification notification = new Notification.Builder(con)
                .setContentTitle(title)
                .setContentText("")
                .addAction(R.drawable.play, playText, playIntent)
                .addAction(R.drawable.stop, stopText, stopIntent).build();
        con.startForeground(NOTIFICATION_ID, notification);
    }

    static Intent getIntent(String requestType, Context con, Class<?> serviceClass) {
        Intent intent = new Intent(con, serviceClass);
        intent.putExtra(TokenFetcherTask.REQUEST_TYPE, requestType);


        return intent;

    }

}