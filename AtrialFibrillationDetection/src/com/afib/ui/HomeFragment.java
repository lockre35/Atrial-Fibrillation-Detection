package com.afib.ui;

import com.afib.data.Constants;
import com.afib.data.InputService;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;



public class HomeFragment extends Fragment{
	
	public TextView PhoneMsg;
	public Button graphActivityButton;
	public Button findDeviceButton;
	public Button instructionsButton;
	private BroadcastReceiver mReceiver;
	
	   @Override
	   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		   
		   View inflatedView = inflater.inflate(R.layout.homefragment, container, false);
		   
			//Locate views in the layout
			graphActivityButton = (Button) inflatedView.findViewById(R.id.GraphActivityButton);
			findDeviceButton = (Button) inflatedView.findViewById(R.id.Button02);
			instructionsButton = (Button) inflatedView.findViewById(R.id.Button03);
			
			//Add an onclick listener to the button so that we can start and stop the ECG graph
			graphActivityButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
		            Intent n = new Intent(getActivity(),GraphActivity.class);
		            startActivity(n);
				}
			});
			
			findDeviceButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent startIntent = new Intent((Context)HomeFragment.this.getActivity(), InputService.class);
					startIntent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
					Context ctx = (Context)HomeFragment.this.getActivity();
					ctx.startService(startIntent);
					Log.i("HomeActivity", "Started Service");
				}
			});
			
			instructionsButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent startIntent = new Intent((Context)HomeFragment.this.getActivity(), InputService.class);
					startIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
					Context ctx = (Context)HomeFragment.this.getActivity();
					ctx.startService(startIntent);
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
	                	//Change padding back to original position
	                	findDeviceButton.setPadding(0, 0, 0, 0);
	                    break;
	                }
	                return false;

	            }           
	        });	
		   
	      /**
	       * return layout
	       */
	      return inflatedView;
	   }
	   
		@Override
		public void onResume() {
			// TODO Auto-generated method stub
			super.onResume();
	 
			IntentFilter intentFilter = new IntentFilter(
					"android.intent.action.MAIN");
	 
			mReceiver = new BroadcastReceiver() {
	 
				@Override
				public void onReceive(Context context, Intent intent) {
					//extract our message from intent
					String msgFromService = intent.getStringExtra("some_msg");
					//log our message value
					//Log.i("HomeActivity", msgFromService);
	 
				}
			};
			//registering our receiver
			getActivity().registerReceiver(mReceiver, intentFilter);
		}
		
		@Override
		public void onPause() {
			// TODO Auto-generated method stub
			super.onPause();
			//unregister our receiver
			getActivity().unregisterReceiver(this.mReceiver);
		}
	}
