package com.afib.graph;

import android.util.Log;

import com.afib.data.*;

public class TestData {

	public static InputThread input;
	public static Frame currentFrame;
	
	public TestData()
	{
		
	}
	
	// x is the data number (not auto generated)
	public Point getDataFromReciever(int x)
	{
		//return getPointFromInputThread();
		return new Point(x, generateRandomData(x));
	}
	
	// generate y values based on the sine wave
	private int generateRandomData(double x)
	{
		/*
		Random random = new Random();
		return random.nextInt(5);
		*/
		
		//Need to modify values since the graph expects int's, not double's
		//(Will likely need to modify the graph to support doubles)
		double y = Math.sin(x/100);
		y = y*100;
		return (int) Math.round(y);
	}
	
	
	private Point getPointFromInputThread()
	{
		if(currentFrame.DataPoints.isEmpty())
		{
			currentFrame = input.InputStack.pop();
		}
		if(!currentFrame.DataPoints.isEmpty())
		{
			DataPoint p = currentFrame.DataPoints.pop();
			return new Point(p.getX(),p.getY());
		}
		else
		{
			return new Point(0,0);
		}
	}
	
	public void startInput()
	{
		input = new InputThread();
		input.start();
		currentFrame = input.InputStack.pop();
		Log.i("TestData", "Input Thread Started");
	}
}
