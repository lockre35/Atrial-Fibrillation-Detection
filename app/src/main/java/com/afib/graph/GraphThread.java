package com.afib.graph;

import org.achartengine.GraphicalView;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.TextView;

public class GraphThread extends Thread{
	
	//Text view for raw ouput
	private TextView RawOutput;
	private LineGraph Line;
	private GraphicalView View;
	private Context Context;
	private Activity Activity;
	private Point CurrentPoint;
    private byte[] data;
	
	//Create a new thread by passing in information about the view, the line to display, and the context
	public GraphThread(TextView rawOutput, LineGraph line, GraphicalView view, Context context, Activity activity, byte[] data){
		super();
		this.RawOutput = rawOutput;
		this.Line = line;
		this.View = view;
		this.Context = context;
		this.Activity = activity;
        this.data = data;
	}
	
	public void run(){
   	 	
		//Clear any existing data
		Line.removeAllPoints();
		
		//Values used for performance evaluation
		long startTime = System.nanoTime();
		long endTime = System.nanoTime();
		
		//If not interrupted (probably don't need this)
		//while(!this.isInterrupted()){
			//Create 3000/10 datapoints for each cycle of the graph
			TestData test = new TestData();
			//test.startInput(this.Context);

			for(int i = 0; i<data.length; i+=1)
			{
				try{
					//Slow the display down
					Thread.sleep(10);
				}catch (InterruptedException e){
					e.printStackTrace();
					return;
				}
				
				//Obtain a new point
				Point p = test.getDataFromReciever(i,data[i]);
				
				//Log time for sanity reasons
				if(p.getX()%630 == 0){
					endTime = System.nanoTime();
					Log.i("GraphThread", "Duration Time = " + (endTime - startTime)/100000);
					startTime = System.nanoTime();
				}
				
				//Set the CurrentPoint value so we can print it on the screen as a raw data point (causes lag on the UI thread)
				CurrentPoint = p;
				
				//To change views in the UI Thread, we need to add changes to a method like this
				if(i%100==0)
				{
					Activity.runOnUiThread(new Runnable() {
					     @Override
					     public void run() {
					    	 	//Display raw data points
								RawOutput.append("(" + CurrentPoint.getX() + "," + CurrentPoint.getY() + ")\r\n");
								return;
					    }
					});
				}
				
				//Add the new point to the line
				Line.addNewPoints(p);
				
				//Update the graph view
				View.repaint();
			}
			//A cycle has completed so we remove all points and start again
			//Line.removeAllPoints();
		//}
	
	}
}
