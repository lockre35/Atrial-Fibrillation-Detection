package com.afib.data;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.afib.ui.MainActivity;
import com.afib.ui.R;
import com.afib.communication.*;


/**
 * InputService extends Service to allow for operations to occur in the background
 * on a user's device even if the main application is closed.  This class also 
 * adds a notification on the menu bar of a device once the service is started.
 * 
 * @author Logan
 */
public class InputService extends Service {
	private static final String LOG_TAG = "ForegroundService";
	private boolean isThreadRunning;
	private Thread dataGet;
	
	
	private BluetoothManager mBluetoothManager;
	private BluetoothAdapter mBluetoothAdapter;
	private String mBluetoothDeviceAddress;
	private BluetoothGatt mBluetoothGatt;
	private Map<UUID, BluetoothGattCharacteristic> map = new HashMap<UUID, BluetoothGattCharacteristic>();

	public final static String ACTION_GATT_CONNECTED = "ACTION_GATT_CONNECTED";
	public final static String ACTION_GATT_DISCONNECTED = "ACTION_GATT_DISCONNECTED";
	public final static String ACTION_GATT_SERVICES_DISCOVERED = "ACTION_GATT_SERVICES_DISCOVERED";
	public final static String ACTION_GATT_RSSI = "ACTION_GATT_RSSI";
	public final static String ACTION_DATA_AVAILABLE = "ACTION_DATA_AVAILABLE";
	public final static String EXTRA_DATA = "EXTRA_DATA";
	
	public int testDataCount = 0;
	public long startTime = 0;

