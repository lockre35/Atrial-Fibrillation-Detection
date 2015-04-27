package com.afib.ui;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.afib.communication.Constants;
import com.afib.communication.InputService;
import com.afib.graph.AnalyzeGraph;
import com.afib.graph.GraphAnalyzer;
import com.afib.graph.GraphThread;
import com.afib.graph.LineGraph;

import org.achartengine.GraphicalView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * AnalyzeFragment extends Fragment to present a stored data to users.
 * 
 * @author Logan
 */
public class AnalyzeFragment extends Fragment{

    private static GraphicalView view;
    private AnalyzeGraph AnalyzeGraph = new AnalyzeGraph(0);
    private static GraphThread thread;
    private static RelativeLayout graphView;
    public Button button1;
    private BlockingQueue DataQueue;

    private EditText StartField;
    private EditText EndField;
    private Spinner FileSpinner;
    private GraphAnalyzer GraphAnalyzer;
    private ProgressDialog progressDialog;


    private static int SamplesInASecond = 1000 / Constants.ACTION.DELAY_BETWEEN_INPUT;;
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
        View inflatedView = inflater.inflate(R.layout.analyzefragment, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        //Locate views in the layout
        button1 = (Button) inflatedView.findViewById(R.id.button1);
        graphView = (RelativeLayout) inflatedView.findViewById(R.id.chart);
        StartField = (EditText) inflatedView.findViewById(R.id.editText3);
        EndField = (EditText) inflatedView.findViewById(R.id.editText4);
        FileSpinner = (Spinner) inflatedView.findViewById(R.id.editText);

         //Set an adapter so that we can add values to the spinner
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, android.R.id.text1);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        FileSpinner.setAdapter(spinnerAdapter);

        //Get the current files stored on the device
        String path = getActivity().getExternalFilesDir(null).getAbsolutePath();
        File recordingFolder = new File(path + "/afib_recordings");
        File[] files = recordingFolder.listFiles();
        for (File file : files) {
            String fileName = file.getName();
            spinnerAdapter.add(fileName);
        }
        spinnerAdapter.notifyDataSetChanged();


        //Add an onclick listener to the button so that we can start and stop the ECG graph
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkInput()) {
                    //create a progress bar while the video file is being loaded
                    progressDialog = new ProgressDialog(getActivity());
                    progressDialog.setMessage("Loading...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                    String fileName = getActivity().getExternalFilesDir(null).getAbsolutePath() + "/afib_recordings/" + ((TextView) FileSpinner.getSelectedView()).getText().toString();
                    int startTime = Integer.parseInt(StartField.getText().toString());
                    int endTime = Integer.parseInt(EndField.getText().toString());

                    graphView.removeAllViews();
                    AnalyzeGraph = new AnalyzeGraph(startTime);
                    view = AnalyzeGraph.getView(getActivity());
                    graphView.addView(view);
                    GraphAnalyzer = new GraphAnalyzer(AnalyzeGraph, view, fileName, startTime, endTime);
                    GraphAnalyzer.BuildGraph();

                    progressDialog.dismiss();
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
     * a chart.
     *
     */
    @Override
    public void onResume() {
        super.onResume();
        graphView.removeAllViews();
        AnalyzeGraph = new AnalyzeGraph(0);
        view = AnalyzeGraph.getView(getActivity());
        graphView.addView(view);
        DataQueue = new ArrayBlockingQueue(1000);
    }


    /**
     * Override the onPause method of Fragment to clear the chart.
     *
     */
    @Override
    public void onPause() {
        super.onPause();

        AnalyzeGraph.removeAllPoints();
        view.repaint();
        DataQueue.clear();
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
        Log.i("AnalyzeFragment","SamplesPerSecond: " + SamplesInASecond);
    }

    /**
     * Determine if user input is valid
     *
     * @return isValid
     */
    public boolean checkInput(){
        boolean isValid = false;
        String fileName = ((TextView)FileSpinner.getSelectedView()).getText().toString();
        if(StartField.getText().toString() == "" || EndField.getText().toString() == "")
        {
            StartField.setText("0");
            EndField.setText("0");
        }
        int startTime = Integer.parseInt(StartField.getText().toString());
        int endTime = Integer.parseInt(EndField.getText().toString());

        Log.i("AnalyzeFragment", "FileName: " + fileName);
        Log.i("AnalyzeFragment", "StartTime: " + startTime);
        Log.i("AnalyzeFragment", "EndTime: " + endTime);

        File inputFile = new File(getActivity().getExternalFilesDir(null).getAbsolutePath() + "/afib_recordings/" + fileName);

        String errorMessage = null;
        if(!inputFile.exists())
        {
            errorMessage = "Failed to open the file.  Please try again.";
        }
        else if(inputFile.length()/SamplesInASecond < endTime){
            errorMessage = "End time exceeds file size.  File is only " + Long.toString(inputFile.length()/SamplesInASecond) + " secs long.";
            EndField.setText(Long.toString(inputFile.length()/SamplesInASecond));
        }
        else if(startTime < 0)
        {
            errorMessage = "Start time can't be negative.";
            StartField.setText("0");
        }
        else if(endTime <= startTime)
        {
            errorMessage = "End time must be greater than start time.";
            EndField.setText(Integer.toString(startTime + 5));
        }
        else if(endTime - startTime > 20)
        {
            errorMessage = "Max interval is 20 secs.  Please change the start or end time.";
        }

        if(errorMessage != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(errorMessage);
            builder.setTitle("Error");
            AlertDialog dialog = builder.create();
            dialog.show();
            isValid = false;
        }
        else
        {
            isValid = true;
        }
        return isValid;
    }
}
