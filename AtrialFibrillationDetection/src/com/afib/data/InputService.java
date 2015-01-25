package com.afib.data;


import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.afib.ui.MainActivity;
import com.afib.ui.R;

public class InputService extends Service {
	private static final String LOG_TAG = "ForegroundService";
	private boolean mRunning;
	private Thread dataGet;
	
	@Override
	public void onCreate() {
		super.onCreate();
		mRunning = false;
	}
 
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {
			Log.i("InputService", "Received Start Foreground Intent ");
			Intent notificationIntent = new Intent(this, MainActivity.class);
			notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
			notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_CLEAR_TASK);
			PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
					notificationIntent, 0);
 
 
			Bitmap icon = BitmapFactory.decodeResource(getResources(),
					R.drawable.ic_launcher);
 
			Notification notification = new NotificationCompat.Builder(this)
					.setContentTitle("Atrial Fibrilation Detection")
					.setTicker("Atrial Fibrilation Detection")
					.setContentText("Data Streaming")
					.setSmallIcon(R.drawable.ic_launcher)
					.setLargeIcon(
							Bitmap.createScaledBitmap(icon, 128, 128, false))
					.setContentIntent(pendingIntent)
					.setOngoing(true)
					.build();
			startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
					notification);
			if(!mRunning)
			{
				final Service parameter = this;
			 dataGet = new Thread(new Runnable(){
				    public void run() {
				    // TODO Auto-generated method stub
				    while(true)
				    {
			           try {
			               Thread.sleep(5000);
			               Log.i("InputService", "Service Running Still");
				       		Intent i = new Intent("android.intent.action.MAIN").putExtra("some_msg", "Message Sent");
				    		parameter.sendBroadcast(i);
			           } catch (InterruptedException e) {
			               // TODO Auto-generated catch block
			               e.printStackTrace();
			               return;
			           }
				    }
	            }
				});
			 dataGet.start();
			}
			 mRunning = true;
		} else if (intent.getAction().equals(
				Constants.ACTION.STOPFOREGROUND_ACTION)) {
			Log.i(LOG_TAG, "Received Stop Foreground Intent");
			mRunning = false;
			dataGet.interrupt();
			stopForeground(true);
			stopSelf();
		}
		return START_STICKY;
	}
 
	@Override
	public void onDestroy() {
		mRunning = false;
		dataGet.interrupt();
		super.onDestroy();
		Log.i("InputService", "In onDestroy");
	}
 
	@Override
	public IBinder onBind(Intent intent) {
		// Used only in case of bound services.
		return null;
	}
}
