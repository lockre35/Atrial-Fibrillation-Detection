package com.afib.graph;

import org.achartengine.GraphicalView;

import android.app.Activity;
import android.content.Context;
import android.widget.TextView;
import java.util.concurrent.BlockingQueue;

public class GraphThread extends Thread{
	
	//Text view for raw ouput
	private LineGraph Line;
	private GraphicalView View;
	private Point CurrentPoint;
    private BlockingQueue<byte[]> DataQueue;
	
	//Create a new thread by passing in information about the view, the line to display, and the context
	public GraphThread(LineGraph line, GraphicalView view, BlockingQueue<byte[]> dataQueue){
		super();
		this.Line = line;
		this.View = view;
        this.DataQueue = dataQueue;
	}
	
	public void run(){
		
		//Values used for performance evaluation
		long startTime = System.nanoTime();
		long endTime = System.nanoTime();

        try {
            while(!this.isInterrupted())
            {
                //Clear any existing data
                Line.removeAllPoints();
                PointBuilder pointBuilder = new PointBuilder();
                while(DataQueue.size() > 2)
                {
                    byte[] dataFromQueue;
                    if((dataFromQueue = DataQueue.take()) == "Terminate Thread".getBytes())
                    {
                        return;
                    }
                }
                for(int i = 0; i<10; i++)
                {
                    byte[] dataFromQueue;
                    if((dataFromQueue = DataQueue.take()) == "Terminate Thread".getBytes())
                    {
                        return;
                    }
                    else
                    {
                        for(int j = 0; j<dataFromQueue.length; j+=1)
                        {
                            try{
                                //Slow the display down
                                Thread.sleep(6);
                            }catch (InterruptedException e){
                                e.printStackTrace();
                                return;
                            }

                            //Obtain a new point
                            Point p = pointBuilder.getDataFromReciever((i*100) + j,dataFromQueue[j]);


                            //Set the CurrentPoint value so we can print it on the screen as a raw data point (causes lag on the UI thread)
                            CurrentPoint = p;

                            //Add the new point to the line
                            Line.addNewPoints(p);

                            //Update the graph view
                            View.repaint();
                        }
                    }
                }

            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
	}
}
