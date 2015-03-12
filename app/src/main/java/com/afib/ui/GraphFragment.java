package com.afib.ui;

import org.achartengine.GraphicalView;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.afib.graph.GraphThread;
import com.afib.graph.LineGraph;

/**
 * GraphFragment extends Fragment to present a streaming graph to 
 * users.
 * 
 * @author Logan
 */
public class GraphFragment extends Fragment{

	private static GraphicalView view;
	private LineGraph line = new LineGraph();
	private static GraphThread thread;
	private boolean StreamingStatus = false;
	private static RelativeLayout graphView;
	public TextView PhoneMsg;
	public Button button1;
	
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
		View inflatedView = inflater.inflate(R.layout.graphfragment, container, false);
		
		//Locate views in the layout
		button1 = (Button) inflatedView.findViewById(R.id.button1);
		PhoneMsg = (TextView) inflatedView.findViewById(R.id.phoneMessage);
		graphView = (RelativeLayout) inflatedView.findViewById(R.id.chart);
		
		//Add an onclick listener to the button so that we can start and stop the ECG graph
		button1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//If graph thread is currently inactive, start the graph
				if(!StreamingStatus && !thread.isAlive()){
					StreamingStatus = true;
					
					//Refresh the graph
					line.removeAllPoints();
					view = line.getView(getActivity());
					graphView.removeAllViews();
					graphView.addView(view);
					
					//Create a new thread (we can't use the thread that was interrupted)
					thread = new GraphThread(PhoneMsg, line, view, getActivity(), getActivity());
					
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
		
		//return the view for the fragment
		return inflatedView;
	}
	
	/**
	 * Override the onStart method of Fragment so that we can create the graph view
	 * and initialize a graph thread.
	 * 
	 */
	@Override
	public void onStart(){
		//Add the graph view to the screen and initialize a graph thread
		super.onStart();
		view = line.getView(getActivity());
		graphView.addView(view);
		thread = new GraphThread(PhoneMsg, line, view, getActivity(), getActivity());
	}
}
