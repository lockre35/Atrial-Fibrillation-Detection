package com.afib.graph;

import android.content.Context;
import android.util.Log;

import com.afib.data.DataPoint;
import com.afib.data.Frame;
import com.afib.data.InputThread;

public class TestData {

	public static InputThread input;
	public static Frame currentFrame;
	public static Context context;
	
	public TestData()
	{
		
	}
	
	// x is the data number (not auto generated)
	public Point getDataFromReciever(int location, byte voltage)
	{
		//return getPointFromInputThread();
		return new Point(location, voltage & 0xFF);
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

}
