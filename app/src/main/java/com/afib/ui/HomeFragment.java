package com.afib.ui;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.afib.data.Constants;
import com.afib.data.InputService;

/**
 * Fragment activity used to display a list of basic features to the user.
 * 
 * @author Logan
 */
public class HomeFragment extends Fragment{
	
	public TextView PhoneMsg;
	public Button graphActivityButton;
	public Button findDeviceButton;
	public Button instructionsButton;
	private BroadcastReceiver mReceiver;
	private boolean isServiceRunning;
	
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//Initialize the view for this fragment
		View inflatedView = inflater.inflate(R.layout.homefragment, container, false);
			   
		//Locate views in the layout
		graphActivityButton = (Button) inflatedView.findViewById(R.id.GraphActivityButton);
		findDeviceButton = (Button) inflatedView.findViewById(R.id.Button02);

		//Check if the input service is already running and set the text
		//of the findDeviceButton accordingly
	    if(isMyServiceRunning(InputService.class))
		{
			isServiceRunning = true;
			findDeviceButton.setText("Disconnect");
		}
		
		//Add an onclick listener to the button so that we can start and stop the ECG graph
		graphActivityButton.setOnClickListener(new View.OnClickListener() {	
			@Override
			public void onClick(View v) {					
				// Create new fragment and transaction
			    Fragment newFragment = new GraphFragment(); 
			    // consider using Java coding conventions (upper first char class names!!!)
			    FragmentTransaction transaction = getFragmentManager().beginTransaction();

			    // Replace whatever is in the fragment_container view with this fragment,
			    // and add the transaction to the back stack so that the return button works
			    // properly
			    transaction.replace(android.R.id.content, newFragment);
			    transaction.addToBackStack(null);

			    // Commit the transaction
			    transaction.commit();
			}
		});
		
		//Listener to start the input service
		findDeviceButton.setOnClickListener(new View.OnClickListener() {	
			@Override
			public void onClick(View v) {

				if(!isServiceRunning)
				{
					/*isServiceRunning = true;
					//Create a new intent for the input service and pass in a custom flag that signals
					//the start of the service
					Intent startIntent = new Intent((Context)HomeFragment.this.getActivity(), InputService.class);
					startIntent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
					Context ctx = (Context)HomeFragment.this.getActivity();
					ctx.startService(startIntent);
					Log.i("HomeFragment", "Started Service");*/
					
					// Create new fragment and transaction
				    Fragment newFragment = new FindDeviceFragment(); 
				    // consider using Java coding conventions (upper first char class names!!!)
				    FragmentTransaction transaction = getFragmentManager().beginTransaction();

				    // Replace whatever is in the fragment_container view with this fragment,
				    // and add the transaction to the back stack so that the return button works
				    // properly
				    transaction.replace(android.R.id.content, newFragment);
				    transaction.addToBackStack(null);
				    
				    // Commit the transaction
				    transaction.commit();
				}else if(isServiceRunning)
				{
					//Create a new intent for the input service and pass in a custom flag that signals the
					//end of the service
					Intent startIntent = new Intent((Context)HomeFragment.this.getActivity(), InputService.class);
					startIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
					Context ctx = (Context)HomeFragment.this.getActivity();
					ctx.startService(startIntent);
					isServiceRunning = false;
				}
			}
		});
		
		//Add an ontouch listener so that the text and padding of a button can be changed
		//when clicked
		graphActivityButton.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                //Button is pressed down
                case MotionEvent.ACTION_DOWN:
                	//Create that nice button press effect by changing padding
                	graphActivityButton.setPadding(0, 7, 0, 0);
                    break;
                //Button is released
                case MotionEvent.ACTION_UP:
                	//Change padding back to original position
                	graphActivityButton.setPadding(0, 0, 0, 0);
                    break;
                }
                return false;

            }           
        });
		
		//Add an ontouch listener so that the text and padding of a button can be changed
		//when clicked
		findDeviceButton.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                //Button is pressed down
                case MotionEvent.ACTION_DOWN:
                	//Create that nice button press effect by changing padding
                	findDeviceButton.setPadding(0, 7, 0, 0);
                    break;
                //Button is released
                case MotionEvent.ACTION_UP:
                	//Set the text to the correct value
                	if(findDeviceButton.getText().equals("Find Device"))
                	{
                		findDeviceButton.setText("Disconnect");
                	}else
                	{
                		findDeviceButton.setText("Find Device");
                	}
                	//Change padding back to original position
                	findDeviceButton.setPadding(0, 0, 0, 0);
                    break;
                }
                return false;
            }           
        });	
		   

	    return inflatedView;
	}
	  
	/**
	 * Override the onResume function of Fragment so that we can initialize a 
	 * BroadcastReceiver that allows for data to be sent from the service
	 * to this activity.
	 * 
	 */
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		IntentFilter intentFilter = new IntentFilter("android.intent.action.MAIN");
 
		mReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				//extract our message from intent
				String msgFromService = intent.getStringExtra("some_msg");
				//log our message value
				Log.i("HomeActivity", msgFromService);
 
			}
		};
		//registering our receiver
		getActivity().registerReceiver(mReceiver, intentFilter);
	}
	
	/**
	 * Override the onPause method of Fragment to unregister the BroadcastReceiver
	 * attached to this activity.
	 * 
	 */
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		//unregister our receiver
		getActivity().unregisterReceiver(this.mReceiver);
	}
	
	private boolean isMyServiceRunning(Class<?> serviceClass) {
	    ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (serviceClass.getName().equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}
}
