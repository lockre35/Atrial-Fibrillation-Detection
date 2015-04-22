package com.afib.ui;

import org.achartengine.GraphicalView;
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
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afib.communication.Constants;
import com.afib.graph.GraphThread;
import com.afib.graph.LineGraph;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;

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
    private BroadcastReceiver mReceiver;
    private BlockingQueue DataQueue;

	
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
                if(!StreamingStatus) {
                    byte[] data = new byte[100];
                    thread = new GraphThread(PhoneMsg, line, view, getActivity(), getActivity(), data, DataQueue);
                    thread.start();
                    StreamingStatus = true;
                    PhoneMsg.append("Output Started!\n");
                }
                else {
                    StreamingStatus = false;
                    try{
                        DataQueue.put("Terminate Thread".getBytes());
                        PhoneMsg.append("Output Stopped!\n");
                    }catch(InterruptedException e){
                        e.printStackTrace();
                    }
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
                //String msgFromService = intent.getStringExtra("EXTRA_DATA");
                byte[] data = intent.getByteArrayExtra(Constants.ACTION.STREAM_DATA);
                //byte[] data = msgFromService.getBytes();
                //log our message value
                Log.i("GraphFragment", "BroadcastReceiver: " + data.toString());
                //If graph thread is currently inactive, start the graph
                if(StreamingStatus == true){
                    //Apply and interrupt to the thread and print some output
                    //thread.interrupt();

/*                    try{
                        DataQueue.put("Terminate Thread".getBytes());
                    }catch(InterruptedException e){
                        e.printStackTrace();
                    }*/
/*                    PhoneMsg.append("Output Stopped!\n");

                    //Refresh the graph
                    line.removeAllPoints();
                    view = line.getView(getActivity());
                    graphView.removeAllViews();
                    graphView.addView(view);

                    //Create a new thread (we can't use the thread that was interrupted)
                    thread = new GraphThread(PhoneMsg, line, view, getActivity(), getActivity(), data, DataQueue);

                    //Start the new thread and print some ouput
                    thread.start();
                    PhoneMsg.append("Output Started!\n");*/
                    try{
                        DataQueue.put(data);
                    }catch(InterruptedException e){
                        e.printStackTrace();
                    }

                    //If graph thread is currently active, stop the graph
                }

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
        try{
            if(thread.isAlive())
                DataQueue.put("Terminate Thread".getBytes());
        }catch(InterruptedException e){
            e.printStackTrace();
        }

        PhoneMsg.append("Output Stopped!\n");
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
        byte[] data = new byte[100];
        DataQueue = new ArrayBlockingQueue(1000);
	}
}
