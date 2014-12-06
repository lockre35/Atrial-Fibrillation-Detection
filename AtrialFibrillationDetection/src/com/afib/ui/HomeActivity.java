package com.afib.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;

public class HomeActivity extends Activity {
	
	public TextView PhoneMsg;
	public Button graphActivityButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		
		//Locate views in the layout
		graphActivityButton = (Button) findViewById(R.id.GraphActivityButton);

		
		//Add an onclick listener to the button so that we can start and stop the ECG graph
		graphActivityButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
	            Intent n = new Intent(HomeActivity.this,GraphActivity.class);
	            startActivity(n);
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
	}

	
}
