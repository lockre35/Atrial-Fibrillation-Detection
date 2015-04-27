package com.afib.communication;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.afib.data.OutputThread;
import com.afib.graph.GraphThread;
import com.afib.ui.MainActivity;
import com.afib.ui.R;


/**
 * InputService extends Service to allow for operations to occur in the background
 * on a user's device even if the main application is closed.  This class also 
 * adds a notification on the menu bar of a device once the service is started.
 * 
 * @author Logan
 */
public class InputService extends Service {
	private static final String LOG_TAG = "ForegroundService";
    private String OutputFileName;
	private boolean isThreadRunning;
	private Thread outputThread;
    private GattService GattService;
    private BlockingQueue DataQueue;
    public Notification notification;
    public boolean BLEDisconnected = false;
	

	/**
	 * Override the onCreate method of Service so that we know the thread is not running
	 * yet.
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		isThreadRunning = false;
	}
 
	/**
	 * Override the onStartCommand so that we can start all needed operations and create the
	 * notification for this service.
	 * 
	 * @param intent stores passed to the service
	 * @param flags with additional information
	 * @param startId request to start
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		//If we are starting the service
		if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {
            DataQueue = new ArrayBlockingQueue(1000);
            Log.i("InputService", "Received Start Foreground Intent ");
            OutputFileName = intent.getStringExtra(Constants.ACTION.OUTPUT_FILENAME);
            //Initialize the gatt service to communicate with the ECG
            GattService = new GattService(intent.getStringExtra(Constants.ACTION.DEVICE_ADDRESS), this, DataQueue);

            Log.i("InputService", "Device Address: " + GattService.mBluetoothDeviceAddress);

            if (!GattService.initialize()) {
                Log.e("InputService", "Unable to initialize Bluetooth");
                isThreadRunning = false;
                stopForeground(true);
                stopSelf();
            }
            GattService.connect(GattService.mBluetoothDeviceAddress);

            //Create an intent for the notification to return to (MainActivity.class)
            Intent notificationIntent = new Intent(this, MainActivity.class);
            notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, 0);

            //Create notification
            Bitmap icon = BitmapFactory.decodeResource(getResources(),
                    R.drawable.ic_launcher);
            notification = new NotificationCompat.Builder(this)
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

            //If the thread is not running (first time starting the service)
            if (!isThreadRunning) {
                outputThread = new OutputThread(OutputFileName, DataQueue);
                outputThread.start();
                isThreadRunning = true;
            }
            isThreadRunning = true;
        //If checking service status
        } else if (intent.getAction().equals(Constants.ACTION.CHECK_STATUS)) {
            Log.i(LOG_TAG, "Received check status Intent");
            //Check the status of the file store thread
            boolean threadStatus = outputThread.isAlive() && isThreadRunning;
            Intent i = new Intent("android.intent.action.MAIN").putExtra(Constants.ACTION.STREAM_STATUS, threadStatus);
            this.sendBroadcast(i);
        } else if(intent.getAction().equals(Constants.ACTION.BLE_DISCONNECTED)){
            Intent i = new Intent("android.intent.action.MAIN").putExtra(Constants.ACTION.BLE_DISCONNECTED, BLEDisconnected);
            this.sendBroadcast(i);
		//If stopping the service
		} else if (intent.getAction().equals(Constants.ACTION.STOPFOREGROUND_ACTION)) {
			//Stop the thread and stop the service
			Log.i(LOG_TAG, "Received Stop Foreground Intent");
			Kill();
            stopForeground(true);
            stopSelf();
		}
		
		//Return as foreground service (makes it harder for the system to destroy the service)
		return START_STICKY;
	}
 
	/**
	 * Override onDestroy of Service so that we can stop the thread running from
	 * this service.
	 */
	@Override
	public void onDestroy() {
		isThreadRunning = false;
		//dataGet.interrupt();

		// After using a given device, you should make sure that
		// BluetoothGatt.close() is called
		// such that resources are cleaned up properly.
		GattService.close();
		super.onDestroy();
		Log.i("InputService", "In onDestroy");
	}
	
	/**
	 * Necessary override
	 */
	@Override
	public IBinder onBind(Intent intent) {
		// Used only in case of bound services.
		return null;
	}

    public void Kill()
    {
        isThreadRunning = false;
        GattService.close();
        //dataGet.interrupt();
        try{
            if(outputThread.isAlive())
                DataQueue.put("Terminate Thread".getBytes());
        }catch(InterruptedException e){
            e.printStackTrace();
        }
        BLEDisconnected = true;
    }
}
