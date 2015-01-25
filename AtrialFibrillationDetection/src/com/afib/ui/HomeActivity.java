package com.afib.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;

import com.afib.data.Constants;
import com.afib.data.InputService;

public class HomeActivity extends Activity {
	
	public TextView PhoneMsg;
	public Button graphActivityButton;
	public Button findDeviceButton;
	public Button instructionsButton;
	private BroadcastReceiver mReceiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		
		//Locate views in the layout
		graphActivityButton = (Button) findViewById(R.id.GraphActivityButton);
		findDeviceButton = (Button) findViewById(R.id.Button02);
		instructionsButton = (Button) findViewById(R.id.Button03);
		
		//Add an onclick listener to the button so that we can start and stop the ECG graph
		graphActivityButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
	            Intent n = new Intent(HomeActivity.this,GraphActivity.class);
	            startActivity(n);
			}
		});
		
		findDeviceButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent startIntent = new Intent(HomeActivity.this, InputService.class);
				startIntent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
				startService(startIntent);
				Log.i("HomeActivity", "Started Service");
			}
		});
		
		instructionsButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent startIntent = new Intent(HomeActivity.this, InputService.class);
				startIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
				startService(startIntent);
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
	}

	@Override
	protected void onResume() {
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
				Log.i("HomeActivity", msgFromService);
 
			}
		};
		//registering our receiver
		this.registerReceiver(mReceiver, intentFilter);
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		//unregister our receiver
		this.unregisterReceiver(this.mReceiver);
	}
}
