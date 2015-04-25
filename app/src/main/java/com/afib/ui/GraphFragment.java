package com.afib.ui;

import android.app.ActivityManager;
import android.app.DownloadManager;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
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
import com.afib.communication.InputService;
import com.afib.graph.GraphThread;
import com.afib.graph.LineGraph;

import org.achartengine.GraphicalView;

import java.sql.Time;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

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
	public Button button1;
    private BroadcastReceiver mReceiver;
    private BlockingQueue DataQueue;
    private boolean IsServiceRunning;
    private TextView message1;
    private TextView message2;
    private Runnable checkStatusTask;
    private Handler mHandler;

	private long TimeOfLastServiceStatusResponse;
    private long TimeOfLastDataObtained;
    private boolean ServiceAppearsActive;
    private boolean DataErrorDisplayed;

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
		graphView = (RelativeLayout) inflatedView.findViewById(R.id.chart);
        message1 = (TextView) inflatedView.findViewById(R.id.textView);
        message2 = (TextView) inflatedView.findViewById(R.id.textView2);

		//Add an onclick listener to the button so that we can start and stop the ECG graph
		button1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
                if(IsServiceRunning)
                {
                    if(!StreamingStatus) {
                        line.removeAllPoints();
                        view.repaint();
                        DataQueue.clear();
                        thread = new GraphThread(line, view, DataQueue);
                        thread.start();
                        StreamingStatus = true;
                    }
                    else {
                        StreamingStatus = false;
                        try{
                            DataQueue.put("Terminate Thread".getBytes());
                            thread.interrupt();
                        }catch(InterruptedException e){
                            e.printStackTrace();
                        }
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
                    if(IsServiceRunning)
                    {
                        //Set the text to the correct value
                        if(StreamingStatus)
                        {
                            button1.setText("Start ECG");
                        }else
                        {
                            button1.setText("Stop ECG");
                        }
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
        super.onResume();
        graphView.removeAllViews();
        line = new LineGraph();
        view = line.getView(getActivity());
        graphView.addView(view);
        DataQueue = new ArrayBlockingQueue(1000);

        //Send a request to get the status of the stream
        IsServiceRunning = isMyServiceRunning(InputService.class);
        if(IsServiceRunning) {
            Intent startIntent = new Intent((Context) GraphFragment.this.getActivity(), InputService.class);
            startIntent.setAction(Constants.ACTION.CHECK_STATUS);
            Context ctx = (Context) GraphFragment.this.getActivity();
            ctx.startService(startIntent);
            TimeOfLastServiceStatusResponse = 0;
            TimeOfLastDataObtained = 0;
            ServiceAppearsActive = true;
        } else {
            //RED COLOR   : message1.setTextColor(Color.argb(255, 159, 25, 12));
            //GREEN COLOR : message2.setTextColor(Color.argb(255, 44, 153, 3));
            message1.setTextColor(Color.argb(255, 159, 25, 12));
            message1.setText("Streaming service not running!");
            message2.setTextColor(Color.BLACK);
            message2.setText("Please go to the home screen to connect to an ECG and begin streaming.");
        }

        IntentFilter intentFilter = new IntentFilter("android.intent.action.MAIN");

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.hasExtra(Constants.ACTION.STREAM_DATA))
                {
                    //extract our message from intent
                    byte[] data = intent.getByteArrayExtra(Constants.ACTION.STREAM_DATA);

                    //update the time of last data obtained
                    TimeOfLastDataObtained = System.currentTimeMillis() / 1000;

                    //log our message value
                    Log.i("GraphFragment", "BroadcastReceiver: " + data.toString());
                    //If graph thread is currently inactive, start the graph
                    if(StreamingStatus == true){
                        //Apply and interrupt to the thread and print some output
                        try{
                            DataQueue.put(data);
                        }catch(InterruptedException e){
                            e.printStackTrace();
                        }

                        //If graph thread is currently active, stop the graph
                    }
                }
                else if(intent.hasExtra(Constants.ACTION.STREAM_STATUS))
                {
                    ServiceAppearsActive = true;
                    TimeOfLastServiceStatusResponse = System.currentTimeMillis() / 1000;

                    if(intent.getBooleanExtra(Constants.ACTION.STREAM_STATUS, false))
                    {
                        message2.setText("ECG data being recorded");
                        message2.setTextColor(Color.argb(255, 44, 153, 3));
                    }
                    else
                    {
                        message2.setText("ECG data not being stored");
                        message2.setTextColor(Color.argb(255, 159, 25, 12));
                    }
                }
            }
        };
        //registering our receiver
        getActivity().registerReceiver(mReceiver, intentFilter);

        //Set up the runnable that periodically checks the status of the streaming service
        mHandler = new Handler(Looper.getMainLooper());
        checkStatusTask = new Runnable() {
            @Override
            public void run() {
                //make call to status logic function
                verifyServiceStatus();
                checkDataInput();
                mHandler.postDelayed(this, 4000);
            }
        };

        mHandler.postDelayed(checkStatusTask, 4000);
        DataErrorDisplayed = true;
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
            if(thread != null && thread.isAlive())
                DataQueue.put("Terminate Thread".getBytes());
            thread.interrupt();
        }catch(Exception e){
            e.printStackTrace();
        }
        mHandler.removeCallbacks(checkStatusTask);

        line.removeAllPoints();
        view.repaint();
        DataQueue.clear();
        button1.setText("Start ECG");
        StreamingStatus = false;
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
	}

    /**
     * Check the status of the background service
     * @param serviceClass
     * @return
     */
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Handle logic for determining how the background service is doing
     * Also, updates alert messages accordingly
     */
    private void verifyServiceStatus(){
        //Check the last time we received a message from the service
        long currentTime = System.currentTimeMillis() / 1000;
        if(ServiceAppearsActive && (currentTime - TimeOfLastServiceStatusResponse) > 5){
            ServiceAppearsActive = false;
            //Set alert messages here
            message1.setText("No response from service");
            message1.setTextColor(Color.argb(255, 159, 25, 12));
            message2.setText("Please try disconnecting from the ECG and connecting again.");
            message2.setTextColor(Color.BLACK);
        } else if(ServiceAppearsActive){
            Intent startIntent = new Intent((Context) GraphFragment.this.getActivity(), InputService.class);
            startIntent.setAction(Constants.ACTION.CHECK_STATUS);
            Context ctx = (Context) GraphFragment.this.getActivity();
            ctx.startService(startIntent);
        }
    }

    /**
     * Handle logic for checking when the last data was obtained.
     * (Tells us if phone is getting data from ECG)
     */
    private void checkDataInput(){
        //Check the time of the last data obtained from the service
        //Check the last time we received a message from the service
        long currentTime = System.currentTimeMillis() / 1000;
        if(ServiceAppearsActive && (currentTime - TimeOfLastDataObtained) > 6){
            //Set alert messages here
            message1.setText("No data from ECG");
            message1.setTextColor(Color.argb(255, 159, 25, 12));
            DataErrorDisplayed = true;
        } else if(ServiceAppearsActive && DataErrorDisplayed){
            //Set alert messages here
            message1.setText("Device streaming properly");
            message1.setTextColor(Color.argb(255, 44, 153, 3));
        }
    }
}
