package com.afib.ui;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.graphics.drawable.AnimationDrawable;
import android.widget.MediaController;
import android.widget.VideoView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afib.graph.GraphThread;
import com.afib.graph.LineGraph;

import org.achartengine.GraphicalView;

/**
 * GuideFragment extends Fragment to present a guide to users
 * 
 * @author Logan
 */
public class GuideFragment extends Fragment{

    private Button button1;
    private VideoView videoView;
    private ProgressDialog progressDialog;
    private MediaController mediaController;
    private int position = 0;
    private Boolean StreamingStatus = false;
    private Button leftButton;
    private Button rightButton;
    private int currentStep = 1;
    private int currentVideo;
    private TextView title;
    private TextView message;

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
	public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState)
	{
		//Initialize the view for this fragment
		View inflatedView = inflater.inflate(R.layout.guidefragment, container, false);

        videoView = (VideoView) inflatedView.findViewById(R.id.videoView);
        leftButton = (Button) inflatedView.findViewById(R.id.button2);
        leftButton.setVisibility(View.INVISIBLE);
        rightButton = (Button) inflatedView.findViewById(R.id.button3);
        button1 = (Button) inflatedView.findViewById(R.id.button1);
        title = (TextView) inflatedView.findViewById(R.id.textView);
        message = (TextView) inflatedView.findViewById(R.id.textView2);
        currentVideo = R.raw.scene1;

        //Add an onclick listener to the button so that we can start and stop the ECG graph
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!StreamingStatus) {
                    StreamingStatus = true;
                    videoView.start();
                }
                else {
                    StreamingStatus = false;
                    videoView.pause();
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
                		button1.setText("Play");
                	}else
                	{
                		button1.setText("Pause");
                	}
                	//Change padding back to original position
                	button1.setPadding(0, 0, 0, 0);
                    break;
                }
                return false;

            }           
        });

        //Add an onclick listener to left button
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentStep > 0)
                {
                    videoView.pause();
                    StreamingStatus = false;
                    button1.setText("Play");
                    currentStep--;
                    UpdateStep();
                    VideoLoader();
                }
            }
        });

        //Add an onclick listener to left button
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentStep < 4)
                {
                    videoView.pause();
                    StreamingStatus = false;
                    button1.setText("Play");
                    currentStep++;
                    UpdateStep();
                    VideoLoader();
                }
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
        position = videoView.getCurrentPosition();
        videoView.pause();
        StreamingStatus = false;
        button1.setText("Play");
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
        VideoLoader();
	}

    //Update text, current video id, and button visibility
    public void UpdateStep()
    {
        switch(currentStep){
            case 1:
                leftButton.setVisibility(View.INVISIBLE);
                title.setText(title1);
                message.setText(message1);
                currentVideo = R.raw.scene1;
                break;
            case 2:
                leftButton.setVisibility(View.VISIBLE);
                title.setText(title2);
                message.setText(message2);
                currentVideo = R.raw.scene1;
                break;
            case 3:
                rightButton.setVisibility(View.VISIBLE);
                title.setText(title3);
                message.setText(message3);
                currentVideo = R.raw.scene1;
                break;
            case 4:
                rightButton.setVisibility(View.INVISIBLE);
                title.setText(title4);
                message.setText(message4);
                currentVideo = R.raw.scene1;
                break;
        }
    }

    //Handle loading of current video
    public void VideoLoader()
    {
        //create a progress bar while the video file is being loaded
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        try{
            //set the media controller in the VideoView
            //videoView.setMediaController(mediaController);
            videoView.setVideoURI(Uri.parse("android.resource://" + MainActivity.PACKAGE_NAME + "/" + currentVideo));
        }catch(Exception e){
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }

        videoView.requestFocus();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                progressDialog.dismiss();
                videoView.setBackgroundColor(Color.TRANSPARENT);
                videoView.seekTo(0);
            }
        });

        videoView.seekTo(position);
    }

    private String title1 = "Step 1";
    private String title2 = "Step 2";
    private String title3 = "Step 3";
    private String title4 = "Step 4";

    private String message1 = "Follow the video below to verify that you have the proper equipment to set up the ECG system.  Navigate between steps using the arrow keys.";
    private String message2 = "Place the three nodes on chest as depicted in the video below.  Once placed, power on the ECG and connect to it using the Find Device option on the mobile application.  Data will begin streaming once connected.";
    private String message3 = "On the mobile application, select the option to View ECG.  Verify the streaming data matches what is depicted below.  If it does not, please try repositioning the node placement.";
    private String message4 = "When the recording is finished, data can be viewed using the Analyze ECG option.  If an error occurs during recording, file recordings can be appended to by selecting the existing file when connecting";
}
