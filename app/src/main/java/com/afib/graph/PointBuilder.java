package com.afib.graph;

import android.content.Context;
import android.util.Log;

import com.afib.data.DataPoint;
import com.afib.data.Frame;
import com.afib.data.InputThread;

public class PointBuilder {
	public static Context context;
	
	// x is the data number (not auto generated)
	public Point getDataFromReciever(int location, byte voltage)
	{
		return new Point(location, voltage & 0xFF);
	}
	
	// generate y values based on the sine wave
	private int generateRandomData(double x)
	{
		//Need to modify values since the graph expects int's, not double's
		//(Will likely need to modify the graph to support doubles)
		double y = Math.sin(x/100);
		y = y*100;
		return (int) Math.round(y);
	}

}
