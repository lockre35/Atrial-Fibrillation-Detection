package com.afib.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.afib.communication.Constants;
import com.afib.communication.InputService;


import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;




/**
 * FindDeviceFragment extends Fragment to present a list of available
 * BLE devices to users.
 * 
 * @author Logan
 */
public class FindDeviceFragment extends Fragment implements OnItemClickListener {
	
	private List<Map<String, String>> listItems = new ArrayList<Map<String, String>>();
	private ListView listView;
	private SimpleAdapter adapter;
	private Map<String, String> map = null;
	private Button scanButton;
	
	//Properties used to handle bluetooth devices
	//private GattService mBluetoothLeService;
	private BluetoothAdapter mBluetoothAdapter;
	private ArrayList<BluetoothDevice> devices;
	private String mDeviceAddress;
	private String mDeviceName;
    private ProgressDialog progressDialog;
	
	private String DEVICE_NAME = "name";
	private String DEVICE_ADDRESS = "address";
	public final static String EXTRA_DEVICE_ADDRESS = "EXTRA_DEVICE_ADDRESS";
	public final static String EXTRA_DEVICE_NAME = "EXTRA_DEVICE_NAME";
	private static final int REQUEST_ENABLE_BT = 1;
	private static final long SCAN_PERIOD = 1500;
	public static final int REQUEST_CODE = 30;
	public static final int RESULT_CODE = 31;
    public final static int FILE_SELECTED_CODE = 999;
    public final static String FILE_NAME = "FILE_NAME";
    public static final int DIALOG_FRAGMENT = 1;
	
	
	/**
	 * Override the onCreateView of Fragment so that we can initialize necessary information.
	 * More information about the Fragment life cycle can be found at the following link,
	 * <a href="http://www.tutorialspoint.com/android/android_fragments.htm">.
	 * 
	 * @param inflater used to load the fragment view
	 * @param container where the fragment will be loaded
	 * @param savedInstanceState used to recreate an existing activity
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		//Initialize the view for this fragment
		View inflatedView = inflater.inflate(R.layout.finddevicefragment, container, false);

		listView = (ListView) inflatedView.findViewById(R.id.listView);
		scanButton = (Button) inflatedView.findViewById(R.id.scan);
		
		devices = new ArrayList<BluetoothDevice>();
		for (BluetoothDevice device : devices) {
			map = new HashMap<String, String>();
			map.put(DEVICE_NAME, device.getName());
			map.put(DEVICE_ADDRESS, device.getAddress());
			listItems.add(map);
		}

		adapter = new SimpleAdapter((Context)this.getActivity(), listItems,
				R.layout.finddevicefragment_listitem, new String[] { "name", "address" },
				new int[] { R.id.deviceName, R.id.deviceAddr });
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
		
		scanButton.setOnClickListener(new View.OnClickListener() {	
			@Override
			public void onClick(View v) {
                //create a progress bar while the video file is being loaded
                progressDialog = new ProgressDialog(getActivity());
                progressDialog.setMessage("Scanning...");
                progressDialog.setCancelable(false);
                progressDialog.show();
				listItems.clear();
				devices.clear();
				map = new HashMap<String, String>();
				map.put(DEVICE_NAME, "No Devices Found");
				map.put(DEVICE_ADDRESS, "");
				listItems.add(map);
				adapter.notifyDataSetChanged();
				scanLeDevice();

			}
		});
		
		final BluetoothManager mBluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = mBluetoothManager.getAdapter();
		if (mBluetoothAdapter == null) {
			Toast.makeText(this.getActivity(), "Ble not supported", Toast.LENGTH_SHORT)
					.show();
		}
		
		return inflatedView;
	}
	

	@Override
	public void onStart(){
		super.onStart();
		
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
		
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// User chose not to enable Bluetooth.
		if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {

		} else if (requestCode == REQUEST_CODE && resultCode == RESULT_CODE) {
			mDeviceAddress = data.getStringExtra(EXTRA_DEVICE_ADDRESS);
			mDeviceName = data.getStringExtra(EXTRA_DEVICE_NAME);
			//mBluetoothLeService.connect(mDeviceAddress);
		}
        // Handle response from file select dialog
        else if (requestCode == DIALOG_FRAGMENT && resultCode == FILE_SELECTED_CODE) {
            Log.i("FindDeviceFragment", "Dismissed with message: " + data.getStringExtra(FILE_NAME));

            //Create a new intent for the input service and pass in a custom flag that signals
            //the start of the service
            Intent startIntent = new Intent((Context)FindDeviceFragment.this.getActivity(), InputService.class);
            startIntent.putExtra(Constants.ACTION.DEVICE_ADDRESS, mDeviceAddress);
            startIntent.putExtra(Constants.ACTION.OUTPUT_FILENAME, data.getStringExtra(FILE_NAME));
            startIntent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
            Context ctx = (Context)FindDeviceFragment.this.getActivity();
            ctx.startService(startIntent);
            Log.i("FindDeviceFragment", "Started Service");


            getFragmentManager().popBackStack();
        }

		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        //Search existing files to provide an option to append to an existing file
        int newRecordingNumber = 0;
        ArrayList<String> existingFiles = new ArrayList<String>();
        String path = getActivity().getExternalFilesDir(null).getAbsolutePath();
        File recordingFolder = new File(path + "/afib_recordings");
        Log.i("FindDeviceFragment", "ExternalFilesDir = " + path);
        if (!recordingFolder.exists())
        {
            recordingFolder.mkdir();
        }
        File[] files = recordingFolder.listFiles();
        for(File file : files) {
            String fileName = file.getName();
            //If this is an afib recording, get the recording number of a new file
            if(fileName.contains("recording_"))
            {
                existingFiles.add(fileName);
                String recordingAndExtension = fileName.split("_")[1];
                String recordingNumber = recordingAndExtension.split("\\.")[0];
                //If this is the largest recording number
                if(Integer.parseInt(recordingNumber) > newRecordingNumber)
                    newRecordingNumber = Integer.parseInt(recordingNumber);
            }
        }

        //Display the FileSelectDialog
        FileSelectDialogFragment selectDialog = new FileSelectDialogFragment();
        selectDialog.setTargetFragment(FindDeviceFragment.this, DIALOG_FRAGMENT);
        selectDialog.setNewFileName("recording_" + (newRecordingNumber + 1) + ".afib");
        selectDialog.setFileNames(existingFiles);
        selectDialog.show(getFragmentManager(), "FindDeviceFragment");

        //Set the selected device address and name
        HashMap<String, String> hashMap = (HashMap<String, String>) listItems.get(position);
        mDeviceAddress = hashMap.get(DEVICE_ADDRESS);
        mDeviceName = hashMap.get(DEVICE_NAME);
	}

	private void scanLeDevice() {
		new Thread() {

			@Override
			public void run() {
				mBluetoothAdapter.startLeScan(mLeScanCallback);

				try {
					Thread.sleep(SCAN_PERIOD);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				mBluetoothAdapter.stopLeScan(mLeScanCallback);
                progressDialog.dismiss();
			}
		}.start();
	}
	
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice scannedDevice, final int rssi,
				byte[] scanRecord) {

			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (scannedDevice != null) {
						if (listItems.get(0).containsValue("No Devices Found"))
						{
							listItems.clear();
							adapter.notifyDataSetChanged();
						}
						if (devices.indexOf(scannedDevice) == -1)
						{
							devices.add(scannedDevice);
							map = new HashMap<String, String>();
							if(scannedDevice.getName()==null)
								map.put(DEVICE_NAME, "(No Name)");
							else
								map.put(DEVICE_NAME, scannedDevice.getName());
							map.put(DEVICE_ADDRESS, scannedDevice.getAddress());
							listItems.add(map);
							adapter.notifyDataSetChanged();
						}
					}
				}
			});
		}
	};
}
