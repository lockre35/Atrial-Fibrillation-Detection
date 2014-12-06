package com.afib.ui;

import org.achartengine.GraphicalView;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afib.graph.GraphThread;
import com.afib.graph.LineGraph;

public class GraphActivity extends Activity {

	private static GraphicalView view;
	private LineGraph line = new LineGraph();
	private static GraphThread thread;
	private boolean StreamingStatus = false;
	private static RelativeLayout graphView;
	
	public TextView PhoneMsg;
	public Button button1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_graph);
		
		//Locate views in the layout
		button1 = (Button) findViewById(R.id.button1);
		PhoneMsg = (TextView) findViewById(R.id.phoneMessage);
		graphView = (RelativeLayout) findViewById(R.id.chart);
		
		//Add an onclick listener to the button so that we can start and stop the ECG graph
		button1.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//If graph thread is currently inactive, start the graph
				if(!StreamingStatus && !thread.isAlive()){
					StreamingStatus = true;
					
					//Refresh the graph
					line.removeAllPoints();
					view = line.getView(GraphActivity.this);
					graphView.removeAllViews();
					graphView.addView(view);
					
					//Create a new thread (we can't use the thread that was interrupted)
					thread = new GraphThread(PhoneMsg, line, view, GraphActivity.this, GraphActivity.this);
					
					//Start the new thread and print some ouput
					thread.start();
					PhoneMsg.append("Output Started!\n");
					
				//If graph thread is currently active, stop the graph
				}else{
					StreamingStatus = false;
					//Apply and interrupt to the thread and print some output
					thread.interrupt();
					PhoneMsg.append("Output Stopped!\n");
				}
			}
		});
		
		//Add an ontouch listener so that the text and padding of a button can be changed
		//when clicked
		button1.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                //Button is pressed down
                case MotionEvent.ACTION_DOWN:
                	//Create that nice button press effect by changing padding
                    button1.setPadding(0, 7, 0, 0);
                    break;
                //Button is released
                case MotionEvent.ACTION_UP:
                	//Set the text to the correct value
                	if(StreamingStatus)
                	{
                		button1.setText("Start ECG");
                	}else
                	{
                		button1.setText("Stop ECG");
                	}
                	//Change padding back to original position
                	button1.setPadding(0, 0, 0, 0);
                    break;
                }
                return false;

            }           
        });
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onStart(){
		//Add the graph view to the screen and initialize a graph thread
		super.onStart();
		view = line.getView(this);
		graphView.addView(view);
		thread = new GraphThread(PhoneMsg, line, view, this, GraphActivity.this);
	}
}