	public final static UUID UUID_BLE_SHIELD_TX = UUID
			.fromString(GattAttributes.BLE_SHIELD_TX);
	public final static UUID UUID_BLE_SHIELD_RX = UUID
			.fromString(GattAttributes.BLE_SHIELD_RX);
	public final static UUID UUID_BLE_SHIELD_SERVICE = UUID
			.fromString(GattAttributes.BLE_SHIELD_SERVICE);

	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status,
				int newState) {
			String intentAction;

			if (newState == BluetoothProfile.STATE_CONNECTED) {
				intentAction = ACTION_GATT_CONNECTED;
				broadcastUpdate(intentAction);
				Log.i("InputService", "Connected to GATT server.");
				// Attempts to discover services after successful connection.
				Log.i("InputService", "Attempting to start service discovery:"
						+ mBluetoothGatt.discoverServices());
			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				intentAction = ACTION_GATT_DISCONNECTED;
				Log.i("InputService", "Disconnected from GATT server.");
				broadcastUpdate(intentAction);
			}
		}

		public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				broadcastUpdate(ACTION_GATT_RSSI, rssi);
			} else {
				Log.w("InputService", "onReadRemoteRssi received: " + status);
			}
		};

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
			} else {
				Log.w("InputService", "onServicesDiscovered received: " + status);
			}
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			Log.i("InputService", "Action: " + "Data Available");
			if (status == BluetoothGatt.GATT_SUCCESS) {
				broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
			}
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic) {
			broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
		}
	};
	
	private void broadcastUpdate(final String action) {
		final Intent intent = new Intent(action);
		getGattService(getSupportedGattService());
		sendBroadcast(intent);
	}

	private void broadcastUpdate(final String action, int rssi) {
		final Intent intent = new Intent(action);
		intent.putExtra(EXTRA_DATA, String.valueOf(rssi));
		sendBroadcast(intent);
	}

	private void broadcastUpdate(final String action,
			final BluetoothGattCharacteristic characteristic) {
		final Intent intent = new Intent(action);
		// This is special handling for the Heart Rate Measurement profile. Data
		// parsing is
		// carried out as per profile specifications:
		// http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
		if (UUID_BLE_SHIELD_RX.equals(characteristic.getUuid())) {
			final byte[] rx = characteristic.getValue();
			testDataCount++;
			//Log.i("InputService", "Data: " + new String(rx));
			intent.putExtra(EXTRA_DATA, rx);
		}

		if(testDataCount > 1000)
		{
			long endTime = 0;
			endTime = System.currentTimeMillis();
			Log.i("InputService", "TotalTime: " + (endTime - startTime));
			close();
		}
		
		sendBroadcast(intent);
	}
	
	private void getGattService(BluetoothGattService gattService) {
		if (gattService == null)
			return;

		BluetoothGattCharacteristic characteristic = gattService
				.getCharacteristic(UUID_BLE_SHIELD_TX);
		map.put(characteristic.getUuid(), characteristic);

		BluetoothGattCharacteristic characteristicRx = gattService
				.getCharacteristic(UUID_BLE_SHIELD_RX);
		setCharacteristicNotification(characteristicRx,
				true);
		readCharacteristic(characteristicRx);
		
	}
	
	/**
	 * Initializes a reference to the local Bluetooth adapter.
	 * 
	 * @return Return true if the initialization is successful.
	 */
	public boolean initialize() {
		// For API level 18 and above, get a reference to BluetoothAdapter
		// through
		// BluetoothManager.
		if (mBluetoothManager == null) {
			mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			if (mBluetoothManager == null) {
				Log.e("InputService", "Unable to initialize BluetoothManager.");
				return false;
			}
		}

		mBluetoothAdapter = mBluetoothManager.getAdapter();
		if (mBluetoothAdapter == null) {
			Log.e("InputService", "Unable to obtain a BluetoothAdapter.");
			return false;
		}

		return true;
	}
	

	/**
	 * Connects to the GATT server hosted on the Bluetooth LE device.
	 * 
	 * @param address
	 *            The device address of the destination device.
	 * 
	 * @return Return true if the connection is initiated successfully. The
	 *         connection result is reported asynchronously through the
	 *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
	 *         callback.
	 */
	public boolean connect(final String address) {
		if (mBluetoothAdapter == null || address == null) {
			Log.w("InputService",
					"BluetoothAdapter not initialized or unspecified address.");
			return false;
		}

		// Previously connected device. Try to reconnect.
		if (mBluetoothDeviceAddress != null
				&& address.equals(mBluetoothDeviceAddress)
				&& mBluetoothGatt != null) {
			Log.d("InputService",
					"Trying to use an existing mBluetoothGatt for connection.");
			if (mBluetoothGatt.connect()) {
				return true;
			} else {
				return false;
			}
		}

		final BluetoothDevice device = mBluetoothAdapter
				.getRemoteDevice(address);
		if (device == null) {
			Log.w("InputService", "Device not found.  Unable to connect.");
			return false;
		}
		// We want to directly connect to the device, so we are setting the
		// autoConnect
		// parameter to false.
		mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
		Log.d("InputService", "Trying to create a new connection.");
		mBluetoothDeviceAddress = address;

		return true;
	}

	/**
	 * Disconnects an existing connection or cancel a pending connection. The
	 * disconnection result is reported asynchronously through the
	 * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
	 * callback.
	 */
	public void disconnect() {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w("InputService", "BluetoothAdapter not initialized");
			return;
		}
		mBluetoothGatt.disconnect();
	}

	/**
	 * After using a given BLE device, the app must call this method to ensure
	 * resources are released properly.
	 */
	public void close() {
		if (mBluetoothGatt == null) {
			return;
		}
		mBluetoothGatt.close();
		mBluetoothGatt = null;
	}
	

	/**
	 * Request a read on a given {@code BluetoothGattCharacteristic}. The read
	 * result is reported asynchronously through the
	 * {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
	 * callback.
	 * 
	 * @param characteristic
	 *            The characteristic to read from.
	 */
	public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w("InputService", "BluetoothAdapter not initialized");
			return;
		}

		mBluetoothGatt.readCharacteristic(characteristic);
	}

	public void readRssi() {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w("InputService", "BluetoothAdapter not initialized");
			return;
		}

		mBluetoothGatt.readRemoteRssi();
	}

	public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w("InputService", "BluetoothAdapter not initialized");
			return;
		}

		mBluetoothGatt.writeCharacteristic(characteristic);
	}

	/**
	 * Enables or disables notification on a give characteristic.
	 * 
	 * @param characteristic
	 *            Characteristic to act on.
	 * @param enabled
	 *            If true, enable notification. False otherwise.
	 */
	public void setCharacteristicNotification(
			BluetoothGattCharacteristic characteristic, boolean enabled) {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w("InputService", "BluetoothAdapter not initialized");
			return;
		}
		mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

		if (UUID_BLE_SHIELD_RX.equals(characteristic.getUuid())) {
			BluetoothGattDescriptor descriptor = characteristic
					.getDescriptor(UUID
							.fromString(GattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
			descriptor
					.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
			mBluetoothGatt.writeDescriptor(descriptor);
		}
	}

	/**
	 * Retrieves a list of supported GATT services on the connected device. This
	 * should be invoked only after {@code BluetoothGatt#discoverServices()}
	 * completes successfully.
	 * 
	 * @return A {@code List} of supported services.
	 */
	public BluetoothGattService getSupportedGattService() {
		if (mBluetoothGatt == null)
			return null;

		return mBluetoothGatt.getService(UUID_BLE_SHIELD_SERVICE);
	}

	
	
	
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
			
			Log.i("InputService", "Received Start Foreground Intent ");
			
			mBluetoothDeviceAddress = intent.getStringExtra(Constants.ACTION.DEVICE_ADDRESS);
			Log.i("InputService", "Device Address: " + mBluetoothDeviceAddress);
			
			if (!initialize()) {
				Log.e("InputService", "Unable to initialize Bluetooth");
				isThreadRunning = false;
				stopForeground(true);
				stopSelf();
			}
			startTime = System.currentTimeMillis();
			connect(mBluetoothDeviceAddress);
			
			//mDeviceName = intent.getStringExtra(Constants.ACTION.DEVICE_NAME);
			
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
			
			//If the thread is not running (first time starting the service)
			if(!isThreadRunning)
			{
//				//Service parameter to pass to this thread
//				final Service parameter = this;
//
//				dataGet = new Thread(new Runnable(){
//				    public void run() {
//				    	InputStream dataInputStream = null;
//				    	BufferedReader br = null;
//				    	
//				    	FileOutputStream localOutputStream = null;
//				    	
//				        try {
//				        	dataInputStream = parameter.getAssets().open("scope_32.csv");
//				        	br = new BufferedReader(new InputStreamReader(dataInputStream));
//				        	
//				        	String path = parameter.getExternalFilesDir(null).getAbsolutePath();
//				        	File file = new File(path + "/afib_recording.txt");
//				        	//Log.i("InputService",path);
//				        	localOutputStream = new FileOutputStream(file);
//				        	/*try {
//				        	    stream.write("testing".getBytes());
//				        	} finally {
//				        	    stream.close();
//				        	}*/
//				        }
//				        catch (Exception e) {
//				            Log.e("Exception", "File write failed: " + e.toString());
//				        }
//					    // TODO Auto-generated method stub
//					    while(true)
//					    {
//				           try {
//				               //Thread.sleep(5000);
//				               //Log.i("InputService", "Service Running Still");
//				               String strLine = br.readLine();
//				               localOutputStream.write((strLine).getBytes());
//				               //Send a message to broadcast receivers
//				               Intent i = new Intent("android.intent.action.MAIN").putExtra("some_msg", strLine);
//				               parameter.sendBroadcast(i);
//				               
//				           } catch (Exception e) {
//				               // TODO Auto-generated catch block
//				               e.printStackTrace();
//				               //Kill the thread
//				               return;
//				           }
//					    }
//	            }
//				});
//				
//				//Start this thread
//				dataGet.start();
			}
			isThreadRunning = true;
		//If stopping the service
		} else if (intent.getAction().equals(Constants.ACTION.STOPFOREGROUND_ACTION)) {
			//Stop the thread and stop the service
			Log.i(LOG_TAG, "Received Stop Foreground Intent");
			isThreadRunning = false;
			close();
			//dataGet.interrupt();
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
		close();
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
}
