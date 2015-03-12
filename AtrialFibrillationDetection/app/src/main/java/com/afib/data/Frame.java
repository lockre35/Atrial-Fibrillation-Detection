package com.afib.data;

import java.util.Stack;

public class Frame {

	public Stack<DataPoint> DataPoints;
	
	public Frame()
	{
		DataPoints = new Stack<DataPoint>();
	}
	
	public void AddPoint(DataPoint p)
	{
		DataPoints.add(p);
	}
}
