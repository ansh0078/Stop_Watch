package com.sambhav2358.testing;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class TimerService extends Service {
    long seconds = 0L;
    boolean notificationJustStarted = true;
    public static Handler timerHandler = new Handler();
    public static Runnable timerRunnable;
    private final String CHANNEL_ID = "Channel_id";
    NotificationManager mNotificationManager;
    int prevSeconds;
    NotificationCompat.Builder timerNotificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID).setContentTitle(CHANNEL_ID);

    @SuppressLint("InlinedApi")
    @Override
    public void onCreate() {
        super.onCreate();
        String TAG = "Timer Service";
        Log.d(TAG, "onCreate: started service");

        // This is used to update the notification
        startForeground(1, new NotificationCompat.Builder(TimerService.this, createChannel()).setContentTitle("Goal In Progress").setPriority(NotificationManager.IMPORTANCE_MAX).build());
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        PrefUtil.setIsRunningInBackground(this, true);
        PrefUtil.setWasTimerRunning(this, true);

        String goalName = "Sample Goal";
        notificationJustStarted = true;

        prevSeconds = (int) PrefUtil.getTimerSecondsPassed(TimerService.this);

        seconds = prevSeconds;

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                // add a second to the counter
                seconds++;

                //update the notification with that second
                updateNotification(goalName, seconds);

                //Print the seconds
                Log.d("timerCount", seconds + "");

                //Save the seconds passed to shared preferences
                PrefUtil.setTimerSecondsPassed(TimerService.this,seconds);

                //Re-run this action
                timerHandler.postDelayed(this, 1000);
            }
        };

        //Start the stop watch
        timerHandler.postDelayed(timerRunnable, 0);

        return START_STICKY;
    }
    @SuppressLint("NewApi")
    public void updateNotification(String goalName, long seconds) {
        if (notificationJustStarted) {
            Intent notificationIntent = new Intent(this, MainActivity.class);
            @SuppressLint("InlinedApi") PendingIntent pendingIntent = PendingIntent.getActivity(this,
                    0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
            timerNotificationBuilder.setContentTitle("Goal In Progress")
                    .setOngoing(true)
                    .setContentIntent(pendingIntent)
                    .setOnlyAlertOnce(true)
                    .setOngoing(true)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setAutoCancel(true)
                    .addAction(createNotificationActionButton("START", "s"))
                    .addAction(createNotificationActionButton("PAUSE", "p"))
                    .addAction(createNotificationActionButton("RESET", "r"));
            notificationJustStarted = false;
        }

        long minutes = TimeUnit.SECONDS.toMinutes(seconds);
        String mins = String.valueOf(minutes).length() == 2 ? minutes + "" : "0" + minutes;
        String time = mins + ":" + (String.valueOf(seconds - TimeUnit.MINUTES.toSeconds(minutes)).length() == 2 ? (seconds - TimeUnit.MINUTES.toSeconds(minutes)) : "0" + (seconds - TimeUnit.MINUTES.toSeconds(minutes)));

        timerNotificationBuilder.setContentText("Stop watch is running.\n Time elapsed: " + time);


        startForeground(1, timerNotificationBuilder.build());
    }

    @Override
    public void onDestroy() {
        timerHandler.removeCallbacks(timerRunnable);
        PrefUtil.setTimerSecondsPassed(this, seconds);
        PrefUtil.setIsRunningInBackground(this, false);
        super.onDestroy();
    }

    public NotificationCompat.Action createNotificationActionButton(String text, String actionName){

        if (actionName.equals("r")) {
            Intent intent = new Intent(this, MainActivity.class).putExtra("action", actionName);

            @SuppressLint("InlinedApi") PendingIntent pendingIntent = PendingIntent.getActivity(this, new Random().nextInt(100), intent, PendingIntent.FLAG_MUTABLE);

            return new NotificationCompat.Action(0, text, pendingIntent);
        }else {

            Intent intent = new Intent(this, StopwatchNotificationActionReceiver.class).putExtra("action", actionName);
            @SuppressLint("InlinedApi") PendingIntent pendingIntent = PendingIntent.getBroadcast(this, new Random().nextInt(100), intent, PendingIntent.FLAG_MUTABLE);

            return new NotificationCompat.Action(0, text, pendingIntent);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @NonNull
    @TargetApi(26)
    private synchronized String createChannel() {
        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        String name = "STOPWATCH";
        int importance = NotificationManager.IMPORTANCE_LOW;

        NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);

        mChannel.setName("Notifications");

        if (mNotificationManager != null) {
            mNotificationManager.createNotificationChannel(mChannel);
        } else {
            stopSelf();
        }

        return CHANNEL_ID;
    }
}
