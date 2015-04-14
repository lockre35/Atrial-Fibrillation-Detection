package com.afib.ui;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
		View inflatedView = inflater.inflate(R.layout.guidefragment, container, false);
		
		//set the media controller buttons
        if(mediaController == null){
            //mediaController = new MediaController(getActivity());
        }

        videoView = (VideoView) inflatedView.findViewById(R.id.videoView);

        //create a progress bar while the video file is being loaded
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Step 1");
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        try{
            //set the media controller in the VideoView
            //videoView.setMediaController(mediaController);
            videoView.setVideoURI(Uri.parse("android.resource://" + MainActivity.PACKAGE_NAME + "/" + R.raw.scene1));
        }catch(Exception e){
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }

        videoView.requestFocus();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                progressDialog.dismiss();
                videoView.seekTo(position);
                if (position == 0) {
                    //videoView.start();
                } else {
                    //if coming from resumed activity, we pause the video
                    //videoView.pause();
                }
            }
        });


        button1 = (Button) inflatedView.findViewById(R.id.button1);

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

/*
		button1 = (Button) inflatedView.findViewById(R.id.button1);
        ImageView sceneImage = (ImageView) inflatedView.findViewById(R.id.imageView);
        sceneImage.setBackgroundResource(R.drawable.scene1);
        rocketAnimation = (AnimationDrawable) sceneImage.getBackground();

		//Add an onclick listener to the button so that we can start and stop the ECG graph
		button1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
                if(!StreamingStatus) {
                    StreamingStatus = true;
                    rocketAnimation.start();
                }
                else {
                    StreamingStatus = false;
                    rocketAnimation.stop();
                }
			}
		});*/

/*
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
                		button1.setText("Start Scene");
                	}else
                	{
                		button1.setText("Stop Scene");
                	}
                	//Change padding back to original position
                	button1.setPadding(0, 0, 0, 0);
                    break;
                }
                return false;

            }           
        });
		*/
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
        videoView.seekTo(position);
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
}
